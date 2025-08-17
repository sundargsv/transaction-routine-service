package com.poc.transaction.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.transaction.exception.ApiException;
import com.poc.transaction.exception.GlobalExceptionHandler;
import com.poc.transaction.model.request.AccountRequest;
import com.poc.transaction.model.response.AccountResponse;
import com.poc.transaction.service.account.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AccountApiTests {

    private MockMvc mockMvc;

    // We mock the service layer so no DB/cache calls are made
    // @MockBean // MockBean is depcrecated in spring boot 3.4.x+, need to change this integration tests with full TestContainers for db, cache, etc.
    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountApi accountApi;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountApi)
                // registering manually, since we are not using @SpringBootTest & @ControllerAdvice is not auto-detected in standalone mode
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

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
