package com.TracoCultural.TracoCultural.config;

import com.TracoCultural.TracoCultural.config.security.JwtAuthFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.List;


@Configuration
public class SecurityConfig {


    @Autowired
    private JwtAuthFilter jwtAuthFilter;



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {


        return http

                .csrf(csrf -> csrf.disable())


                .cors(cors -> cors.configurationSource(corsConfigurationSource()))


                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )


                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/verificar-codigo").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/reenviar-codigo").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/esqueci-senha").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/redefinir-senha").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/auth/register").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/eventos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/eventos/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/eventos/{id}/comentarios").permitAll()

                        .requestMatchers(HttpMethod.POST,   "/api/v1/eventos").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/eventos/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/eventos/{id}").authenticated()
                        .requestMatchers("/api/v1/favoritos/{id}").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/v1/eventos/{id}/comentarios").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/eventos/{id}/comentarios/{id2}").authenticated()
                        .requestMatchers("/api/v1/usuarios/{id}").authenticated()
                        .requestMatchers("/api/v1/admin/{id}").authenticated()

                        .anyRequest().authenticated()
                )


                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                )


                .build();
    }


    @Bean
    public PasswordEncoder passwordEncoder(){

        return new BCryptPasswordEncoder();

    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource(){


        CorsConfiguration configuration = new CorsConfiguration();


        configuration.setAllowedOriginPatterns(
                List.of("*")
        );


        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );


        configuration.setAllowedHeaders(
                List.of("*")
        );


        configuration.setAllowCredentials(false);



        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();



        source.registerCorsConfiguration(
                "/**",
                configuration
        );



        return source;

    }

}