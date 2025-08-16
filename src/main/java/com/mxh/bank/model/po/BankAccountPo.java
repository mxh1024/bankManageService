package com.mxh.bank.model.po;


import com.mxh.bank.utils.IdUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "bank_account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountPo {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "account_holder_name", nullable = false)
    private String accountHolderName;
    @Column(name = "contact_number", nullable = false)
    private String contactNumber;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "create_time", nullable = false)
    private Long createTime = System.currentTimeMillis();

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = IdUtils.getNextId();
        }
    }
}
