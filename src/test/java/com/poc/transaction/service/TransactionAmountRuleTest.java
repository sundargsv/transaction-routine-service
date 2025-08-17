package com.poc.transaction.service;

import com.poc.transaction.exception.ApiException;
import com.poc.transaction.service.transaction.TransactionAmountRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionAmountRuleTest {

    @Test
    void applySignRule_shouldNegateAmount_forCashPurchase() {
        BigDecimal result = TransactionAmountRule.applySignRule(1, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("-100.00"), result);
    }

    @Test
    void applySignRule_shouldNegateAmount_forInstallmentPurchase() {
        BigDecimal result = TransactionAmountRule.applySignRule(2, new BigDecimal("200.00"));
        assertEquals(new BigDecimal("-200.00"), result);
    }

    @Test
    void applySignRule_shouldNegateAmount_forWithdrawal() {
        BigDecimal result = TransactionAmountRule.applySignRule(3, new BigDecimal("300.00"));
        assertEquals(new BigDecimal("-300.00"), result);
    }

    @Test
    void applySignRule_shouldKeepPositive_forPayment() {
        BigDecimal result = TransactionAmountRule.applySignRule(4, new BigDecimal("400.00"));
        assertEquals(new BigDecimal("400.00"), result);
    }

    @Test
    void applySignRule_shouldThrowApiException_forInvalidOperationType() {
        ApiException exception = assertThrows(ApiException.class,
                () -> TransactionAmountRule.applySignRule(99, new BigDecimal("500.00")));

        assertEquals("Provided operation type id is invalid.", exception.getMessage());
        assertEquals("INVALID_OPERATION_TYPE", exception.getCode());
    }

    @Test
    void getDescription_shouldReturnCorrectDescription() {
        assertEquals("CASH PURCHASE", TransactionAmountRule.getDescription(1));
        assertEquals("INSTALLMENT PURCHASE", TransactionAmountRule.getDescription(2));
        assertEquals("WITHDRAWAL", TransactionAmountRule.getDescription(3));
        assertEquals("PAYMENT", TransactionAmountRule.getDescription(4));
    }

    @Test
    void getDescription_shouldReturnNull_forUnknownOperationType() {
        assertNull(TransactionAmountRule.getDescription(999));
    }
}

