package com.moodify.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error; // Назва стаусів
    private String message; // Повідомлення з винятку
    private String path; // Шлях запиту, де виникла помилка

    public ErrorResponse(HttpStatus httpStatus, String message, String path) {
        this.timestamp = LocalDateTime.now(); // Поточний час помилки
        this.status = httpStatus.value(); // Числовий код статусу (404, 403, 400, ...)
        this.error = httpStatus.getReasonPhrase(); // Текстове подання статусу ("Not Found", "Forbidden", ...)
        this.message = message; // Повідомлення про помилку
        this.path = path; // URI запиту
    }
}