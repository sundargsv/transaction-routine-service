package com.poc.transaction.exception;

import com.poc.transaction.model.response.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Handles validation errors (@Valid failures)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errorMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return new ResponseEntity<>(ApiErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .httpStatus(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failures")
                .errors(errorMessages)
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = ApiException.class)
    public ResponseEntity<ApiErrorResponse> exception(ApiException exception) {
        log.error("Deducted API exception/ Error with code={}, message={}", exception.getCode(), exception.getMessage());
        return new ResponseEntity<>(ApiErrorResponse.builder()
                .code(exception.getHttpStatus().value())
                .httpStatus(exception.getHttpStatus().getReasonPhrase())
                .message(exception.getMessage())
                .build(), exception.getHttpStatus());
//        return new ResponseEntity<>(new HashMap<String, String>() {{
//            put("message", exception.getMessage());
//            put("httpStatus", exception.getHttpStatus().getReasonPhrase());
//            put("code", exception.getResponseCode());
//        }}, exception.getHttpStatus());
    }

     @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception exception) {
        log.error("Deducted Un-expected exception/ Error with message={}", exception.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("message", exception.getMessage());
        error.put("status", 500);
        error.put("code", "INTERNAL_ERROR");
        return ResponseEntity.status(500).body(error);
    }
}
