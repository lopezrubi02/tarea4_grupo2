package com.example.tarea4_grupo2.oauth;

import com.example.tarea4_grupo2.entity.Usuario;
import com.example.tarea4_grupo2.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UsuarioRepository repo;

    public boolean processOAuthPostLogin(String username) {
        Usuario existUser = repo.getUserByUsername(username);
        boolean existe = true;
        if (existUser == null) {
            existe = false;
        }
        return existe;
    }
}