package com.credibanco.exception;

import com.credibanco.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@ControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setMensaje(ex.getMessage());
        error.setTipoError(ex.getEstado() != null ? ex.getEstado().name() : "Negocio");
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .filter(message -> message != null && !message.isBlank())
                .collect(Collectors.joining(". "));
        ErrorResponse error = new ErrorResponse();
        error.setMensaje(mensaje.isBlank() ? "Datos inválidos" : mensaje);
        error.setTipoError("Validacion");
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleCredenciales(BadCredentialsException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setMensaje("Credenciales inválidas");
        error.setTipoError("Autenticacion");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}

