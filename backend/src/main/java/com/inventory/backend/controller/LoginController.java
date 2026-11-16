
package com.inventory.backend.controller;

import com.inventory.backend.dto.LoginRequest;
import com.inventory.backend.model.Usuario;
import com.inventory.backend.service.SenhaService;
import com.inventory.backend.service.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class LoginController {

    private final UsuarioController usuarioController;
    private final SenhaService senhaService;
    private final JwtService jwtService;

    public LoginController(
            UsuarioController usuarioController,
            SenhaService senhaService,
            JwtService jwtService) {

        this.usuarioController = usuarioController;
        this.senhaService = senhaService;
        this.jwtService = jwtService;
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest dados) {

        Usuario usuario = usuarioController.buscarPorCpf(dados.getCpf());

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("CPF ou senha incorretos.");
        }

        if (!senhaService.verificar(dados.getSenha(), usuario.getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("CPF ou senha incorretos.");
        }

        if (!"ATIVO".equals(usuario.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Usuário não está autorizado a acessar o sistema.");
        }

        String token = jwtService.gerarToken(usuario);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "tipo", "Bearer",
                "usuario", usuario
        ));
    }

    // TROCAR SENHA NO PRIMEIRO ACESSO
    @PutMapping("/trocar-senha")
    public ResponseEntity<?> trocarSenha(
            @RequestBody Map<String, String> dados,
            Authentication autenticacao) {

        if (autenticacao == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Autenticação necessária.");
        }

        Usuario usuario = usuarioController.buscarPorCpf(
                autenticacao.getName()
        );

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Usuário não encontrado.");
        }

        if (!usuario.isTrocarSenhaNoPrimeiroAcesso()) {
            return ResponseEntity.badRequest()
                    .body("A troca obrigatória de senha já foi realizada.");
        }

        String senhaAtual = dados.get("senhaAtual");
        String novaSenha = dados.get("novaSenha");

        if (senhaAtual == null || senhaAtual.isBlank()
                || novaSenha == null || novaSenha.isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Informe a senha atual e a nova senha.");
        }

        if (!senhaService.verificar(senhaAtual, usuario.getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("A senha temporária está incorreta.");
        }

        if (novaSenha.length() < 8) {
            return ResponseEntity.badRequest()
                    .body("A nova senha deve possuir pelo menos 8 caracteres.");
        }

        // SALVAR A NOVA SENHA CRIPTOGRAFADA
        usuario.setSenha(senhaService.criptografar(novaSenha));

        // LIBERAR O ACESSO NORMAL AO SISTEMA
        usuario.setTrocarSenhaNoPrimeiroAcesso(false);

        // GERAR UM NOVO TOKEN SEM A RESTRIÇÃO
        String novoToken = jwtService.gerarToken(usuario);

        return ResponseEntity.ok(Map.of(
                "mensagem", "Senha alterada com sucesso.",
                "token", novoToken,
                "tipo", "Bearer",
                "usuario", usuario
        ));
    }
}
