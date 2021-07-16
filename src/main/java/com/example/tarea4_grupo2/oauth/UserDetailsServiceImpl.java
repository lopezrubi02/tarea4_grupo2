package com.example.tarea4_grupo2.oauth;

import com.example.tarea4_grupo2.entity.Usuario;
import com.example.tarea4_grupo2.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UsuarioRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username){
        Usuario user = userRepository.getUserByUsername(username);
        System.out.println("******TRACER 2**************");
        if (user == null) {
            System.out.println("USUARIO NULO");
            throw new UsernameNotFoundException("Could not find user");
        }else{
            System.out.println(user.getEmail());
            System.out.println("USUARIO NO NULO");
        }
        return new MyUserDetails(user);
    }
}
