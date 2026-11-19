package br.com.territory.servico;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.territory.dominio.Alerta;
import br.com.territory.dominio.DadosTerritorio;
import br.com.territory.dominio.EstrategiaPrevisao;
import br.com.territory.dominio.Medicao;
import br.com.territory.dominio.NivelAlerta;
import br.com.territory.dominio.Sensor;
import br.com.territory.dominio.SensorCargaRede;
import br.com.territory.dominio.SensorTemperaturaAr;
import br.com.territory.dominio.SensorTemperaturaOleo;
import br.com.territory.dominio.Transformador;
import br.com.territory.excecao.DadosInvalidosException;
import br.com.territory.persistencia.AlertaRepository;
import br.com.territory.persistencia.MedicaoRepository;

@Service
public class ServicoMonitoramento {

    @Autowired
    private MedicaoRepository medicaoRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private FabricaPrevisao fabricaPrevisao;

    // Território monitorado (bairro fictício) e o transformador que abastece a região
    private DadosTerritorio territorio = this.montarTerritorio();
    private Transformador transformador = this.montarTransformador();

    // --- Leituras ---

    // Lê o sensor e grava a medição. Se o sensor falhar ou mandar valor inválido,
    // a exceção sobe para o menu e NADA é gravado.
    public Medicao registrarLeitura(Sensor sensor) {
        double valor = sensor.lerValor();
        Medicao medicao = new Medicao(sensor.getCodigo(), sensor.retornarTipo(), valor);
        return this.medicaoRepository.save(medicao);
    }

    // --- Previsões e alertas ---

    // Roda uma estratégia sobre o histórico do banco, compara com o limiar
    // e grava um alerta quando o limite é ultrapassado. Devolve a explicação.
    public String preverEAvaliar(Sensor sensor, EstrategiaPrevisao estrategia) {
        List<Double> historico = this.buscarValoresPorSensor(sensor.getCodigo());
        double previsto = estrategia.prever(historico); // polimorfismo: cada filha calcula do seu jeito
        double limite = ConfiguracaoSistema.getInstancia().buscarLimiar(sensor.retornarTipo());
        String unidade = sensor.retornarUnidade();

        String resultado = String.format("%s | %s (%s): %.1f %s previsto, limite %.1f %s",
                sensor.getCodigo(), estrategia.getNome(), estrategia.descreverCalculo(),
                previsto, unidade, limite, unidade);

        NivelAlerta nivel = this.classificarNivel(previsto, limite);
        if (nivel == null) {
            return resultado + " → dentro do limite, nenhum alerta.";
        }

        String motivo = String.format("%s (%s): previsão de %.1f %s pela %s passa do limite de %.1f %s.",
                sensor.retornarTipo(), sensor.getCodigo(), previsto, unidade,
                estrategia.descreverCalculo(), limite, unidade);
        this.alertaRepository.save(new Alerta(sensor.getCodigo(), nivel, motivo));

        return resultado + " → " + nivel.getDescricao() + " emitido. " + nivel.getOrientacao();
    }

    // Percorre os níveis do mais leve ao mais grave e fica com o último que o valor alcançou
    private NivelAlerta classificarNivel(double previsto, double limite) {
        NivelAlerta nivelAlcancado = null;
        for (NivelAlerta nivel : NivelAlerta.values()) {
            if (previsto >= limite * nivel.getFatorLimite()) {
                nivelAlcancado = nivel;
            }
        }
        return nivelAlcancado;
    }

    // --- Histórico ---

    public List<Medicao> buscarHistoricoPorSensor(String codigoSensor) {
        return this.medicaoRepository.findByCodigoSensorOrderByDataHoraAsc(codigoSensor);
    }

    public long contarLeituras(String codigoSensor) {
        return this.medicaoRepository.countByCodigoSensor(codigoSensor);
    }

    public List<Alerta> listarAlertas() {
        return this.alertaRepository.findAllByOrderByDataHoraDesc();
    }

    private List<Double> buscarValoresPorSensor(String codigoSensor) {
        List<Double> valores = new ArrayList<>();
        for (Medicao medicao : this.buscarHistoricoPorSensor(codigoSensor)) {
            valores.add(medicao.getValor());
        }
        return valores;
    }

    // --- Território e limiares ---

    // Só quem sabe o código de acesso altera os limiares de alerta
    public void alterarLimiar(String codigoAcesso, String tipoSensor, double novoLimite) {
        if (!this.territorio.validarCodigoAcesso(codigoAcesso)) {
            throw new DadosInvalidosException("código de acesso incorreto; os limiares não foram alterados");
        }
        ConfiguracaoSistema.getInstancia().definirLimiar(tipoSensor, novoLimite);
    }

    public List<EstrategiaPrevisao> getEstrategias() {
        return this.fabricaPrevisao.criarTodas();
    }

    public List<Sensor> getSensores() {
        return this.transformador.getSensoresInstalados();
    }

    public DadosTerritorio getTerritorio() {
        return this.territorio;
    }

    public Transformador getTransformador() {
        return this.transformador;
    }

    private DadosTerritorio montarTerritorio() {
        DadosTerritorio dados = new DadosTerritorio("Zona Leste - Setor Jardim Aurora (fictício)", 48000, "GS2026");
        dados.setPopulacaoVulneravel(9600); // cerca de 20%: idosos e crianças até 5 anos
        return dados;
    }

    private Transformador montarTransformador() {
        Transformador trafo = new Transformador("TRAFO-ZL-01", this.territorio.getRegiao(), 500);
        trafo.instalarSensor(new SensorTemperaturaAr("TEMP-AR-01"));
        trafo.instalarSensor(new SensorCargaRede("CARGA-01"));
        trafo.instalarSensor(new SensorTemperaturaOleo("OLEO-01"));
        return trafo;
    }
}
