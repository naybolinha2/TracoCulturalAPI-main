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


        http

            // desativa proteção CSRF para API REST
            .csrf(csrf -> csrf.disable())


            // libera CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))


            // API usando JWT não usa sessão
            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS
                    )
            )


            .authorizeHttpRequests(auth -> auth


                    // libera todas as rotas de autenticação
                    .requestMatchers(
                            "/api/v1/auth/**"
                    ).permitAll()



                    // libera OPTIONS do navegador
                    .requestMatchers(
                            HttpMethod.OPTIONS,
                            "/**"
                    ).permitAll()



                    // eventos públicos
                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/v1/eventos/**"
                    ).permitAll()



                    // página de erro
                    .requestMatchers(
                            "/error"
                    ).permitAll()



                    // qualquer outra rota precisa de token
                    .anyRequest().authenticated()

            )



            .addFilterBefore(
                    jwtAuthFilter,
                    UsernamePasswordAuthenticationFilter.class
            );



        return http.build();

    }




    @Bean
    public PasswordEncoder passwordEncoder(){

        return new BCryptPasswordEncoder();

    }





    @Bean
    public CorsConfigurationSource corsConfigurationSource(){


        CorsConfiguration config = new CorsConfiguration();


        config.setAllowedOriginPatterns(
                List.of("*")
        );


        config.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );


        config.setAllowedHeaders(
                List.of("*")
        );


        config.setExposedHeaders(
                List.of("Authorization")
        );


        // como está usando "*", precisa ficar false
        config.setAllowCredentials(false);



        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();


        source.registerCorsConfiguration(
                "/**",
                config
        );


        return source;

    }

}