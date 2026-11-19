package br.com.territory.servico;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.territory.dominio.Alerta;
import br.com.territory.dominio.Medicao;
import br.com.territory.dominio.PrevisaoMediaMovel;
import br.com.territory.dominio.Sensor;
import br.com.territory.dominio.SensorTemperaturaAr;
import br.com.territory.excecao.DadosInvalidosException;
import br.com.territory.excecao.FonteIndisponivelException;
import br.com.territory.persistencia.AlertaRepository;
import br.com.territory.persistencia.MedicaoRepository;

// Teste unitário: NÃO sobe o Spring e NÃO usa o banco H2.
// @Mock cria repositories falsos; @InjectMocks injeta esses falsos no serviço.
@ExtendWith(MockitoExtension.class)
public class ServicoMonitoramentoTest {

    @Mock
    private MedicaoRepository medicaoRepository;

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private Sensor sensorFalso; // dublê de sensor: devolve o que o teste mandar

    @InjectMocks
    private ServicoMonitoramento servico;

    private Sensor termometro;

    @BeforeEach
    public void setUp() {
        termometro = new SensorTemperaturaAr("TEMP-AR-01"); // limiar padrão: 35 °C
    }

    // Monta um histórico com 5 leituras iguais
    private List<Medicao> historicoCom(double valor) {
        List<Medicao> historico = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            historico.add(new Medicao("TEMP-AR-01", SensorTemperaturaAr.TIPO, valor));
        }
        return historico;
    }

    @Test
    public void deveEmitirAlertaCriticoQuandoPrevisaoPassaVintePorCentoDoLimite() {
        // Arrange: média de 43 °C, acima de 35 × 1,20 = 42 °C
        when(medicaoRepository.findByCodigoSensorOrderByDataHoraAsc("TEMP-AR-01")).thenReturn(historicoCom(43.0));

        // Act
        String resultado = servico.preverEAvaliar(termometro, new PrevisaoMediaMovel(5));

        // Assert
        assertTrue(resultado.contains("CRÍTICO"));
        verify(alertaRepository).save(any(Alerta.class));
    }

    @Test
    public void naoDeveEmitirAlertaQuandoPrevisaoFicaAbaixoDoLimite() {
        // Arrange: média de 30 °C, abaixo do limite de 35 °C
        when(medicaoRepository.findByCodigoSensorOrderByDataHoraAsc("TEMP-AR-01")).thenReturn(historicoCom(30.0));

        // Act
        String resultado = servico.preverEAvaliar(termometro, new PrevisaoMediaMovel(5));

        // Assert
        assertTrue(resultado.contains("dentro do limite"));
        verify(alertaRepository, never()).save(any());
    }

    @Test
    public void deveGravarLeituraValidaDoSensor() {
        // Arrange
        when(sensorFalso.lerValor()).thenReturn(31.2);
        when(sensorFalso.getCodigo()).thenReturn("TEMP-AR-01");
        when(sensorFalso.retornarTipo()).thenReturn(SensorTemperaturaAr.TIPO);

        // Act
        servico.registrarLeitura(sensorFalso);

        // Assert: a medição foi entregue ao repository para ser gravada
        verify(medicaoRepository).save(any(Medicao.class));
    }

    @Test
    public void naoDeveGravarNadaQuandoSensorFalha() {
        // Arrange
        when(sensorFalso.lerValor()).thenThrow(new FonteIndisponivelException("TEMP-AR-01", "sem sinal"));

        // Act + Assert
        assertThrows(FonteIndisponivelException.class, () -> servico.registrarLeitura(sensorFalso));
        verifyNoInteractions(medicaoRepository);
    }

    @Test
    public void deveRecusarAlteracaoDeLimiarComCodigoErrado() {
        DadosInvalidosException exception = assertThrows(
                DadosInvalidosException.class,
                () -> servico.alterarLimiar("senha-errada", SensorTemperaturaAr.TIPO, 30.0));

        assertTrue(exception.getMessage().contains("código de acesso incorreto"));
        assertEquals(35.0, ConfiguracaoSistema.getInstancia().buscarLimiar(SensorTemperaturaAr.TIPO), 0.001);
    }
}
