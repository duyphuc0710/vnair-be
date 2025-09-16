package com.vnair.common.exception;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    @Builder.Default
    private boolean success = false;
    private String message;
    private String errorCode;
    private Object data;
    private List<FieldError> fieldErrors;
    private String path;
    private LocalDateTime timestamp;
    private String traceId;
    
    @Data
    @Builder
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}