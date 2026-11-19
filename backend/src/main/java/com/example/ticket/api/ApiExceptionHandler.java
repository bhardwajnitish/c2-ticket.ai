package com.example.ticket.api;

import com.example.ticket.domain.InvalidStatusTransitionException;
import com.example.ticket.domain.StatusChangeNotAllowedException;
import com.example.ticket.domain.TicketNotFoundException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(basePackages = {"com.example.ticket.api", "com.example.rag.api"})
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorDetail> details =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(this::toDetail)
                        .collect(Collectors.toList());
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed.", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof UnrecognizedPropertyException upe) {
            ErrorDetail detail =
                    new ErrorDetail(upe.getPropertyName(), "Unknown property is not allowed.");
            return error(
                    HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR",
                    "Request validation failed.",
                    List.of(detail));
        }
        return error(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Malformed JSON request.",
                Collections.emptyList());
    }

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TicketNotFoundException ex) {
        return error(
                HttpStatus.NOT_FOUND,
                "TICKET_NOT_FOUND",
                "Ticket not found.",
                Collections.emptyList());
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidStatusTransitionException ex) {
        return error(
                HttpStatus.BAD_REQUEST,
                "INVALID_STATUS_TRANSITION",
                "Status transition is not allowed.",
                Collections.emptyList());
    }

    @ExceptionHandler(StatusChangeNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleStatusOnPatch(StatusChangeNotAllowedException ex) {
        return error(
                HttpStatus.BAD_REQUEST,
                "STATUS_CHANGE_NOT_ALLOWED_ON_THIS_ENDPOINT",
                "Status cannot be changed on this endpoint.",
                Collections.emptyList());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleCustomValidation(ValidationException ex) {
        return error(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                ex.getMessage(),
                Collections.emptyList());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String field = ex.getName() != null ? ex.getName() : "parameter";
        return error(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request validation failed.",
                List.of(new ErrorDetail(field, "Invalid value.")));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<ErrorDetail> details =
                ex.getConstraintViolations().stream()
                        .map(v -> new ErrorDetail(v.getPropertyPath().toString(), v.getMessage()))
                        .collect(Collectors.toList());
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed.", details);
    }

    private ErrorDetail toDetail(FieldError fieldError) {
        return new ErrorDetail(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private ResponseEntity<ErrorResponse> error(
            HttpStatus status, String code, String message, List<ErrorDetail> details) {
        return ResponseEntity.status(status).body(new ErrorResponse(new ErrorBody(code, message, details)));
    }
}