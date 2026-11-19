package br.com.territory.dominio;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import br.com.territory.excecao.DadosInvalidosException;

public class MedicaoTest {

    @Test
    public void deveCriarMedicaoValida() {
        // Arrange + Act
        Medicao medicao = new Medicao("TEMP-AR-01", SensorTemperaturaAr.TIPO, 36.5);

        // Assert
        assertEquals("TEMP-AR-01", medicao.getCodigoSensor());
        assertEquals(36.5, medicao.getValor(), 0.001);
        assertNotNull(medicao.getDataHora());
    }

    @Test
    public void deveLancarExcecaoQuandoCodigoSensorVazio() {
        // Act + Assert
        DadosInvalidosException exception = assertThrows(
                DadosInvalidosException.class,
                () -> new Medicao("", SensorTemperaturaAr.TIPO, 36.5));

        assertEquals("Dados inválidos: a medição precisa do código do sensor", exception.getMessage());
    }

    @Test
    public void deveLancarExcecaoQuandoValorNaoEhNumero() {
        assertThrows(
                DadosInvalidosException.class,
                () -> new Medicao("TEMP-AR-01", SensorTemperaturaAr.TIPO, Double.NaN));
    }
}
