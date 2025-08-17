package com.poc.transaction.service.transaction;

import com.poc.transaction.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

public class TransactionAmountRule {

    // this could be in a separate config table and pulled once from when app starts - just made it as enum for simplicity
    private static final Map<Integer, String> OPERATION_TYPES = new HashMap<>();

    static {
        OPERATION_TYPES.put(1, "CASH PURCHASE");
        OPERATION_TYPES.put(2, "INSTALLMENT PURCHASE");
        OPERATION_TYPES.put(3, "WITHDRAWAL");
        OPERATION_TYPES.put(4, "PAYMENT");
    }

    private TransactionAmountRule() {
        // utility class, prevent instantiation
    }

    public static BigDecimal applySignRule(int operationTypeId, BigDecimal amount) {
        validateOperationType(operationTypeId);

        switch (operationTypeId) {
            case 1: // CASH PURCHASE
            case 2: // INSTALLMENT PURCHASE
            case 3: // WITHDRAWAL
                return amount.negate(); // enforce negative
            case 4: // PAYMENT
                return amount; // keep positive
            default:
                throw new IllegalArgumentException("Unsupported operation type: " + operationTypeId);
        }
    }

    private static void validateOperationType(int operationTypeId) {
        if (!OPERATION_TYPES.containsKey(operationTypeId)) {
            throw new ApiException("Provided operation type id is invalid.", HttpStatus.BAD_REQUEST, "INVALID_OPERATION_TYPE");
        }
    }

    // this may not be required
    public static String getDescription(int operationTypeId) {
        return OPERATION_TYPES.get(operationTypeId);
    }
}
