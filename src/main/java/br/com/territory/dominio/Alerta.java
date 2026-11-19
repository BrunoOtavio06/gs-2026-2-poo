package br.com.territory.dominio;

import java.time.LocalDateTime;

import br.com.territory.excecao.DadosInvalidosException;
import jakarta.persistence.*;

// @Entity: cada alerta emitido fica guardado na tabela "alertas"
@Entity
@Table(name = "alertas")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigoSensor;

    // Grava o nome do nível ("CRITICO") em vez da posição (2) no enum
    @Enumerated(EnumType.STRING)
    private NivelAlerta nivel;

    // O porquê do alerta: sensor, valor previsto e limite estourado
    @Column(length = 500)
    private String motivo;

    private LocalDateTime dataHora;

    public Alerta() {}

    public Alerta(String codigoSensor, NivelAlerta nivel, String motivo) {
        this.setCodigoSensor(codigoSensor);
        this.setNivel(nivel);
        this.setMotivo(motivo);
        this.dataHora = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }

    public String getCodigoSensor() { return codigoSensor; }

    public void setCodigoSensor(String codigoSensor) {
        if (codigoSensor == null || codigoSensor.isBlank()) {
            throw new DadosInvalidosException("o alerta precisa do código do sensor");
        }
        this.codigoSensor = codigoSensor;
    }

    public NivelAlerta getNivel() { return nivel; }

    public void setNivel(NivelAlerta nivel) {
        if (nivel == null) {
            throw new DadosInvalidosException("o alerta precisa de um nível");
        }
        this.nivel = nivel;
    }

    public String getMotivo() { return motivo; }

    public void setMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new DadosInvalidosException("todo alerta precisa explicar o motivo");
        }
        this.motivo = motivo;
    }

    public LocalDateTime getDataHora() { return dataHora; }
}
