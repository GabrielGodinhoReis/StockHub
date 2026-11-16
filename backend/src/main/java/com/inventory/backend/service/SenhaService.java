package com.inventory.backend.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SenhaService {

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public String criptografar(String senha) {
        return passwordEncoder.encode(senha);
    }

    public boolean verificar(String senhaDigitada, String senhaCriptografada) {
        return passwordEncoder.matches(senhaDigitada, senhaCriptografada);
    }
}