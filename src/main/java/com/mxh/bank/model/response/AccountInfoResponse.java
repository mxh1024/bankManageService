package com.mxh.bank.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AccountInfoResponse {

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("account_holder_name")
    private String accountHolderName;

    @JsonProperty("contact_number")
    private String contactNumber;

    @JsonProperty("balance")
    private BigDecimal balance;
}
