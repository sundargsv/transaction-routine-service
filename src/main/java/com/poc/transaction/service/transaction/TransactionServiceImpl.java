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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
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

        // prepare txn entity
        // this new variable can be avoided by having the updateBalance func within the TransactionMapper itself
        TransactionEntity entity = TransactionMapper.toEntity(request, account, finalAmount);

        // set balance differently for purchases vs payments
        entity.setBalance(updateBalance(request));

        // save transaction to the repository and return response with txn id
        // for payment type operation - saving the transaction first, then process discharge makes strong consistency
        // saving early is safer (so the txn exists even if discharge fails mid-way).
        TransactionEntity savedEntity = transactionRepository.save(entity);

        // discharge only if operation type is payment
        if (request.getOperationTypeId() == 4){
            // this could be async process using event-driven, so events can be sourced to payment-discharge, audit-log, notify but for made it sync for simplicity
            // setting balance again for tests, but balance data will be ignored in API response.
            entity.setBalance(discharge(savedEntity));
        }

        // Update balance in account -> Removing this as we don't need it for now.
        /*
        account.setAvailableBalance(account.getAvailableBalance().add(finalAmount));
        accountRepository.save(account);
        */

        return TransactionMapper.toResponse(savedEntity);
    }

    /**
     * Discharge logic:
     * - Use the positive payment amount to offset negative purchase balances.
     * - Purchases are iterated in order, and each is partially/fully balanced.
     * - Remaining payment amount is updated accordingly.
     */
    private BigDecimal discharge(TransactionEntity paymentTransaction) {
        // Start with the full payment amount (always positive for payments)
        BigDecimal amountToBalance = paymentTransaction.getAmount();

        // Fetch all purchase transactions for this account that might need balancing
        List<TransactionEntity> purchases = transactionRepository.fetchTransactions(
                paymentTransaction.getAccount().getAccountId(),
                LocalDateTime.now()
        );

        // If no purchases exist, just set balance on payment and save
        if (purchases.isEmpty()) {
            // audit-log it for reports (no need to console log it everytime)
            log.info("No previous purchases found for accountId={}, saving payment txn with balance={}", paymentTransaction.getAccount().getAccountId(), amountToBalance);
            return amountToBalance;
        }

        // List of transactions (purchases + payment) that will be updated and saved
        List<TransactionEntity> updatedPurchases = new ArrayList<>();

        // Iterate through each purchase transaction and try to offset its negative balance
        for (TransactionEntity purchase : purchases) {
            // Check if this purchase is eligible:
            // (1) purchase has negative balance (unpaid), and
            // (2) payment still has some remaining amount
            if (isEligibleForDischarge(purchase, amountToBalance)) {

                // apply discharge logic -> find offset amount to discharge & update purchase balance by applying the offset
                // returns [offsetAmount, remainingPaymentBalance]
                BigDecimal[] results = applyDischarge(purchase, amountToBalance);

                // Update remaining payment after discharge
                amountToBalance = results[1];

                // audit log it to db/ elastic cache for reporting
                // audit-log it for reporting (no need to console log it everytime)
                log.info("Discharge: purchaseId={}, newBalance={}, remainingPayment={}",
                        purchase.getTransactionId(), purchase.getBalance(), amountToBalance);

                // Add updated purchase to the list of entities to persist later
                updatedPurchases.add(purchase);
            }
        }

        // After applying to all purchases, set the leftover balance on the payment itself
        paymentTransaction.setBalance(amountToBalance);

        // Add payment transaction itself to the update list
        updatedPurchases.add(paymentTransaction);
        updatedPurchases.sort(Comparator.comparing(TransactionEntity::getEventDate));

        // Persist all changes (both purchases and payment)
        transactionRepository.saveAll(updatedPurchases);

        return paymentTransaction.getBalance();

    }

    /**
     * Check if a purchase is eligible for discharge.
     * i.e., purchase has negative balance AND payment still has funds.
     */
    private boolean isEligibleForDischarge(TransactionEntity purchase, BigDecimal amountToBalance) {
        return purchase.getBalance().compareTo(BigDecimal.ZERO) < 0
                && amountToBalance.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Apply discharge: offset purchase balance with available payment amount.
     * Returns array: [offsetAmount, remainingPaymentBalance]
     */
    private BigDecimal[] applyDischarge(TransactionEntity purchase, BigDecimal amountToBalance) {
        BigDecimal offset = purchase.getBalance().abs().min(amountToBalance);

        // Update purchase balance by applying the offset
        purchase.setBalance(purchase.getBalance().add(offset));

        amountToBalance = amountToBalance.subtract(offset);

        return new BigDecimal[]{offset, amountToBalance};
    }


    private BigDecimal updateBalance(TransactionRequest request) {
        return switch (request.getOperationTypeId()) { // CASH PURCHASE
            // INSTALLMENT PURCHASE
            case 1, 2, 3 -> // WITHDRAWAL
                    request.getAmount().negate(); // enforce negative
            case 4 -> // PAYMENT
                // To process discharge when payment happens
                    request.getAmount(); // keep positive
            default ->
                    throw new IllegalArgumentException("Unsupported operation type: " + request.getOperationTypeId());
        };
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
