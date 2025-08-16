package com.mxh.bank.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OperationResponse {
    @JsonProperty("status")
    private final String status;

    @JsonProperty("operation_message")
    private final String operationMsg;

    public OperationResponse(String status, String operationMsg) {
        this.status = status;
        this.operationMsg = operationMsg;
    }
}
