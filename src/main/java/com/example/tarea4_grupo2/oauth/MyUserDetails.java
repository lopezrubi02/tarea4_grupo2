package com.example.tarea4_grupo2.oauth;

import com.example.tarea4_grupo2.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MyUserDetails implements UserDetails {

    private Usuario user;

    public MyUserDetails(Usuario user) {
        System.out.println("****** TRACER 31******************");
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        System.out.println("****** TRACER 30******************");
        String roluser = user.getRol();

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(roluser));
        return authorities;
    }

    @Override
    public String getPassword() {
        System.out.println("****** TRACER 32******************");
        System.out.println(user.getContraseniaHash());

        return user.getContraseniaHash();
    }

    @Override
    public String getUsername() {
        System.out.println("****** TRACER 33******************");
        System.out.println(user.getEmail());

        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        System.out.println("****** TRACER 37******************");

        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        System.out.println("****** TRACER 36******************");

        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        System.out.println("****** TRACER 35******************");

        return true;
    }

    @Override
    public boolean isEnabled() {
        System.out.println("****** TRACER 34******************");
        System.out.println(user.isCuentaActiva());

        return user.isCuentaActiva();
    }
}
