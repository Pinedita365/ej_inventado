package com.example.ej_inventado.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TwoFactorInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("2fa_pending"))) {
            return true;
        }

        String uri = request.getRequestURI();

        // Permitir la propia página de verificación, reenvío, recursos estáticos y OAuth2
        if (uri.startsWith("/verificar-2fa")
                || uri.startsWith("/reenviar-2fa")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/img/")
                || uri.startsWith("/oauth2/")
                || uri.startsWith("/login/oauth2/")) {
            return true;
        }

        response.sendRedirect(request.getContextPath() + "/verificar-2fa");
        return false;
    }
}
