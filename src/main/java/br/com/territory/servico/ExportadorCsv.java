package br.com.territory.servico;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import br.com.territory.dominio.Medicao;

// Bônus: exporta o histórico consultado para um arquivo CSV (abre no Excel)
public class ExportadorCsv {

    public String exportar(String codigoSensor, List<Medicao> historico) throws IOException {
        String nomeArquivo = "historico-" + codigoSensor + ".csv";

        // try-with-resources: o arquivo é fechado mesmo se der erro no meio
        try (PrintWriter arquivo = new PrintWriter(nomeArquivo, "UTF-8")) {
            arquivo.println("data_hora;codigo_sensor;tipo_sensor;valor");
            for (Medicao medicao : historico) {
                arquivo.println(medicao.getDataHora() + ";" + medicao.getCodigoSensor() + ";"
                        + medicao.getTipoSensor() + ";" + medicao.getValor());
            }
        }
        return nomeArquivo;
    }
}
