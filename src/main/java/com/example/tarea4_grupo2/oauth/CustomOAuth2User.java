package com.example.tarea4_grupo2.oauth;

import com.example.tarea4_grupo2.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import sun.plugin2.message.LaunchJVMAppletMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User, Serializable {

    private OAuth2User oAuth2User;

    private Usuario user;

    public CustomOAuth2User(OAuth2User oauth2User, Usuario user) {
        this.oAuth2User = oauth2User;
        this.user = user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        System.out.println("*********TRACER 22******************");
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return AuthorityUtils.createAuthorityList(user.getRol());
    }

    @Override
    public String getName() {
        System.out.println("****************TRACER 13************");
        return oAuth2User.getAttribute("name");
    }

    public String getFullName(){
        System.out.println("****************TRACER 4************");
        return oAuth2User.getAttribute("name");
    }

    public String getEmail(){
        System.out.println("****************TRACER 7************");
        return oAuth2User.getAttribute("email");
    }
}
