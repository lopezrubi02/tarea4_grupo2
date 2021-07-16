package com.example.tarea4_grupo2.oauth;

import com.example.tarea4_grupo2.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serializable;
import java.util.Collection;
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
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(user != null){
            return AuthorityUtils.createAuthorityList(user.getRol());
        }else{
            return oAuth2User.getAuthorities();
        }
    }

    @Override
    public String getName() {
        return oAuth2User.getAttribute("name");
    }

    public String getFullName(){
        return oAuth2User.getAttribute("name");
    }

    public String getEmail(){
        return oAuth2User.getAttribute("email");
    }
}