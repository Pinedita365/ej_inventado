package com.example.ej_inventado.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TwoFactorInterceptor twoFactorInterceptor;

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver slr = new CookieLocaleResolver();
        slr.setDefaultLocale(new Locale("es")); // Fuerza español por defecto
        slr.setCookieName("language");
        return slr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang"); // El botón debe enviar ?lang=es o ?lang=en
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
        registry.addInterceptor(twoFactorInterceptor)
                .excludePathPatterns("/css/**", "/js/**", "/img/**", "/static/**",
                                     "/login", "/logout", "/registrar",
                                     "/oauth2/**", "/login/oauth2/**",
                                     "/recuperar-password", "/reset-password",
                                     "/verificar-2fa", "/verificar-2fa/**");
    }
}