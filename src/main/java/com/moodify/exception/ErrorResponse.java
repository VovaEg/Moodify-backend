package com.moodify.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error; // Status name
    private String message; // Message from the exception
    private String path; // The query path where the error occured

    public ErrorResponse(HttpStatus httpStatus, String message, String path) {
        this.timestamp = LocalDateTime.now(); // Current error time
        this.status = httpStatus.value(); // Numerical status code (404, 403, 400, ...)
        this.error = httpStatus.getReasonPhrase(); // Text display of status ("Not Found", "Forbidden", ...)
        this.message = message; // Error message
        this.path = path; // Request URI
    }
}