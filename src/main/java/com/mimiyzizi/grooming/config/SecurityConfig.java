package com.mimiyzizi.grooming.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/registro",
                    "/assets/**",
                    "/webjars/**",
                    "/favicon.ico",
                    "/error"
                ).permitAll()

                .requestMatchers("/admin/**").hasRole("ADMIN")

                .requestMatchers("/cliente/**")
                .hasAnyRole("CLIENTE", "ADMIN")

                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/")
                .loginProcessingUrl("/login")
                .usernameParameter("correo")
                .passwordParameter("contrasena")

                .successHandler((request, response, authentication) -> {

                    boolean administrador =
                        authentication.getAuthorities().stream()
                            .anyMatch(authority ->
                                "ROLE_ADMIN".equals(
                                    authority.getAuthority()
                                )
                            );

                    String destino = administrador
                        ? "/admin/inicio"
                        : "/cliente/inicio";

                    response.sendRedirect(
                        request.getContextPath() + destino
                    );
                })

                .failureHandler((request, response, exception) ->
                    response.sendRedirect(
                        request.getContextPath() + "/?loginError=1"
                    )
                )

                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}