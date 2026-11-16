
package com.inventory.backend.service;

import com.inventory.backend.model.Loja;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class LojaService {

    private List<Loja> lojas = new ArrayList<>();

    private Map<Long, String> codigosAtivacao = new HashMap<>();

    private Long proximoId = 1L;

    public List<Loja> listar() {
        return lojas;
    }

    public Optional<Loja> buscarPorId(Long id) {
        return lojas.stream()
                .filter(loja -> loja.getId().equals(id))
                .findFirst();
    }

    public boolean cnpjExiste(String cnpj) {
        return lojas.stream()
                .anyMatch(loja -> loja.getCnpj().equals(cnpj));
    }

    public Loja salvar(Loja loja) {
        loja.setId(proximoId);
        lojas.add(loja);

        String codigo = UUID.randomUUID().toString().replace("-", "");
        codigosAtivacao.put(proximoId, codigo);

        proximoId++;

        return loja;
    }

    public String buscarCodigoAtivacao(Long lojaId) {
        return codigosAtivacao.get(lojaId);
    }

    public boolean codigoAtivacaoValido(Long lojaId, String codigo) {
        String codigoSalvo = codigosAtivacao.get(lojaId);

        return codigoSalvo != null && codigoSalvo.equals(codigo);
    }

    public void consumirCodigoAtivacao(Long lojaId) {
        codigosAtivacao.remove(lojaId);
    }
}
