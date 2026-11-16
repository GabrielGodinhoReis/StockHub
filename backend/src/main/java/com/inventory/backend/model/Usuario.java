
package com.inventory.backend.model;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Usuario {

    private Long id;
    private String nome;
    private String sobrenome;
    private String cpf;
    private String email;
    private LocalDate dataNascimento;
    private String funcao;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    private String status;
    private Loja loja;

    private boolean trocarSenhaNoPrimeiroAcesso;

    public Usuario() {
    }

    public Usuario(Long id, String nome, String sobrenome,
                   String cpf, LocalDate dataNascimento,
                   String funcao, String senha,
                   String status, Loja loja) {

        this.id = id;
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.funcao = funcao;
        this.senha = senha;
        this.status = status;
        this.loja = loja;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public String getCpf() {
        return cpf;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getFuncao() {
        return funcao;
    }

    public String getSenha() {
        return senha;
    }

    public String getStatus() {
        return status;
    }

    public Loja getLoja() {
        return loja;
    }

    public boolean isTrocarSenhaNoPrimeiroAcesso() {
        return trocarSenhaNoPrimeiroAcesso;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public void setFuncao(String funcao) {
        this.funcao = funcao;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setLoja(Loja loja) {
        this.loja = loja;
    }

    public void setTrocarSenhaNoPrimeiroAcesso(boolean trocarSenhaNoPrimeiroAcesso) {
        this.trocarSenhaNoPrimeiroAcesso = trocarSenhaNoPrimeiroAcesso;
    }
}
