package com.mimiyzizi.grooming.controllers;

import com.mimiyzizi.grooming.dto.RegistroForm;
import com.mimiyzizi.grooming.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @InitBinder("registroForm")
    public void configurarRegistro(WebDataBinder binder) {
        binder.setAllowedFields(
                "nombreCompleto",
                "telefono",
                "correo",
                "contrasena",
                "confirmarContrasena",
                "aceptaTerminos");
    }

    @PostMapping("/registro")
    public Object registrar(
            @Valid @ModelAttribute("registroForm") RegistroForm formulario,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes,
            @RequestHeader(name = "X-Requested-With", required = false) String requestedWith) {

        boolean solicitudAjax = "XMLHttpRequest".equalsIgnoreCase(requestedWith);

        if (formulario.getConfirmarContrasena() != null
                && !formulario.getConfirmarContrasena().isBlank()
                && !Objects.equals(formulario.getContrasena(), formulario.getConfirmarContrasena())) {

            resultado.rejectValue(
                    "confirmarContrasena",
                    "registro.confirmacion",
                    "Las contraseñas no coinciden.");
        }

        if (formulario.getCorreo() != null
                && usuarioService.correoRegistrado(formulario.getCorreo())) {

            resultado.rejectValue(
                    "correo",
                    "registro.correo.duplicado",
                    "Este correo ya está registrado. Inicia sesión o utiliza otro correo.");
        }

        if (resultado.hasErrors()) {

            if (solicitudAjax) {
                return respuestaAjaxConErrores(
                        resultado,
                        "Revisa los datos señalados para completar tu registro.");
            }

            model.addAttribute("authView", "register");
            model.addAttribute("authNotice", "");
            model.addAttribute(
                    "authRegisterNotice",
                    "Revisa los datos señalados para completar tu registro.");
            model.addAttribute("loginError", false);

            return "index";
        }

        try {
            usuarioService.registrarCliente(formulario);

        } catch (IllegalArgumentException ex) {

            resultado.rejectValue(
                    "correo",
                    "registro.correo.duplicado",
                    ex.getMessage());

            if (solicitudAjax) {
                return respuestaAjaxConErrores(
                        resultado,
                        "No pudimos crear tu cuenta todavía.");
            }

            model.addAttribute("authView", "register");
            model.addAttribute("authNotice", "");
            model.addAttribute(
                    "authRegisterNotice",
                    "No pudimos crear tu cuenta todavía.");
            model.addAttribute("loginError", false);

            return "index";
        }

        String mensaje = "¡Tu cuenta Mimi y Zizi está lista! Ahora puedes iniciar sesión.";

        if (solicitudAjax) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", mensaje));
        }

        redirectAttributes.addFlashAttribute("authView", "login");
        redirectAttributes.addFlashAttribute("authNotice", mensaje);
        redirectAttributes.addFlashAttribute("authRegisterNotice", "");

        return "redirect:/";
    }

    private ResponseEntity<Map<String, Object>> respuestaAjaxConErrores(
            BindingResult resultado,
            String mensaje) {

        Map<String, String> errores = new LinkedHashMap<>();

        resultado.getFieldErrors().forEach(error -> errores.putIfAbsent(
                error.getField(),
                error.getDefaultMessage()));

        Map<String, Object> respuesta = new LinkedHashMap<>();

        respuesta.put("success", false);
        respuesta.put("message", mensaje);
        respuesta.put("errors", errores);

        return ResponseEntity.unprocessableEntity().body(respuesta);
    }
}