package com.example.tarea4_grupo2.config;

import com.example.tarea4_grupo2.oauth.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Collection;

@Configuration
@EnableWebSecurity
public class WebSecurityConfigAdapter extends org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter {

    //para api google
    @Autowired
    private CustomOAuth2UserService oauthUserService;
    @Autowired
    private UserService userService;

    @Autowired
    DataSource dataSource;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception{


     /*   httpSecurity.formLogin()
                .loginPage("/login") // for the Controlller
                .loginProcessingUrl("/processLogin") // for the POST request of the login form
                .defaultSuccessUrl("/redirectByRol",true);

        httpSecurity.oauth2Login()
                .loginPage("/login") // for the Controlller
                .userInfoEndpoint().userService(oauthUserService)
                .and()
                .successHandler(oAuth2LoginSuccessHandler);


        httpSecurity.logout()
                .logoutSuccessUrl("/login")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true);
*/
        httpSecurity.authorizeRequests()
                .antMatchers("/oauth2/**").permitAll()
                .antMatchers("/admin", "/admin/**").hasAuthority("AdminSistema")
                .antMatchers("/cliente","/cliente/**").hasAuthority("Cliente")
                .antMatchers("/adminrest","/adminrest/**").hasAuthority("AdminRestaurante")
                .antMatchers("/repartidor","/repartidor/**").hasAuthority("Repartidor")
                .antMatchers("/login", "/login/**").permitAll()
                .antMatchers("/cambiar1/**", "/cambiar1/", "/cambiarContrasenia").permitAll()
                .anyRequest().permitAll()
                .and()
                .formLogin().permitAll()
                    .loginPage("/login")
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .defaultSuccessUrl("/redirectByRolDB")
                .and()
                .oauth2Login()
                    .loginPage("/login")
                    .userInfoEndpoint()
                        .userService(oauthUserService)
                    .and()
                    .successHandler(new AuthenticationSuccessHandler() {

                        @Override
                        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                            Authentication authentication) throws IOException, ServletException {
                            System.out.println("AuthenticationSuccessHandler invoked");
                            System.out.println("Authentication name: " + authentication.getName());
                            CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();
                            boolean existe = userService.processOAuthPostLogin(oauthUser.getEmail());
                            if(existe){
                                System.out.println("DEBERIA IR A REDIRECT BY ROL");
                                response.sendRedirect("/redirectByRol");
                            }else{
                                System.out.println("REGRESA LA LOGIN");
                                response.sendRedirect("/login");
                            }
                        }
                    })
                .and()
                .logout()
                    .logoutSuccessUrl("/login")
                    .deleteCookies("JSESSIONID")
                    .invalidateHttpSession(true)
                .and()
                .exceptionHandling().accessDeniedPage("/login/403");

        System.out.println("****************TRACER 1************");
    }

    //para login por db se usa MyUserDetails, UserDetailsService y UserService
    /*@Bean
    public UserDetailsService userDetailsService() {
        System.out.println("*********TRACER 22******************");

        return new UserDetailsServiceImpl();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        System.out.println("*********TRACER 23******************");

        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        System.out.println("*********TRACER 24******************");

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        System.out.println("¨*****TRACER LOG DB*************");
        auth.authenticationProvider(authenticationProvider());

    }*/


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(new BCryptPasswordEncoder())
                .usersByUsernameQuery("select email, contraseniahash, cuentaactiva from usuarios WHERE email =?")
                .authoritiesByUsernameQuery("select u.email, u.rol from usuarios u where u.email=? and u.cuentaactiva!=-1");

        System.out.println("****************TRACER 2************");

    }
                //valores cuenta activa:
    /*
                -1 -> no hay acceso (se setea al registrar repartidor) cuenta repartidor por aceptar
                1 -> restaurante aceptado / cuenta repartidor aceptada
                2 -> restaurante por aceptar
                3 -> no hay restaurante registrado / no aceptado
     */

}