package com.example.tarea4_grupo2.oauth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private OAuth2User oAuth2User;

    public CustomOAuth2User(OAuth2User oAuth2User) {
        this.oAuth2User = oAuth2User;
    }

    @Override
    public Map<String, Object> getAttributes() {
        System.out.println("*********TRACER 22******************");

        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        System.out.println("*********TRACER 21******************");
        return oAuth2User.getAuthorities();
    }

    @Override
    public String getName() {
        System.out.println("****************TRACER 13************");
        System.out.println(oAuth2User.getAuthorities());
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
