package br.com.territory.dominio;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.territory.excecao.DadosInvalidosException;

public class PrevisaoMediaMovelTest {

    @Test
    public void deveCalcularMediaDasUltimasCincoLeituras() {
        // Arrange: as duas primeiras leituras ficam FORA da janela de 5
        PrevisaoMediaMovel previsao = new PrevisaoMediaMovel(5);
        List<Double> historico = List.of(10.0, 10.0, 30.0, 32.0, 34.0, 36.0, 38.0);

        // Act
        double previsto = previsao.prever(historico);

        // Assert: (30 + 32 + 34 + 36 + 38) / 5 = 34
        assertEquals(34.0, previsto, 0.001);
    }

    @Test
    public void deveSeCalarComMenosDeCincoLeituras() {
        // Arrange
        PrevisaoMediaMovel previsao = new PrevisaoMediaMovel(5);
        List<Double> historico = List.of(30.0, 32.0, 34.0);

        // Act + Assert
        DadosInvalidosException exception = assertThrows(
                DadosInvalidosException.class,
                () -> previsao.prever(historico));

        assertTrue(exception.getMessage().contains("há apenas 3"));
    }

    @Test
    public void deveRecusarJanelaMenorQueOMinimo() {
        assertThrows(DadosInvalidosException.class, () -> new PrevisaoMediaMovel(2));
    }
}
