package com.poc.transaction.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.transaction.exception.ApiException;
import com.poc.transaction.exception.GlobalExceptionHandler;
import com.poc.transaction.model.request.TransactionRequest;
import com.poc.transaction.model.response.TransactionResponse;
import com.poc.transaction.service.transaction.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest
//@AutoConfigureMockMvc
//@WebMvcTest(TransactionApi.class)
@ExtendWith(MockitoExtension.class)
class TransactionApiTests {

//    @Autowired
    private MockMvc mockMvc;

    // @MockBean
    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionApi transactionApi;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionApi)
                // registering manually, since we are not using @SpringBootTest & @ControllerAdvice is not auto-detected in standalone mode
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testCreateTransaction_success() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
                .accountId(1L)
                .operationTypeId(1) // CASH PURCHASE
                .amount(BigDecimal.valueOf(100.00))
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .transactionId(10L)
                .accountId(1L)
                .operationTypeId(1)
                .amount(BigDecimal.valueOf(-100.00)) // enforced negative by rule
                .eventDate(LocalDateTime.now())
                .build();

        when(transactionService.createTransaction(any(TransactionRequest.class)))
                .thenReturn(response);

        // Act & expect
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(10L))
                .andExpect(jsonPath("$.accountId").value(1L))
                .andExpect(jsonPath("$.operationTypeId").value(1))
                .andExpect(jsonPath("$.amount").value(-100.00));
    }

    @Test
    void testCreateTransaction_invalidAmount() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
                .accountId(1L)
                .operationTypeId(1)
                .amount(BigDecimal.ZERO)
                .build();

        // Act & expect
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTransaction_accountNotFound() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
                .accountId(99L)
                .operationTypeId(1)
                .amount(BigDecimal.valueOf(50))
                .build();

        when(transactionService.createTransaction(any(TransactionRequest.class)))
                .thenThrow(new ApiException("Account not found", HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND"));

        // Act & expect
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateTransaction_insufficientFunds() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
                .accountId(1L)
                .operationTypeId(1)
                .amount(BigDecimal.valueOf(500))
                .build();

        when(transactionService.createTransaction(any(TransactionRequest.class)))
                .thenThrow(new ApiException("Insufficient funds. Transaction is failed.",
                        HttpStatus.BAD_REQUEST, "INSUFFICIENT_FUNDS"));

        // Act & expect
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

