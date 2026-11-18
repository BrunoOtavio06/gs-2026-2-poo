package br.com.territory.dominio;

import br.com.territory.excecao.DadosInvalidosException;
import br.com.territory.excecao.FonteIndisponivelException;

// Medidor de carga: quanto da capacidade do transformador está em uso.
public class SensorCargaRede implements Sensor {

    // Constantes da classe: tipo, unidade e faixa física aceita
    public static final String TIPO = "Carga da rede";
    private static final String UNIDADE = "%";
    private static final double VALOR_MINIMO = 0.0;
    private static final double VALOR_MAXIMO = 150.0;

    private String codigo;

    public SensorCargaRede(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public double lerValor() {
        if (!this.verificarFuncionamento()) {
            throw new FonteIndisponivelException(this.codigo, "o equipamento não respondeu à consulta");
        }

        // Simulação: uso de ventiladores e ar-condicionado, entre 50% e 105% da capacidade
        double valor = 50 + Math.random() * 55;

        // Em 3% das leituras o equipamento manda um valor corrompido
        if (Math.random() < 0.03) {
            valor = -999.0;
        }

        this.validarFaixa(valor);
        return Math.round(valor * 10) / 10.0; // uma casa decimal
    }

    @Override
    public boolean verificarFuncionamento() {
        return Math.random() > Sensor.CHANCE_FALHA;
    }

    @Override
    public String retornarTipo() {
        return TIPO;
    }

    @Override
    public String retornarUnidade() {
        return UNIDADE;
    }

    @Override
    public String getCodigo() {
        return this.codigo;
    }

    // Fail fast: leitura fora da faixa física nunca entra no sistema
    private void validarFaixa(double valor) {
        if (valor < VALOR_MINIMO || valor > VALOR_MAXIMO) {
            throw new DadosInvalidosException(String.format(
                    "leitura de %s fora da faixa física (%.0f a %.0f %s): %.1f",
                    TIPO.toLowerCase(), VALOR_MINIMO, VALOR_MAXIMO, UNIDADE, valor));
        }
    }
}
