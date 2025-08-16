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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class AccountManagerServiceImplTest {

    @Autowired
    private AccountManagerService accountManagerService;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    private RedissonClient redissonClient;

    @MockBean
    private RedisUtils redisUtils;

    @Autowired
    private TransactionManager transactionManager;

    // 模拟分布式锁
    @BeforeEach
    void setUp() throws InterruptedException {
        RLock mockLock = mock(RLock.class);
        when(redissonClient.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        doNothing().when(mockLock).unlock();
        when(mockLock.isHeldByCurrentThread()).thenReturn(true);
    }

    @Test
    @Transactional
    void testCreateAccount_Success() {
        CreateAccountRequestParam request = new CreateAccountRequestParam();
        request.setAccountNumber("TEST123456");
        request.setAccountHolderName("Test User");
        request.setContactNumber("1234567890");

        AccountInfoResponse response = accountManagerService.createAccount(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(request.getAccountNumber(), response.getAccountNumber());
        Assertions.assertEquals(request.getAccountHolderName(), response.getAccountHolderName());
        Assertions.assertEquals(new BigDecimal("0.00"), response.getBalance());
    }

    @Test
    @Transactional
    void testCreateAccount_DuplicateAccountNumber() {
        CreateAccountRequestParam request = new CreateAccountRequestParam();
        request.setAccountNumber("TEST123456");
        request.setAccountHolderName("Test User");
        request.setContactNumber("1234567890");
        accountManagerService.createAccount(request);

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () -> {
            accountManagerService.createAccount(request);
        });

        Assertions.assertEquals("BM-002", exception.getErrorCode());
    }

    @Test
    @Transactional
    void testGetAccount_Success() {
        String accountNumber = "TEST123456";
        createTestAccount(accountNumber);

        AccountInfoResponse response = accountManagerService.getAccount(accountNumber);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(accountNumber, response.getAccountNumber());
    }

    @Test
    @Transactional
    void testGetAccount_NotFound() {
        BusinessException exception = Assertions.assertThrows(BusinessException.class, () -> {
            accountManagerService.getAccount("NOT_EXIST");
        });

        Assertions.assertNotNull(exception);
    }

    @Test
    @Transactional
    void testUpdateAccount_Success() {
        String accountNumber = "TEST123456";
        createTestAccount(accountNumber);

        UpdateAccountRequestParam updateRequest = new UpdateAccountRequestParam();
        updateRequest.setAccountHolderName("Updated Name");
        updateRequest.setContactNumber("9876543210");

        AccountInfoResponse updatedAccount = accountManagerService.updateAccount(accountNumber, updateRequest);

        Assertions.assertNotNull(updatedAccount);
        Assertions.assertEquals("Updated Name", updatedAccount.getAccountHolderName());
        Assertions.assertEquals("9876543210", updatedAccount.getContactNumber());
    }

    @Test
    @Transactional
    void testDeleteAccount_Success() {
        String accountNumber = "TEST123456";
        createTestAccount(accountNumber);

        OperationResponse response = accountManagerService.deleteAccount(accountNumber);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("success", response.getStatus());

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () -> {
            accountManagerService.getAccount(accountNumber);
        });
        Assertions.assertNotNull(exception);
    }

    @Test
    @Transactional
    void testListAllAccounts() {
        createTestAccount("TEST001");
        createTestAccount("TEST002");

        Pageable pageable = PageRequest.of(0, 10);
        Page<AccountInfoResponse> accounts = accountManagerService.listAllAccounts(pageable);

        Assertions.assertNotNull(accounts);
        Assertions.assertEquals(2, accounts.getTotalElements());
    }

    @Test
    @Transactional
    void testTransferFunds_Success() {
        String fromAccount = "FROM123";
        String toAccount = "TO456";
        createTestAccountWithBalance(fromAccount, new BigDecimal("1000.00"));
        createTestAccountWithBalance(toAccount, new BigDecimal("500.00"));

        TransferRequestParam request = new TransferRequestParam();
        request.setFromAccount(fromAccount);
        request.setToAccount(toAccount);
        request.setAmount(new BigDecimal("300.00"));

        OperationResponse response = accountManagerService.transferFunds(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("success", response.getStatus());

        AccountInfoResponse fromAccountAfter = accountManagerService.getAccount(fromAccount);
        AccountInfoResponse toAccountAfter = accountManagerService.getAccount(toAccount);

        Assertions.assertEquals(new BigDecimal("700.00"), fromAccountAfter.getBalance());
        Assertions.assertEquals(new BigDecimal("800.00"), toAccountAfter.getBalance());
    }

    @Test
    @Transactional
    void testTransferFunds_InsufficientBalance() {
        String fromAccount = "FROM123";
        String toAccount = "TO456";
        createTestAccountWithBalance(fromAccount, new BigDecimal("100.00"));
        createTestAccountWithBalance(toAccount, new BigDecimal("500.00"));

        TransferRequestParam request = new TransferRequestParam();
        request.setFromAccount(fromAccount);
        request.setToAccount(toAccount);
        request.setAmount(new BigDecimal("300.00"));

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () -> {
            accountManagerService.transferFunds(request);
        });

        Assertions.assertNotNull(exception);
    }

    @Test
    @Transactional
    void testTransferFunds_SameAccount() {
        String accountNumber = "TEST123";
        createTestAccountWithBalance(accountNumber, new BigDecimal("1000.00"));

        TransferRequestParam request = new TransferRequestParam();
        request.setFromAccount(accountNumber);
        request.setToAccount(accountNumber);
        request.setAmount(new BigDecimal("100.00"));

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () -> {
            accountManagerService.transferFunds(request);
        });

        Assertions.assertNotNull(exception);
    }

    @Test
    void testTransferFunds_currency() throws Exception {
        String fromAccount = "FROM123";
        String toAccount = "TO456";
        createTestAccountWithBalance(fromAccount, new BigDecimal("1000.00"));
        createTestAccountWithBalance(toAccount, new BigDecimal("500.00"));

        TransferRequestParam request = new TransferRequestParam();
        request.setFromAccount(fromAccount);
        request.setToAccount(toAccount);
        request.setAmount(new BigDecimal("10.00"));

        TransferRequestParam request1 = new TransferRequestParam();
        request1.setFromAccount(toAccount);
        request1.setToAccount(fromAccount);
        request1.setAmount(new BigDecimal("5.00"));

        // 执行转账
        List<CompletableFuture<Void>> completableFutureList = new ArrayList<>();
        for (int i = 0; i < 80; i++) {
            completableFutureList.add(CompletableFuture.runAsync(() -> accountManagerService.transferFunds(request)));
        }

        for (int i = 0; i < 80; i++) {
            completableFutureList.add(CompletableFuture.runAsync(() -> accountManagerService.transferFunds(request1)));
        }
        CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[0])).join();

        AccountInfoResponse fromAccountAfter = accountManagerService.getAccount(fromAccount);
        AccountInfoResponse toAccountAfter = accountManagerService.getAccount(toAccount);

        Assertions.assertEquals(new BigDecimal("600.00"), fromAccountAfter.getBalance());
        Assertions.assertEquals(new BigDecimal("900.00"), toAccountAfter.getBalance());
    }

    private void createTestAccount(String accountNumber) {
        CreateAccountRequestParam request = new CreateAccountRequestParam();
        request.setAccountNumber(accountNumber);
        request.setAccountHolderName("Test User");
        request.setContactNumber("1234567890");
        accountManagerService.createAccount(request);
    }

    private void createTestAccountWithBalance(String accountNumber, BigDecimal balance) {
        BankAccountPo account = BankAccountPo.builder()
                .accountNumber(accountNumber)
                .accountHolderName(accountNumber)
                .contactNumber(accountNumber)
                .balance(balance)
                .createTime(System.currentTimeMillis())
                .build();
        accountRepository.save(account);
    }
}

