package com.example.tarea4_grupo2.oauth;

import com.example.tarea4_grupo2.entity.Usuario;
import com.example.tarea4_grupo2.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    UsuarioRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);
        System.out.println("CustomOAuth2UserService invoked 2");
        System.out.println(user.getAttribute("email").toString());
        Usuario userobtenido = userRepository.getUserByUsername(user.getAttribute("email").toString());
        return new CustomOAuth2User(user,userobtenido);
    }
}