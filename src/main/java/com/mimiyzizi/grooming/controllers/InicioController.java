package com.mimiyzizi.grooming.controllers;

import com.mimiyzizi.grooming.dto.RegistroForm;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class InicioController {

    @GetMapping("/")
    public String mostrarInicio(
            @RequestParam(name = "loginError", required = false) String loginError,
            @RequestParam(name = "logout", required = false) String logout,
            @RequestParam(required = false) String authView,
            @RequestParam(required = false) String authNotice,
            @RequestAttribute("_csrf") CsrfToken csrfToken,
            Model model) {

        // Fuerza la carga del token CSRF antes de renderizar la vista.
        csrfToken.getToken();

        if (!model.containsAttribute("registroForm")) {
            model.addAttribute("registroForm", new RegistroForm());
        }

        if (!model.containsAttribute("authView")) {
            model.addAttribute("authView", authView != null ? authView : "");
        }

        if (!model.containsAttribute("authNotice")) {
            model.addAttribute(
                    "authNotice",
                    authNotice != null
                            ? authNotice
                            : logout != null
                                    ? "Has cerrado sesión correctamente."
                                    : "");
        }

        if (!model.containsAttribute("authRegisterNotice")) {
            model.addAttribute("authRegisterNotice", "");
        }

        model.addAttribute("loginError", loginError != null);

        return "index";
    }
}