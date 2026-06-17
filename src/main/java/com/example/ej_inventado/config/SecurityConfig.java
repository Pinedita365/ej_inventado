package com.example.ej_inventado.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import com.example.ej_inventado.services.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline'; " +
                    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                    "font-src 'self' https://fonts.gstatic.com; " +
                    "img-src 'self' data: blob: https://res.cloudinary.com; " +
                    "connect-src 'self' https://nominatim.openstreetmap.org;"
                ))
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            )
            .sessionManagement(session -> session
                .sessionFixation(fixation -> fixation.migrateSession())
            )
            .authorizeHttpRequests(auth -> auth
                // Admin exclusivo
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Empresa o Admin
                .requestMatchers("/empresa/**").hasAnyRole("EMPRESA", "ADMIN")
                // Páginas públicas (no requieren login)
                .requestMatchers(
                    "/", "/login", "/registrar", "/css/**", "/static/**", "/js/**",
                    "/img/**", "/images/**",
                    "/invitado", "/explorar",
                    "/sobre-nosotros", "/cookies", "/terminos", "/privacidad", "/clientes",
                    "/recuperar-password", "/reset-password",
                    "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html"
                ).permitAll()
                // Requieren sesión: detalle de actividad, planificación y perfil
                .requestMatchers(
                    "/verMas", "/configurar-viaje", "/comprobar",
                    "/inicio", "/eliminarCarrito",
                    "/planificar", "/confirmarCarrito", "/final",
                    "/enviar-itinerario", "/perfil", "/perfil/**",
                    "/resenas/crear"
                ).authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService))
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
