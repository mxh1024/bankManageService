package com.mxh.bank.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class TransferRequestParam {

    @NotBlank(message = "BM-008")
    @JsonProperty("from_account")
    private String fromAccount;

    @NotBlank(message = "BM-008")
    @JsonProperty("to_account")
    private String toAccount;

    @DecimalMin(value = "0.01", message = "BM-009")
    @Digits(integer = 9, fraction = 2, message = "BM-009")
    @JsonProperty("amount")
    private BigDecimal amount;
}
