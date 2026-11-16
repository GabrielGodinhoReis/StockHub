
package com.inventory.backend.config;

import com.inventory.backend.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {
            Claims claims = jwtService.validarToken(token);

            String cpf = claims.getSubject();
            String funcao = claims.get("funcao", String.class);

            Boolean trocarSenha = claims.get("trocarSenha", Boolean.class);

            List<SimpleGrantedAuthority> autoridades = new ArrayList<>();

            autoridades.add(
                    new SimpleGrantedAuthority("ROLE_" + funcao)
            );

            if (Boolean.TRUE.equals(trocarSenha)) {
                autoridades.add(
                        new SimpleGrantedAuthority("SENHA_PENDENTE")
                );
            }

            var autenticacao = new UsernamePasswordAuthenticationToken(
                    cpf,
                    null,
                    autoridades
            );

            SecurityContextHolder.getContext().setAuthentication(autenticacao);

            // BLOQUEAR ACESSO ENQUANTO A SENHA TEMPORÁRIA NÃO FOR TROCADA
            String caminho = request.getServletPath();

            boolean rotaTrocaSenha = "/auth/trocar-senha".equals(caminho);
            boolean requisicaoOptions = "OPTIONS".equalsIgnoreCase(request.getMethod());

            if (Boolean.TRUE.equals(trocarSenha)
                    && !rotaTrocaSenha
                    && !requisicaoOptions) {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Você precisa trocar sua senha antes de acessar o sistema."
                );

                return;
            }

        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
