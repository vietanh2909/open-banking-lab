package com.navi.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(404).body(Map.of(
                "error", "NOT_FOUND",
                "message", e.getMessage()
        ));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<?> handleConflict(ConflictException e) {
        return ResponseEntity.status(409).body(Map.of(
                "error", "CONFLICT",
                "message", e.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception e) {
        return ResponseEntity.status(500).body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", e.getMessage()
        ));
    }

    @ExceptionHandler(ConsentVerifyException.class)
    public ResponseEntity<Map<String, Object>> handleConsentVerify(ConsentVerifyException ex) {
        int http = switch (ex.getCode()) {
            case "INVALID_TOKEN", "TOKEN_EXPIRED" -> 401;
            default -> 403;
        };

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", ex.getCode());
        body.put("error_description", ex.getMessage());
        return ResponseEntity.status(http).body(body);
    }
}
