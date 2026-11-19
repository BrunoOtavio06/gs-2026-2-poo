package br.com.territory.servico;

import java.util.HashMap;
import java.util.Map;

import br.com.territory.dominio.SensorCargaRede;
import br.com.territory.dominio.SensorTemperaturaAr;
import br.com.territory.dominio.SensorTemperaturaOleo;
import br.com.territory.excecao.DadosInvalidosException;

// SINGLETON: uma única configuração para o sistema inteiro.
// Todos os pontos que consultam limiares leem a MESMA instância.
public class ConfiguracaoSistema {

    private static ConfiguracaoSistema instancia;

    // Limite de cada tipo de sensor (a chave é o tipo do sensor)
    private Map<String, Double> limiares;
    private int janelaMediaMovel;
    private int periodoComparacao;

    // Construtor PRIVATE: ninguém fora daqui consegue dar new
    private ConfiguracaoSistema() {
        this.limiares = new HashMap<>();
        this.limiares.put(SensorTemperaturaAr.TIPO, 35.0);    // °C: dia de onda de calor
        this.limiares.put(SensorCargaRede.TIPO, 85.0);        // % da capacidade do transformador
        this.limiares.put(SensorTemperaturaOleo.TIPO, 95.0);  // °C: óleo isolante aquecido demais
        this.janelaMediaMovel = 5;
        this.periodoComparacao = 3;
    }

    // synchronized: duas threads não criam duas instâncias ao mesmo tempo
    public static synchronized ConfiguracaoSistema getInstancia() {
        if (instancia == null) {
            instancia = new ConfiguracaoSistema();
        }
        return instancia;
    }

    public double buscarLimiar(String tipoSensor) {
        Double limite = this.limiares.get(tipoSensor);
        if (limite == null) {
            throw new DadosInvalidosException("não existe limiar configurado para o sensor do tipo " + tipoSensor);
        }
        return limite;
    }

    public void definirLimiar(String tipoSensor, double novoLimite) {
        if (!this.limiares.containsKey(tipoSensor)) {
            throw new DadosInvalidosException("tipo de sensor desconhecido: " + tipoSensor);
        }
        if (novoLimite <= 0) {
            throw new DadosInvalidosException("o limiar deve ser maior que zero: " + novoLimite);
        }
        this.limiares.put(tipoSensor, novoLimite);
    }

    public int getJanelaMediaMovel() {
        return this.janelaMediaMovel;
    }

    public int getPeriodoComparacao() {
        return this.periodoComparacao;
    }
}
