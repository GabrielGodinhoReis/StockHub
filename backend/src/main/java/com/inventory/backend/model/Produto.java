
package com.inventory.backend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public class Produto {

    private Long id;

    @NotBlank(message = "O código SKU é obrigatório.")
    private String sku;

    @NotBlank(message = "O nome do produto é obrigatório.")
    private String nome;

    @NotBlank(message = "A categoria do produto é obrigatória.")
    private String categoria;

    @PositiveOrZero(message = "O preço de custo não pode ser negativo.")
    private double precoCusto;

    @PositiveOrZero(message = "O preço de venda não pode ser negativo.")
    private double precoVenda;

    @PositiveOrZero(message = "A quantidade não pode ser negativa.")
    private int quantidade;

    @PositiveOrZero(message = "O estoque mínimo não pode ser negativo.")
    private int estoqueMinimo;

    public Produto() {
    }

    public Produto(Long id, String sku, String nome, String categoria,
                   double precoCusto, double precoVenda,
                   int quantidade, int estoqueMinimo) {
        this.id = id;
        this.sku = sku;
        this.nome = nome;
        this.categoria = categoria;
        this.precoCusto = precoCusto;
        this.precoVenda = precoVenda;
        this.quantidade = quantidade;
        this.estoqueMinimo = estoqueMinimo;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getNome() {
        return nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public double getPrecoCusto() {
        return precoCusto;
    }

    public double getPrecoVenda() {
        return precoVenda;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public int getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setPrecoCusto(double precoCusto) {
        this.precoCusto = precoCusto;
    }

    public void setPrecoVenda(double precoVenda) {
        this.precoVenda = precoVenda;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public void setEstoqueMinimo(int estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }
}
