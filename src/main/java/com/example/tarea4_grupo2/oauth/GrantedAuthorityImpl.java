package com.example.tarea4_grupo2.oauth;

import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;

public class GrantedAuthorityImpl extends Object implements GrantedAuthority, Serializable {


    public GrantedAuthorityImpl(String adminSistema) {

    }

    @Override
    public String getAuthority() {
        return null;
    }
}
