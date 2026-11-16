
package com.inventory.backend.service;

import com.inventory.backend.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String SECRET_KEY =
            "MinhaChaveSuperSecretaParaJWT2026SistemaEstoque123456";

    private static final long TEMPO_EXPIRACAO = 1000L * 60 * 60 * 8;

    private SecretKey getChave() {
        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }

    // GERAR TOKEN
    public String gerarToken(Usuario usuario) {

        return Jwts.builder()
                .subject(usuario.getCpf())
                .claim("id", usuario.getId())
                .claim("funcao", usuario.getFuncao())
                .claim("lojaId", usuario.getLoja().getId())
                .claim("trocarSenha", usuario.isTrocarSenhaNoPrimeiroAcesso())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TEMPO_EXPIRACAO))
                .signWith(getChave())
                .compact();
    }

    // VALIDAR TOKEN E EXTRAIR INFORMAÇÕES
    public Claims validarToken(String token) {

        return Jwts.parser()
                .verifyWith(getChave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
