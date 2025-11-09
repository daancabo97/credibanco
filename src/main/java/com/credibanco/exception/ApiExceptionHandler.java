package com.credibanco.exception;

import com.credibanco.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setMensaje(ex.getMessage());
        error.setTipoError("Negocio");
        return ResponseEntity.badRequest().body(error);
    }
}

