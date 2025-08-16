package com.mxh.bank.controller;


import com.mxh.bank.model.request.CreateAccountRequestParam;
import com.mxh.bank.model.request.TransferRequestParam;
import com.mxh.bank.model.request.UpdateAccountRequestParam;
import com.mxh.bank.model.response.AccountInfoResponse;
import com.mxh.bank.model.response.OperationResponse;
import com.mxh.bank.service.AccountManagerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bank-manager")
public class BankAccountController {

    @Autowired
    private AccountManagerService accountManagerService;

    @PostMapping("/account")
    public AccountInfoResponse createAccount(@RequestBody @Valid CreateAccountRequestParam accountRequestParam) {
        return accountManagerService.createAccount(accountRequestParam);
    }

    @GetMapping("/account/{account_number}")
    public AccountInfoResponse getAccount(@PathVariable("account_number") String accountNumber) {
        return accountManagerService.getAccount(accountNumber);
    }

    @PutMapping("/account/{account_number}")
    public AccountInfoResponse updateAccount(
            @PathVariable("account_number") String accountNumber,
            @RequestBody @Valid UpdateAccountRequestParam updatedAccount) {
        return accountManagerService.updateAccount(accountNumber, updatedAccount);
    }

    @DeleteMapping("/account/{account_number}")
    public OperationResponse deleteAccount(@PathVariable("account_number") String accountNumber) {
        return accountManagerService.deleteAccount(accountNumber);
    }

    @GetMapping("/accounts")
    public Page<AccountInfoResponse> listAllAccounts(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return accountManagerService.listAllAccounts(pageable);
    }

    @PostMapping("/account/transfer")
    public OperationResponse transferFunds(@RequestBody @Valid TransferRequestParam transferRequestParam) {
        return accountManagerService.transferFunds(transferRequestParam);
    }
}