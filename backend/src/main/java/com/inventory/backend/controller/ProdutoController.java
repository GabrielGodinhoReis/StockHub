
package com.inventory.backend.controller;

import com.inventory.backend.model.Produto;
import com.inventory.backend.model.Usuario;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import jakarta.validation.Valid;

import java.text.Normalizer;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Locale;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final Map<Long, List<Produto>> produtosPorLoja = new HashMap<>();

    private final Map<Long, Long> proximoIdPorLoja = new HashMap<>();

    private final UsuarioController usuarioController;

    public ProdutoController(UsuarioController usuarioController) {
        this.usuarioController = usuarioController;
    }

    // IDENTIFICAR A LOJA PELO USUÁRIO AUTENTICADO
    private Long obterLojaId(Authentication autenticacao) {

        if (autenticacao == null) {
            return null;
        }

        Usuario usuario = usuarioController.buscarPorCpf(
                autenticacao.getName()
        );

        if (usuario == null || usuario.getLoja() == null) {
            return null;
        }

        return usuario.getLoja().getId();
    }

    // NORMALIZAR TEXTO PARA COMPARAÇÃO
    // IGNORA ACENTOS, MAIÚSCULAS, MINÚSCULAS E ESPAÇOS EXTRAS
    private String normalizarTexto(String texto) {

        if (texto == null) {
            return "";
        }

        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", " ");
    }

    // VERIFICAR SE O NOME JÁ EXISTE NA LOJA
    private boolean nomeExiste(
            List<Produto> produtos,
            String nome,
            Long idIgnorado
    ) {

        String nomeNormalizado = normalizarTexto(nome);

        return produtos.stream().anyMatch(produto ->
                normalizarTexto(produto.getNome()).equals(nomeNormalizado)
                && (idIgnorado == null || !produto.getId().equals(idIgnorado))
        );
    }

    // VERIFICAR SE O SKU JÁ EXISTE NA LOJA
    private boolean skuExiste(
            List<Produto> produtos,
            String sku,
            Long idIgnorado
    ) {

        String skuNormalizado = sku == null
                ? ""
                : sku.trim().toLowerCase(Locale.ROOT);

        return produtos.stream().anyMatch(produto ->
                produto.getSku() != null
                && produto.getSku().trim().toLowerCase(Locale.ROOT)
                        .equals(skuNormalizado)
                && (idIgnorado == null || !produto.getId().equals(idIgnorado))
        );
    }

    // LISTAR PRODUTOS DA PRÓPRIA LOJA
    @GetMapping
    public ResponseEntity<?> listarProdutos(Authentication autenticacao) {

        Long lojaId = obterLojaId(autenticacao);

        if (lojaId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Não foi possível identificar a loja do usuário.");
        }

        List<Produto> produtos = produtosPorLoja.computeIfAbsent(
                lojaId,
                id -> new ArrayList<>()
        );

        return ResponseEntity.ok(produtos);
    }

    // CADASTRAR PRODUTO NA PRÓPRIA LOJA
    @PostMapping
    public ResponseEntity<?> cadastrarProduto(
            Authentication autenticacao,
            @Valid @RequestBody Produto produto
    ) {

        Long lojaId = obterLojaId(autenticacao);

        if (lojaId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Não foi possível identificar a loja do usuário.");
        }

        List<Produto> produtos = produtosPorLoja.computeIfAbsent(
                lojaId,
                id -> new ArrayList<>()
        );

        // VERIFICAR NOME DUPLICADO
        if (nomeExiste(produtos, produto.getNome(), null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Já existe um produto com este nome nesta loja.");
        }

        // VERIFICAR SKU DUPLICADO
        if (skuExiste(produtos, produto.getSku(), null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Já existe um produto com este SKU nesta loja.");
        }

        Long proximoId = proximoIdPorLoja.getOrDefault(lojaId, 1L);

        produto.setId(proximoId);

        // REMOVER ESPAÇOS EXTRAS NAS INFORMAÇÕES
        produto.setNome(produto.getNome().trim());
        produto.setSku(produto.getSku().trim());

        produtos.add(produto);

        proximoIdPorLoja.put(lojaId, proximoId + 1);

        return ResponseEntity.status(HttpStatus.CREATED).body(produto);
    }

    // ATUALIZAR PRODUTO DA PRÓPRIA LOJA
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarProduto(
            Authentication autenticacao,
            @PathVariable Long id,
            @Valid @RequestBody Produto produtoAtualizado
    ) {

        Long lojaId = obterLojaId(autenticacao);

        if (lojaId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Não foi possível identificar a loja do usuário.");
        }

        List<Produto> produtos = produtosPorLoja.get(lojaId);

        if (produtos == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Produto não encontrado.");
        }

        for (Produto produto : produtos) {

            if (produto.getId().equals(id)) {

                // VERIFICAR NOME DUPLICADO, IGNORANDO O PRÓPRIO PRODUTO
                if (nomeExiste(
                        produtos,
                        produtoAtualizado.getNome(),
                        id
                )) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Já existe outro produto com este nome nesta loja.");
                }

                // VERIFICAR SKU DUPLICADO, IGNORANDO O PRÓPRIO PRODUTO
                if (skuExiste(
                        produtos,
                        produtoAtualizado.getSku(),
                        id
                )) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Já existe outro produto com este SKU nesta loja.");
                }

                produto.setSku(produtoAtualizado.getSku().trim());
                produto.setNome(produtoAtualizado.getNome().trim());
                produto.setCategoria(produtoAtualizado.getCategoria());
                produto.setPrecoCusto(produtoAtualizado.getPrecoCusto());
                produto.setPrecoVenda(produtoAtualizado.getPrecoVenda());
                produto.setQuantidade(produtoAtualizado.getQuantidade());
                produto.setEstoqueMinimo(produtoAtualizado.getEstoqueMinimo());

                return ResponseEntity.ok(produto);
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Produto não encontrado nesta loja.");
    }

    // EXCLUIR PRODUTO DA PRÓPRIA LOJA
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirProduto(
            Authentication autenticacao,
            @PathVariable Long id
    ) {

        Long lojaId = obterLojaId(autenticacao);

        if (lojaId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Não foi possível identificar a loja do usuário.");
        }

        List<Produto> produtos = produtosPorLoja.get(lojaId);

        if (produtos == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Produto não encontrado.");
        }

        Iterator<Produto> iterator = produtos.iterator();

        while (iterator.hasNext()) {

            Produto produto = iterator.next();

            if (produto.getId().equals(id)) {

                iterator.remove();

                return ResponseEntity.ok("Produto excluído com sucesso!");
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Produto não encontrado nesta loja.");
    }
}
