package com.poc.transaction.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.transaction.exception.ApiException;
import com.poc.transaction.model.request.AccountRequest;
import com.poc.transaction.model.response.AccountResponse;
import com.poc.transaction.service.account.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountApiTests {

    @Autowired
    private MockMvc mockMvc;

    // We mock the service layer so no DB/cache calls are made
    @MockBean // MockBean is depcrecated in spring boot 3.4.x+, need to change this integration tests with full TestContainers for db, cache, etc.
    private AccountService accountService;

    @Test
    void testCreateAccount_success() throws Exception {
        AccountRequest request = new AccountRequest();
        request.setDocumentNumber("12345678900");

        AccountResponse response = new AccountResponse();
        response.setAccountId(1L);
        response.setDocumentNumber("12345678900");

        // when service is called, return mocked response
        when(accountService.createAccount(any(AccountRequest.class)))
                .thenReturn(response);

        // Act & expect
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(1L))
                .andExpect(jsonPath("$.documentNumber").value("12345678900"));
    }

    @Test
    void testGetAccountById_success() throws Exception {
        AccountResponse response = new AccountResponse();
        response.setAccountId(1L);
        response.setDocumentNumber("12345678900");

        when(accountService.getAccount(1L)).thenReturn(response);

        // Act & expect
        mockMvc.perform(get("/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(1L))
                .andExpect(jsonPath("$.documentNumber").value("12345678900"));
    }

    @Test
    void testGetAccountById_notFound() throws Exception {
        when(accountService.getAccount(99L))
                .thenThrow(new ApiException("Account not found", HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND"));

        // Act & expect
        mockMvc.perform(get("/accounts/99"))
                .andExpect(status().is4xxClientError()); // or 404 if you mapped exceptions
    }
}
