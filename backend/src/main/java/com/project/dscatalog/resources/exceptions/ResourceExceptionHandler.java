package com.project.dscatalog.resources.exceptions;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.project.dscatalog.services.exceptions.DatabaseException;
import com.project.dscatalog.services.exceptions.EmailException;
import com.project.dscatalog.services.exceptions.ResourceEntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

/**
 * Traduz exceções de negócio e validação para respostas HTTP padronizadas.
 */
@ControllerAdvice
public class ResourceExceptionHandler {

    /**
     * Retorna erro 404 quando um recurso não e encontrado.
     */
    @ExceptionHandler(ResourceEntityNotFoundException.class)
    public ResponseEntity<StandardError> entityNotFound(ResourceEntityNotFoundException e, HttpServletRequest request) {
        StandardError error = new StandardError();
        error.setTimestamp(Instant.now());
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setError("Resource not found!");
        error.setMessage(e.getMessage());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Retorna erro de banco quando ocorre violação de integridade ou regra relacional.
     */
    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<StandardError> databaseException(DatabaseException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError error = new StandardError();
        error.setTimestamp(Instant.now());
        error.setStatus(status.value());
        error.setError("Database error!");
        error.setMessage(e.getMessage());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }

    /**
     * Retorna erro 422 com os campos inválidos da requisição.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> validation(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        ValidationError error = new ValidationError();
        error.setTimestamp(Instant.now());
        error.setStatus(status.value());
        error.setError("Validation exception!");
        error.setMessage(e.getMessage());
        error.setPath(request.getRequestURI());

        for (FieldError f : e.getBindingResult().getFieldErrors()) {
            error.addError(f.getField(), f.getDefaultMessage());
        }

        return ResponseEntity.status(status).body(error);
    }

    /**
     * Retorna erro para falhas durante envio de email.
     */
    @ExceptionHandler(EmailException.class)
    public ResponseEntity<StandardError> email(EmailException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError error = new StandardError();
        error.setTimestamp(Instant.now());
        error.setStatus(status.value());
        error.setError("Email exception!");
        error.setMessage(e.getMessage());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }

    /**
     * Retorna erro 400 quando ocorre uma exceção do serviço da AWS.
     */
    @ExceptionHandler(AmazonServiceException.class)
    public ResponseEntity<StandardError> amazonService(AmazonServiceException e, HttpServletRequest request) {
        StandardError error = new StandardError();
        error.setTimestamp(Instant.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError("AWS Amazon Service exception!");
        error.setMessage(e.getMessage());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Retorna erro 400 quando ocorre uma exceção do cliente da AWS.
     */
    @ExceptionHandler(AmazonClientException.class)
    public ResponseEntity<StandardError> amazonClient(AmazonClientException e, HttpServletRequest request) {
        StandardError error = new StandardError();
        error.setTimestamp(Instant.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError("AWS Amazon Client exception!");
        error.setMessage(e.getMessage());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Retorna erro 400 quando ocorre uma exceção de argumento ilegal.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StandardError> illegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        StandardError error = new StandardError();
        error.setTimestamp(Instant.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError("Illegal Argument exception!");
        error.setMessage(e.getMessage());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
