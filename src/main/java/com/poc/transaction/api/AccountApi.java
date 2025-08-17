package com.poc.transaction.api;

import com.poc.transaction.model.request.AccountRequest;
import com.poc.transaction.model.response.AccountResponse;
import com.poc.transaction.service.account.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Account API", description = "Operations related to accounts")
public class AccountApi {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create a new account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request/ validation error"),
            @ApiResponse(responseCode = "409", description = "Account already exists"),
    })
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request) {
        log.info("Received request to create account with documentNumber={}", request.getDocumentNumber());
        return ResponseEntity.status(201).body(accountService.createAccount(request));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Account not found"),
    })
    public ResponseEntity<AccountResponse> getAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getAccount(accountId));
    }
}
