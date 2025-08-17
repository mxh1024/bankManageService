package com.mxh.bank.repository;

import com.mxh.bank.model.po.BankAccountPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

@Repository
public interface AccountRepository extends JpaRepository<BankAccountPo, Long> {
    BankAccountPo findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    void deleteByAccountNumber(String accountNumber);

    @Lock(PESSIMISTIC_WRITE)
    Optional<BankAccountPo> findWithLockByAccountNumber(String accountNumber);
}
