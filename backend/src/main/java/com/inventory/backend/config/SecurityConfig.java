
package com.inventory.backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // CONFIGURAÇÃO CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://127.0.0.1:5500",
                "http://localhost:5500"
        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                // ROTAS PÚBLICAS
                .requestMatchers(
                    HttpMethod.POST,
                    "/auth/login",
                    "/lojas",
                    "/usuarios/administrador"
                ).permitAll()

                // CONSULTA DO CÓDIGO DE ATIVAÇÃO
                .requestMatchers(
                    HttpMethod.GET,
                    "/lojas/*/codigo-ativacao"
                ).permitAll()

                // TROCA DE SENHA
                .requestMatchers(
                    HttpMethod.PUT,
                    "/auth/trocar-senha"
                ).authenticated()

                // PRODUTOS
                .requestMatchers(HttpMethod.GET, "/produtos")
                    .hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")

                .requestMatchers(HttpMethod.POST, "/produtos")
                    .hasRole("ADMINISTRADOR")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/produtos/**"
                ).hasRole("ADMINISTRADOR")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/produtos/**"
                ).hasRole("ADMINISTRADOR")

                // USUÁRIOS
                .requestMatchers(
                    HttpMethod.GET,
                    "/usuarios"
                ).hasRole("ADMINISTRADOR")

                .requestMatchers(
                    HttpMethod.POST,
                    "/usuarios"
                ).hasRole("ADMINISTRADOR")

                // DEMAIS ROTAS EXIGEM AUTENTICAÇÃO
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
