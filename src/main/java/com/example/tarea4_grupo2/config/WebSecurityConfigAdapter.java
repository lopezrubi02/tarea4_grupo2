package com.example.tarea4_grupo2.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfigAdapter extends org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter {

    @Autowired
    DataSource dataSource;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception{

        httpSecurity.formLogin()
                .loginPage("/login") // for the Controlller
                .loginProcessingUrl("/processLogin") // for the POST request of the login form
                .defaultSuccessUrl("/redirectByRol",true);
        httpSecurity.logout()
                .logoutSuccessUrl("/loginAdmin")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true);

        httpSecurity.authorizeRequests()
                .antMatchers("/admin", "/admin/**").hasAuthority("AdminSistema")
             //   .antMatchers("/cliente","/cliente/**").hasAuthority("Cliente")
                .antMatchers("/adminrest","/adminrest/**").hasAuthority("AdminRestaurante")
                .anyRequest().permitAll();


    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(new BCryptPasswordEncoder())
                .usersByUsernameQuery("select email, contraseniahash, cuentaactiva from usuarios WHERE email =?")
                .authoritiesByUsernameQuery("select email, rol from usuarios where cuentaactiva=1 and email =?");


    }

}