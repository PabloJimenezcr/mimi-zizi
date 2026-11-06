package com.mimiyzizi.grooming.service;

import com.mimiyzizi.grooming.domain.RolUsuario;
import com.mimiyzizi.grooming.domain.Usuario;
import com.mimiyzizi.grooming.dto.RegistroForm;
import com.mimiyzizi.grooming.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean correoRegistrado(String correo) {
        return correo != null && usuarioRepository.existsByCorreoIgnoreCase(correo.trim());
    }

    @Transactional
    public Usuario registrarCliente(RegistroForm formulario) {
        String correo = formulario.getCorreo().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new IllegalArgumentException("Este correo ya tiene una cuenta. Puedes iniciar sesión.");
        }
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(formulario.getNombreCompleto().trim());
        usuario.setTelefono(formulario.getTelefono().trim());
        usuario.setCorreo(correo);
        usuario.setContrasena(passwordEncoder.encode(formulario.getContrasena()));
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }
}
