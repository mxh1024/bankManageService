package com.mxh.bank.service.impl;

import com.mxh.bank.exception.BusinessException;
import com.mxh.bank.model.po.BankAccountPo;
import com.mxh.bank.model.request.CreateAccountRequestParam;
import com.mxh.bank.model.request.TransferRequestParam;
import com.mxh.bank.model.request.UpdateAccountRequestParam;
import com.mxh.bank.model.response.AccountInfoResponse;
import com.mxh.bank.model.response.OperationResponse;
import com.mxh.bank.repository.AccountRepository;
import com.mxh.bank.service.AccountManagerService;
import com.mxh.bank.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AccountManagerServiceImpl implements AccountManagerService {
    private static final String ACCOUNT_LOCK_PREFIX = "account:lock:";
    private static final String ACCOUNT_CACHE_PREFIX = "account:cache:";

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    @Transactional
    public AccountInfoResponse createAccount(CreateAccountRequestParam accountRequestParam) {
        String lockKey = ACCOUNT_LOCK_PREFIX + accountRequestParam.getAccountNumber();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean locked = lock.tryLock(2, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("BM-003", accountRequestParam.getAccountNumber());
            }

            if (accountRepository.existsByAccountNumber(accountRequestParam.getAccountNumber())) {
                throw new BusinessException("BM-002", accountRequestParam.getAccountNumber());
            }
            // 创建并保存新账户
            BankAccountPo account = BankAccountPo.builder()
                    .accountNumber(accountRequestParam.getAccountNumber())
                    .accountHolderName(accountRequestParam.getAccountHolderName())
                    .contactNumber(accountRequestParam.getContactNumber())
                    .balance(new BigDecimal("0.00"))
                    .createTime(System.currentTimeMillis())
                    .build();
            accountRepository.save(account);
            redisUtils.set(getCacheKey(accountRequestParam.getAccountNumber()), account, 10, TimeUnit.MINUTES);
            log.info("create account = {} finish", account);
            return mapToAccountResponse(account);
        } catch (InterruptedException e) {
            log.error("create account error, accountNumber = {}", accountRequestParam.getAccountNumber(), e);
            throw new BusinessException("BM-004");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public AccountInfoResponse getAccount(String accountNumber) {
        BankAccountPo accountFromCache = redisUtils.get(getCacheKey(accountNumber), BankAccountPo.class);
        if (accountFromCache != null) {
            return mapToAccountResponse(accountFromCache);
        }
        BankAccountPo account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            log.error("accountNumber = {} is not exist", accountNumber);
            throw new BusinessException("BM-005", accountNumber);
        }
        return mapToAccountResponse(account);
    }

    @Override
    @Transactional
    public AccountInfoResponse updateAccount(String accountNumber, UpdateAccountRequestParam updatedAccount) {
        String lockKey = ACCOUNT_LOCK_PREFIX + accountNumber;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("BM-003", accountNumber);
            }
            BankAccountPo account = accountRepository.findWithLockByAccountNumber(accountNumber).orElseThrow(() ->
                    new BusinessException("BM-005", accountNumber));
            account.setAccountHolderName(updatedAccount.getAccountHolderName());
            account.setContactNumber(updatedAccount.getContactNumber());
            accountRepository.save(account);
            redisUtils.set(getCacheKey(accountNumber), account, 10, TimeUnit.MINUTES);
            log.info("update account = {} finish", account);
            return mapToAccountResponse(account);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional
    public OperationResponse deleteAccount(String accountNumber) {
        String lockKey = ACCOUNT_LOCK_PREFIX + accountNumber;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("BM-002", accountNumber);
            }
            BankAccountPo account = accountRepository.findByAccountNumber(accountNumber);
            if (account == null) {
                log.error("accountNumber = {} is not exist", accountNumber);
                throw new BusinessException("BM-005", accountNumber);
            }
            accountRepository.deleteByAccountNumber(accountNumber);
            redisUtils.delete(getCacheKey(accountNumber));
            log.info("accountNumber = {} delete finish", accountNumber);
            return new OperationResponse("success", "delete finish");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public Page<AccountInfoResponse> listAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable)
                .map(this::mapToAccountResponse);
    }

    @Override
    @Transactional
    public OperationResponse transferFunds(TransferRequestParam transferRequestParam) {
        String toAccountNo = transferRequestParam.getToAccount();
        String fromAccountNo = transferRequestParam.getFromAccount();
         if (fromAccountNo.equals(toAccountNo)) {
            throw new BusinessException("BM-006");
        }
        String firstLock;
        String secondLock;
        if (toAccountNo.compareTo(fromAccountNo) < 0) {
            firstLock = toAccountNo;
            secondLock = fromAccountNo;
        } else {
            firstLock = fromAccountNo;
            secondLock = toAccountNo;
        }

        BankAccountPo firstAccount = accountRepository.findByAccountNumberForUpdate(firstLock).orElseThrow(() -> new BusinessException("BM-005", firstLock));
        BankAccountPo secondAccount = accountRepository.findByAccountNumberForUpdate(secondLock).orElseThrow(() -> new BusinessException("BM-005", secondLock));

        BankAccountPo fromAccount = firstLock.equals(fromAccountNo) ? firstAccount : secondAccount;
        BankAccountPo toAccount = firstLock.equals(fromAccountNo) ? secondAccount : firstAccount;

        // 验证余额是否充足
        if (fromAccount.getBalance().compareTo(transferRequestParam.getAmount()) < 0) {
            throw new BusinessException("BM-007");
        }

        BigDecimal amount = transferRequestParam.getAmount();
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        redisUtils.delete(getCacheKey(fromAccountNo));
        redisUtils.delete(getCacheKey(toAccountNo));
        log.info("account = {} to account = {}, amount = {}", fromAccount, toAccount, amount);
        return new OperationResponse("success", "transfer finish");
    }

    private AccountInfoResponse mapToAccountResponse(BankAccountPo account) {
        return AccountInfoResponse.builder()
                .accountNumber(account.getAccountNumber())
                .accountHolderName(account.getAccountHolderName())
                .contactNumber(account.getContactNumber())
                .balance(account.getBalance())
                .build();
    }

    private String getCacheKey(String accountNumber) {
        return ACCOUNT_CACHE_PREFIX + accountNumber;
    }
}
