package com.example.tarea4_grupo2.oauth;

import com.example.tarea4_grupo2.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
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

        return oAuth2User.getAuthorities();
    }

 /*   public Collection<? extends GrantedAuthority> getAuthorities(
            String roles) {
        List<GrantedAuthority> authorities
                = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority(roles));

        return authorities;
    }
*/
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
