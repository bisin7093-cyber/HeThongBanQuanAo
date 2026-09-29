package com.project_shopping.shopee.controller;

import com.project_shopping.shopee.dto.ApiDtos.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {
 @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<ErrorResponse> status(ResponseStatusException ex,HttpServletRequest request) {
  HttpStatus status=HttpStatus.valueOf(ex.getStatusCode().value()); return body(status,ex.getReason()==null?status.getReasonPhrase():ex.getReason());
 }
 @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex) {
  String message=ex.getBindingResult().getFieldErrors().stream().map(e->e.getField()+" "+e.getDefaultMessage()).findFirst().orElse("Request validation failed"); return body(HttpStatus.BAD_REQUEST,message);
 }
 @ExceptionHandler(HttpMessageNotReadableException.class) public ResponseEntity<ErrorResponse> unreadable() { return body(HttpStatus.BAD_REQUEST,"Malformed request or invalid enum value"); }
 @ExceptionHandler(DataIntegrityViolationException.class) public ResponseEntity<ErrorResponse> conflict() { return body(HttpStatus.CONFLICT,"Request conflicts with an existing record or database constraint"); }
 private ResponseEntity<ErrorResponse> body(HttpStatus status,String message) { return ResponseEntity.status(status).body(new ErrorResponse(Instant.now(),status.value(),status.getReasonPhrase(),message)); }
}
