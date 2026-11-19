package br.com.territory.dominio;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

public class PrevisaoComparacaoTest {

    @Test
    public void deveProjetarATendenciaDeAlta() {
        // Arrange: período anterior com média 30, período atual com média 33
        PrevisaoComparacao previsao = new PrevisaoComparacao(3);
        List<Double> historico = List.of(29.0, 30.0, 31.0, 32.0, 33.0, 34.0);

        // Act
        double previsto = previsao.prever(historico);

        // Assert: subiu 3 graus de um período para o outro → 33 + 3 = 36
        assertEquals(36.0, previsto, 0.001);
    }
}
