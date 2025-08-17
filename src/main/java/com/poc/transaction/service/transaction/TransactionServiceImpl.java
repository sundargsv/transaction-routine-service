package com.poc.transaction.service.transaction;

import com.poc.transaction.exception.ApiException;
import com.poc.transaction.model.request.TransactionRequest;
import com.poc.transaction.model.response.TransactionResponse;
import com.poc.transaction.repository.AccountRepository;
import com.poc.transaction.repository.TransactionRepository;
import com.poc.transaction.repository.entity.AccountEntity;
import com.poc.transaction.repository.entity.TransactionEntity;
import com.poc.transaction.utils.mappers.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    public TransactionResponse createTransaction(TransactionRequest request) {

        // validate account existence
        AccountEntity account = validateAccount(request.getAccountId());

        // Validate operation type and apply sign rule
        BigDecimal finalAmount = TransactionAmountRule.applySignRule(request.getOperationTypeId(), request.getAmount());

        // Check available balance before debt transactions
        validateBalance(account, finalAmount, request.getOperationTypeId());

        // Create transaction entity
        // Save transaction to the repository and return response with txn id
        TransactionEntity savedEntity = transactionRepository.save(TransactionMapper.toEntity(request, account, finalAmount));

        // Update balance
        account.setAvailableBalance(account.getAvailableBalance().add(finalAmount));
        accountRepository.save(account);

        return TransactionMapper.toResponse(savedEntity);
    }

    private AccountEntity validateAccount(Long accountId) {
        Optional<AccountEntity> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            throw new ApiException("Account not found", HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND");
        }
        return accountOpt.get();
    }

    private void validateBalance(AccountEntity account, BigDecimal finalAmount, int operationTypeId) {
        if (operationTypeId == 1 || operationTypeId == 2 || operationTypeId == 3) { // debt
            BigDecimal newBalance = account.getAvailableBalance().add(finalAmount); // 100.00 + (-50.00) = 50.00
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new ApiException("Insufficient funds. Transaction is failed.",
                        HttpStatus.BAD_REQUEST, "INSUFFICIENT_FUNDS");
            }
        }
        // if op type = 4 (payment), no validation required
    }
}
