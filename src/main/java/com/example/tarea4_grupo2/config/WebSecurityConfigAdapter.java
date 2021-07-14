package com.example.tarea4_grupo2.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
                .logoutSuccessUrl("/login")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true);
        
        httpSecurity.authorizeRequests(a -> a
                .antMatchers("/admin", "/admin/**").hasAuthority("AdminSistema")
                .antMatchers("/cliente","/cliente/**").hasAuthority("Cliente")
                .antMatchers("/adminrest","/adminrest/**").hasAuthority("AdminRestaurante")
                .antMatchers("/repartidor","/repartidor/**").hasAuthority("Repartidor")
                .antMatchers("/login", "/login/**").permitAll()
                .antMatchers("/cambiar1/**", "/cambiar1/", "/cambiarContrasenia").permitAll()
                .anyRequest().permitAll())
                .exceptionHandling(e->e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .oauth2Login().permitAll()//cambiar el defaultUrl!?
                .defaultSuccessUrl("/")
                .userInfoEndpoint(userInfoEndpoint ->
                        userInfoEndpoint
                                .userAuthoritiesMapper(this.userAuthoritiesMapper()))
                .and()
                .logout()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutSuccessUrl("/");


    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(new BCryptPasswordEncoder())
                .usersByUsernameQuery("select email, contraseniahash, cuentaactiva from usuarios WHERE email =?")
                .authoritiesByUsernameQuery("select u.email, u.rol from usuarios u where u.email=? and u.cuentaactiva!=-1");
    }
                //valores cuenta activa:
    /*
                -1 -> no hay acceso (se setea al registrar repartidor) cuenta repartidor por aceptar
                1 -> restaurante aceptado / cuenta repartidor aceptada
                2 -> restaurante por aceptar
                3 -> no hay restaurante registrado / no aceptado
     */

    private GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (OidcUserAuthority.class.isInstance(authority)) {
                    OidcUserAuthority oidcUserAuthority = (OidcUserAuthority)authority;

                    OidcIdToken idToken = oidcUserAuthority.getIdToken();
                    OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();

                    // Map the claims found in idToken and/or userInfo
                    // to one or more GrantedAuthority's and add it to mappedAuthorities

                } else if (OAuth2UserAuthority.class.isInstance(authority)) {
                    OAuth2UserAuthority oauth2UserAuthority = (OAuth2UserAuthority)authority;

                    Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();

                    // Map the attributes found in userAttributes
                    // to one or more GrantedAuthority's and add it to mappedAuthorities

                }
            });

            return mappedAuthorities;
        };
    }
}