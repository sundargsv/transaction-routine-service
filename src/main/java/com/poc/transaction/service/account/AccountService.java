package com.poc.transaction.service.account;

import com.poc.transaction.model.request.AccountRequest;
import com.poc.transaction.model.response.AccountResponse;

public interface AccountService {
    AccountResponse createAccount(AccountRequest request);
    AccountResponse getAccount(Long accountId);
}
