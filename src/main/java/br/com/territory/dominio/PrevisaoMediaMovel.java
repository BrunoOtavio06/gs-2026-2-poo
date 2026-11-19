package br.com.territory.dominio;

import java.util.List;

// Média móvel: a previsão é a média das últimas N leituras.
public class PrevisaoMediaMovel extends EstrategiaPrevisao {

    private int janela;

    public PrevisaoMediaMovel(int janela) {
        super("Média móvel", janela); // a mãe guarda o nome e valida o mínimo de leituras
        this.janela = janela;
    }

    @Override
    public double prever(List<Double> historico) {
        super.prever(historico); // valida se há leituras suficientes

        List<Double> ultimas = historico.subList(historico.size() - this.janela, historico.size());
        return this.calcularMedia(ultimas);
    }

    @Override
    public String descreverCalculo() {
        return "média das últimas " + this.janela + " leituras";
    }
}
