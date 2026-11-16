
package com.inventory.backend.controller;

import com.inventory.backend.model.Loja;
import com.inventory.backend.service.LojaService;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/lojas")
public class LojaController {

    private final LojaService lojaService;

    public LojaController(LojaService lojaService) {
        this.lojaService = lojaService;
    }

    @GetMapping
    public List<Loja> listarLojas() {
        return lojaService.listar();
    }

    @PostMapping
    public ResponseEntity<?> cadastrarLoja(@RequestBody Loja loja) {

        if (lojaService.cnpjExiste(loja.getCnpj())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Este CNPJ já está cadastrado.");
        }

        Loja lojaSalva = lojaService.salvar(loja);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lojaSalva);
    }

    @GetMapping("/{id}/codigo-ativacao")
    public ResponseEntity<?> consultarCodigoAtivacao(@PathVariable Long id) {

        Loja loja = lojaService.buscarPorId(id).orElse(null);

        if (loja == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Loja não encontrada.");
        }

        String codigo = lojaService.buscarCodigoAtivacao(id);

        if (codigo == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Código de ativação já utilizado.");
        }

        return ResponseEntity.ok(codigo);
    }
}
