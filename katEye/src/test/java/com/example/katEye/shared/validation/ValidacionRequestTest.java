package com.example.katEye.shared.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.katEye.modulos.contenido.mensajecontacto.dto.MensajeContactoRequest;
import com.example.katEye.modulos.usuarios.auth.dto.LoginRequest;
import com.example.katEye.modulos.usuarios.usuario.dto.RegistroRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ValidacionRequestTest {

    private static Validator validator;

    @BeforeAll
    static void configurar() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static boolean valido(Object request) {
        return validator.validate(request).isEmpty();
    }

    @Test
    void registroAceptaDominioPermitidoYPasswordDeCuatroADiez() {
        assertTrue(valido(new RegistroRequest(
                "Ana Pérez",
                "ana@gmail.com",
                "1234",
                null,
                "13",
                "13101")));
    }

    @Test
    void registroRechazaDominioNoPermitido() {
        assertFalse(valido(new RegistroRequest(
                "Ana Pérez",
                "ana@hotmail.com",
                "1234",
                null,
                "13",
                "13101")));
    }

    @Test
    void registroRechazaPasswordMayorADiez() {
        assertFalse(valido(new RegistroRequest(
                "Ana Pérez",
                "ana@gmail.com",
                "12345678901",
                null,
                "13",
                "13101")));
    }

    @Test
    void loginAceptaDominioPermitido() {
        assertTrue(valido(new LoginRequest("ana@duoc.cl", "1234")));
    }

    @Test
    void loginRechazaDominioNoPermitido() {
        assertFalse(valido(new LoginRequest("ana@hotmail.com", "1234")));
    }

    @Test
    void contactoAceptaCorreoOpcionalVacioONulo() {
        assertTrue(valido(new MensajeContactoRequest("Ana", "", "Hola")));
        assertTrue(valido(new MensajeContactoRequest("Ana", null, "Hola")));
    }

    @Test
    void contactoRechazaCorreoNoPermitido() {
        assertFalse(valido(new MensajeContactoRequest("Ana", "ana@hotmail.com", "Hola")));
    }
}
