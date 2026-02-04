package com.example.ej_inventado.config; // Ajusta el paquete si es necesario

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.ej_inventado.services.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
        @Autowired
        private CustomOAuth2UserService customOAuth2UserService;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/", "/registrar", "/login", "/css/**", "/static/**",
                                                                "/js/**", "/invitado", "/images/**", "/img/**",
                                                                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/configurar-viaje", true)
                                                .permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/")
                                                .defaultSuccessUrl("/configurar-viaje", true)
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService)))
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/?logout=true")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll());

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}