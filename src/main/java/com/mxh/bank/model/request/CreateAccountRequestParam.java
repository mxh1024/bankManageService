package com.mxh.bank.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAccountRequestParam {

    @NotBlank(message = "BM-008")
    @JsonProperty("account_number")
    private String accountNumber;

    @NotBlank(message = "BM-008")
    @JsonProperty("account_holder_name")
    private String accountHolderName;

    @NotBlank(message = "BM-008")
    @JsonProperty("contact_number")
    private String contactNumber;
}
