package br.com.territory.dominio;

import java.util.List;

// Comparação com o período anterior: compara a média das últimas N leituras
// com a média das N anteriores e projeta que a tendência continua.
// Ex.: anterior 30 °C, atual 33 °C → subiu 3 → previsão 36 °C.
public class PrevisaoComparacao extends EstrategiaPrevisao {

    private int periodo;

    public PrevisaoComparacao(int periodo) {
        super("Comparação com o período anterior", periodo * 2); // precisa de dois períodos
        this.periodo = periodo;
    }

    @Override
    public double prever(List<Double> historico) {
        super.prever(historico); // valida se há leituras suficientes

        int fim = historico.size();
        double mediaAtual = this.calcularMedia(historico.subList(fim - this.periodo, fim));
        double mediaAnterior = this.calcularMedia(historico.subList(fim - this.periodo * 2, fim - this.periodo));

        double variacao = mediaAtual - mediaAnterior;
        return mediaAtual + variacao;
    }

    @Override
    public String descreverCalculo() {
        return "tendência das últimas " + this.periodo + " leituras contra as " + this.periodo + " anteriores";
    }
}
