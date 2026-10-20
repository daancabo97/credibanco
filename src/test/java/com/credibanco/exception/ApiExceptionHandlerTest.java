package com.credibanco.exception;

import com.credibanco.controller.AuthController;
import com.credibanco.dto.ErrorResponse;
import com.credibanco.dto.LoginRequest;
import com.credibanco.models.EstadoTransaccion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private ApiExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApiExceptionHandler();
    }

    @Test
    void negocioSinEstado_responde400ConTipoNegocio() {
        ResponseEntity<ErrorResponse> response = handler.handleBusiness(new BusinessException("fallo de regla"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("fallo de regla", response.getBody().getMensaje());
        assertEquals("Negocio", response.getBody().getTipoError());
    }

    @Test
    void negocioConEstado_usaElNombreDelEstado() {
        ResponseEntity<ErrorResponse> response = handler.handleBusiness(
                new BusinessException("Tarjeta vencida", EstadoTransaccion.RECHAZADA));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Tarjeta vencida", response.getBody().getMensaje());
        assertEquals("RECHAZADA", response.getBody().getTipoError());
    }

    @Test
    void validacion_uneMensajesYDescartaVacios() throws Exception {
        ResponseEntity<ErrorResponse> response = validacion(
                field("username", "El usuario es obligatorio"),
                field("password", "La contraseña es obligatoria"),
                field("extra", "   "),
                field("otro", null));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El usuario es obligatorio. La contraseña es obligatoria", response.getBody().getMensaje());
        assertEquals("Validacion", response.getBody().getTipoError());
    }

    @Test
    void validacion_sinMensajesUtiles_usaTextoGenerico() throws Exception {
        ResponseEntity<ErrorResponse> response = validacion();

        assertEquals("Datos inválidos", response.getBody().getMensaje());
        assertEquals("Validacion", response.getBody().getTipoError());
    }

    @Test
    void credencialesInvalidas_responde401() {
        ResponseEntity<ErrorResponse> response = handler.handleCredenciales(new BadCredentialsException("bad"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Credenciales inválidas", response.getBody().getMensaje());
        assertEquals("Autenticacion", response.getBody().getTipoError());
    }

    private ResponseEntity<ErrorResponse> validacion(FieldError... errores) throws Exception {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new LoginRequest(), "request");
        for (FieldError error : errores) {
            result.addError(error);
        }
        MethodParameter parameter = new MethodParameter(
                AuthController.class.getDeclaredMethod("login", LoginRequest.class), 0);
        return handler.handleValidacion(new MethodArgumentNotValidException(parameter, result));
    }

    private FieldError field(String campo, String mensaje) {
        return new FieldError("request", campo, mensaje);
    }
}
