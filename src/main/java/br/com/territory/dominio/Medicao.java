package br.com.territory.dominio;

import java.time.LocalDateTime;

import br.com.territory.excecao.DadosInvalidosException;
import jakarta.persistence.*;

// @Entity: cada objeto Medicao vira uma linha da tabela "medicoes"
@Entity
@Table(name = "medicoes")
public class Medicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigoSensor;
    private String tipoSensor;
    private Double valor;
    private LocalDateTime dataHora;

    // Construtor vazio: exigido pelo JPA para montar o objeto a partir do banco
    public Medicao() {}

    public Medicao(String codigoSensor, String tipoSensor, Double valor) {
        this.setCodigoSensor(codigoSensor);
        this.setTipoSensor(tipoSensor);
        this.setValor(valor);
        this.dataHora = LocalDateTime.now();
    }

    // Getters e Setters (os setters validam antes de aceitar o dado)
    public Long getId() { return id; }

    public String getCodigoSensor() { return codigoSensor; }

    public void setCodigoSensor(String codigoSensor) {
        if (codigoSensor == null || codigoSensor.isBlank()) {
            throw new DadosInvalidosException("a medição precisa do código do sensor");
        }
        this.codigoSensor = codigoSensor;
    }

    public String getTipoSensor() { return tipoSensor; }

    public void setTipoSensor(String tipoSensor) {
        if (tipoSensor == null || tipoSensor.isBlank()) {
            throw new DadosInvalidosException("a medição precisa do tipo do sensor");
        }
        this.tipoSensor = tipoSensor;
    }

    public Double getValor() { return valor; }

    public void setValor(Double valor) {
        if (valor == null || valor.isNaN() || valor.isInfinite()) {
            throw new DadosInvalidosException("o valor da medição não é um número válido: " + valor);
        }
        this.valor = valor;
    }

    public LocalDateTime getDataHora() { return dataHora; }
}
