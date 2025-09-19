package com.muiyurocodes.ecommerc.advice;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private LocalDateTime timeStamp;
    private String error;
    private HttpStatus statusCode;
    private Map<String, String> validationErrors;

    public ApiError() {
        this.timeStamp = LocalDateTime.now();
    }

    public ApiError(String error, HttpStatus statusCode) {
        this();
        this.error = error;
        this.statusCode = statusCode;
    }

    public ApiError(String error, HttpStatus statusCode, Map<String, String> validationErrors) {
        this(error, statusCode);
        this.validationErrors = validationErrors;
    }
}
