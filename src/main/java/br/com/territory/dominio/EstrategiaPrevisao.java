package br.com.territory.dominio;

import java.util.List;

import br.com.territory.excecao.DadosInvalidosException;

// Molde de toda previsão. Não existe "previsão genérica":
// existe média móvel, comparação com o período anterior... (as filhas).
public abstract class EstrategiaPrevisao {

    // Com menos leituras do que isso, o sistema prefere se calar
    public static final int MINIMO_LEITURAS = 5;

    private String nome;
    private int leiturasNecessarias;

    public EstrategiaPrevisao(String nome, int leiturasNecessarias) {
        this.nome = nome;
        this.setLeiturasNecessarias(leiturasNecessarias);
    }

    // Comportamento base: confere se o histórico basta e devolve a última leitura.
    // As filhas chamam super.prever(historico) e depois fazem a própria conta.
    public double prever(List<Double> historico) {
        if (historico == null || historico.size() < this.leiturasNecessarias) {
            int quantidade = (historico == null) ? 0 : historico.size();
            throw new DadosInvalidosException(String.format(
                    "%s precisa de %d leituras no histórico e há apenas %d",
                    this.nome, this.leiturasNecessarias, quantidade));
        }
        return historico.get(historico.size() - 1);
    }

    // Cada filha explica, em português, como chegou ao número
    public abstract String descreverCalculo();

    public String getNome() {
        return this.nome;
    }

    public int getLeiturasNecessarias() {
        return this.leiturasNecessarias;
    }

    // Média de um trecho do histórico (usada pelas filhas)
    protected double calcularMedia(List<Double> valores) {
        double soma = 0;
        for (double valor : valores) {
            soma += valor;
        }
        return soma / valores.size();
    }

    private void setLeiturasNecessarias(int leiturasNecessarias) {
        if (leiturasNecessarias < MINIMO_LEITURAS) {
            throw new DadosInvalidosException(String.format(
                    "uma previsão precisa de pelo menos %d leituras (pedido: %d)", MINIMO_LEITURAS, leiturasNecessarias));
        }
        this.leiturasNecessarias = leiturasNecessarias;
    }
}
