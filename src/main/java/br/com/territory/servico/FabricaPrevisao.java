package br.com.territory.servico;

import java.util.List;

import org.springframework.stereotype.Component;

import br.com.territory.dominio.EstrategiaPrevisao;
import br.com.territory.dominio.PrevisaoComparacao;
import br.com.territory.dominio.PrevisaoMediaMovel;

// FACTORY: a fábrica sabe montar cada estratégia com os parâmetros da configuração.
// @Component: é um bean Spring, criado pelo container e injetado onde precisar.
@Component
public class FabricaPrevisao {

    public EstrategiaPrevisao criarMediaMovel() {
        return new PrevisaoMediaMovel(ConfiguracaoSistema.getInstancia().getJanelaMediaMovel());
    }

    public EstrategiaPrevisao criarComparacao() {
        return new PrevisaoComparacao(ConfiguracaoSistema.getInstancia().getPeriodoComparacao());
    }

    // Todas as estratégias prontas: quem usa só percorre a lista, sem if de tipo
    public List<EstrategiaPrevisao> criarTodas() {
        return List.of(this.criarMediaMovel(), this.criarComparacao());
    }
}
