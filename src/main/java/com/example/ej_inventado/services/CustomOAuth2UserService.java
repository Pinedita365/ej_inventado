package com.example.ej_inventado.services;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.ej_inventado.clases.Usuario;
import com.example.ej_inventado.repositories.UsuarioRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Cargamos el usuario básico de Google/GitHub
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String proveedor = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        String email = oAuth2User.getAttribute("email");
        String nombre = oAuth2User.getAttribute("name");
        
        if (email == null) {
            email = oAuth2User.getAttribute("login") + "@github.com";
        }

        // 2. Buscamos en nuestra base de datos
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        Usuario usuarioReal;

        if (usuarioOpt.isEmpty()) {
            // Si no existe, lo creamos como ROLE_USER
            usuarioReal = new Usuario();
            usuarioReal.setEmail(email);
            usuarioReal.setNombre(nombre != null ? nombre : email);
            usuarioReal.setProveedor(proveedor);
            usuarioReal.setRol("ROLE_USER"); 
            usuarioRepository.save(usuarioReal);
            System.out.println("✅ Nuevo usuario registrado desde " + proveedor + ": " + email);
        } else {
            // Si existe, lo recuperamos para sacar su ROL (ROLE_ADMIN o ROLE_USER)
            usuarioReal = usuarioOpt.get();
        }

        // 3. ¡ESTA ES LA PARTE CLAVE! 
        // Creamos la autoridad basada en el rol de nuestra base de datos
        SimpleGrantedAuthority autoridad = new SimpleGrantedAuthority(usuarioReal.getRol());

        // 4. Devolvemos un nuevo usuario que SI TIENE el rol de la base de datos
        return new DefaultOAuth2User(
                Collections.singleton(autoridad), 
                oAuth2User.getAttributes(), 
                "email" // O la clave que use tu proveedor como ID (habitualmente email)
        );
    }
}