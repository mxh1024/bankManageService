package com.mxh.bank.service;

import com.mxh.bank.model.request.CreateAccountRequestParam;
import com.mxh.bank.model.request.TransferRequestParam;
import com.mxh.bank.model.request.UpdateAccountRequestParam;
import com.mxh.bank.model.response.AccountInfoResponse;
import com.mxh.bank.model.response.OperationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountManagerService {
    AccountInfoResponse createAccount(CreateAccountRequestParam account);

    AccountInfoResponse getAccount(String accountNumber);

    AccountInfoResponse updateAccount(String accountNumber, UpdateAccountRequestParam updatedAccount);

    OperationResponse deleteAccount(String accountNumber);

    Page<AccountInfoResponse> listAllAccounts(Pageable pageable);

    OperationResponse transferFunds(TransferRequestParam transferRequestParam);
}
