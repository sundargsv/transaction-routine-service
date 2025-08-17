package com.poc.transaction.service.transaction;

import com.poc.transaction.model.request.TransactionRequest;
import com.poc.transaction.model.response.TransactionResponse;

public interface TransactionService {
    /**
     * Creates a transaction for the given account and operation type.
     *
     * @param request the request of the TransactionRequest payload
     * @return a response containing details of the created transaction
     */
    TransactionResponse createTransaction(TransactionRequest request);
}
