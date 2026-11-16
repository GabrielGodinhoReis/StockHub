
package com.inventory.backend.controller;

import com.inventory.backend.model.Usuario;
import com.inventory.backend.model.Loja;
import com.inventory.backend.service.LojaService;
import com.inventory.backend.service.SenhaService;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final List<Usuario> usuarios = new ArrayList<>();

    private final LojaService lojaService;
    private final SenhaService senhaService;

    private final SecureRandom geradorAleatorio = new SecureRandom();

    private Long proximoId = 1L;

    public UsuarioController(
            LojaService lojaService,
            SenhaService senhaService) {

        this.lojaService = lojaService;
        this.senhaService = senhaService;
    }

    // BUSCAR USUÁRIO PELO CPF
    public Usuario buscarPorCpf(String cpf) {

        return usuarios.stream()
                .filter(u -> u.getCpf() != null
                        && u.getCpf().equals(cpf))
                .findFirst()
                .orElse(null);
    }

    // IDENTIFICAR O ADMINISTRADOR AUTENTICADO
    private Usuario obterAdministrador(Authentication autenticacao) {

        if (autenticacao == null) {
            return null;
        }

        Usuario administrador = buscarPorCpf(autenticacao.getName());

        if (administrador == null) {
            return null;
        }

        if (!"ADMINISTRADOR".equals(administrador.getFuncao())) {
            return null;
        }

        if (!"ATIVO".equals(administrador.getStatus())) {
            return null;
        }

        if (administrador.getLoja() == null) {
            return null;
        }

        return administrador;
    }

    // GERAR SENHA TEMPORÁRIA ALEATÓRIA
    private String gerarSenhaTemporaria() {

        String caracteres =
                "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$";

        StringBuilder senha = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            int indice = geradorAleatorio.nextInt(caracteres.length());
            senha.append(caracteres.charAt(indice));
        }

        return senha.toString();
    }

    // LISTAR USUÁRIOS DA LOJA DO ADMINISTRADOR
    @GetMapping
    public ResponseEntity<?> listarUsuarios(Authentication autenticacao) {

        Usuario administrador = obterAdministrador(autenticacao);

        if (administrador == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acesso permitido somente a administradores ativos.");
        }

        Long lojaId = administrador.getLoja().getId();

        List<Usuario> usuariosDaLoja = usuarios.stream()
                .filter(u -> u.getLoja() != null
                        && u.getLoja().getId().equals(lojaId))
                .toList();

        return ResponseEntity.ok(usuariosDaLoja);
    }

    // CADASTRAR FUNCIONÁRIO OU OUTRO ADMINISTRADOR
    @PostMapping
    public ResponseEntity<?> cadastrarUsuario(
            @RequestBody Usuario usuario,
            Authentication autenticacao) {

        Usuario administrador = obterAdministrador(autenticacao);

        if (administrador == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Somente administradores ativos podem cadastrar usuários.");
        }

        // VALIDAR NOME
        if (usuario.getNome() == null
                || usuario.getNome().isBlank()
                || usuario.getSobrenome() == null
                || usuario.getSobrenome().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Preencha o nome e o sobrenome.");
        }

        // VALIDAR CPF
        if (usuario.getCpf() == null
                || usuario.getCpf().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("O CPF é obrigatório.");
        }

        // VALIDAR E-MAIL
        if (usuario.getEmail() == null
                || !usuario.getEmail().matches(
                        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            return ResponseEntity.badRequest()
                    .body("Informe um e-mail válido.");
        }

        // VALIDAR FUNÇÃO
        String funcao = usuario.getFuncao();

        if (!"FUNCIONARIO".equals(funcao)
                && !"ADMINISTRADOR".equals(funcao)) {

            return ResponseEntity.badRequest()
                    .body("Selecione uma função válida.");
        }

        // VERIFICAR CPF DUPLICADO
        if (buscarPorCpf(usuario.getCpf()) != null) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Este CPF já está cadastrado.");
        }

        // GERAR SENHA TEMPORÁRIA
        String senhaTemporaria = gerarSenhaTemporaria();

        // IDENTIFICAR A LOJA DO ADMINISTRADOR
        Loja loja = administrador.getLoja();

        // CONFIGURAR NOVO USUÁRIO
        usuario.setId(proximoId);
        usuario.setLoja(loja);
        usuario.setFuncao(funcao);
        usuario.setStatus("ATIVO");

        // EXIGIR TROCA DE SENHA NO PRIMEIRO ACESSO
        usuario.setTrocarSenhaNoPrimeiroAcesso(true);

        // ARMAZENAR SOMENTE A SENHA CRIPTOGRAFADA
        usuario.setSenha(
                senhaService.criptografar(senhaTemporaria)
        );

        usuarios.add(usuario);
        proximoId++;

        // RETORNAR A SENHA TEMPORÁRIA UMA ÚNICA VEZ
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "mensagem", "Usuário cadastrado com sucesso.",
                        "senhaTemporaria", senhaTemporaria,
                        "usuario", usuario
                )
        );
    }

    // CADASTRAR PRIMEIRO ADMINISTRADOR DA LOJA
    @PostMapping("/administrador")
    public ResponseEntity<?> cadastrarAdministrador(
            @RequestParam Long lojaId,
            @RequestParam String codigoAtivacao,
            @RequestBody Usuario usuario) {

        Loja loja = lojaService.buscarPorId(lojaId).orElse(null);

        if (loja == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Loja não encontrada.");
        }

        if (!lojaService.codigoAtivacaoValido(lojaId, codigoAtivacao)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Código de ativação inválido.");
        }

        boolean administradorExiste = usuarios.stream()
                .anyMatch(u -> "ADMINISTRADOR".equals(u.getFuncao())
                        && u.getLoja() != null
                        && u.getLoja().getId().equals(lojaId));

        if (administradorExiste) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Esta loja já possui um administrador.");
        }

        if (buscarPorCpf(usuario.getCpf()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Este CPF já está cadastrado.");
        }

        usuario.setId(proximoId);
        usuario.setLoja(loja);
        usuario.setFuncao("ADMINISTRADOR");
        usuario.setStatus("ATIVO");
        usuario.setSenha(senhaService.criptografar(usuario.getSenha()));

        usuarios.add(usuario);
        proximoId++;

        lojaService.consumirCodigoAtivacao(lojaId);

        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }
}
