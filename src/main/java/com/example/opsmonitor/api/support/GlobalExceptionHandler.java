package com.example.opsmonitor.api.support;

import com.example.opsmonitor.application.support.BadRequestException;
import com.example.opsmonitor.application.support.ConflictException;
import com.example.opsmonitor.application.support.NotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
    List<FieldValidationError> errors =
        exception.getBindingResult().getFieldErrors().stream().map(this::fromFieldError).toList();
    return ResponseEntity.badRequest()
        .body(ErrorResponse.of("VALIDATION_ERROR", "Request validation failed", errors));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      ConstraintViolationException exception) {
    List<FieldValidationError> errors =
        exception.getConstraintViolations().stream().map(this::fromConstraint).toList();
    return ResponseEntity.badRequest()
        .body(ErrorResponse.of("VALIDATION_ERROR", "Request validation failed", errors));
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.of("NOT_FOUND", exception.getMessage(), List.of()));
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponse> handleConflict(ConflictException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponse.of("CONFLICT", exception.getMessage(), List.of()));
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException exception) {
    return ResponseEntity.badRequest()
        .body(ErrorResponse.of("BAD_REQUEST", exception.getMessage(), List.of()));
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class
  })
  public ResponseEntity<ErrorResponse> handleRequestParsingErrors(Exception exception) {
    return ResponseEntity.badRequest()
        .body(ErrorResponse.of("BAD_REQUEST", "Request payload could not be parsed", List.of()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of("INTERNAL_ERROR", "Unexpected server error", List.of()));
  }

  private FieldValidationError fromFieldError(FieldError fieldError) {
    return new FieldValidationError(fieldError.getField(), fieldError.getDefaultMessage());
  }

  private FieldValidationError fromConstraint(ConstraintViolation<?> violation) {
    return new FieldValidationError(violation.getPropertyPath().toString(), violation.getMessage());
  }
}
