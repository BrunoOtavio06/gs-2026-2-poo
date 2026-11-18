package br.com.territory.excecao;

// Lançada quando um sensor (ou o gerador de dados) não consegue entregar a leitura.
public class FonteIndisponivelException extends RuntimeException {

    private String codigoFonte;

    public FonteIndisponivelException(String codigoFonte, String motivo) {
        super(String.format("Fonte '%s' indisponível: %s", codigoFonte, motivo)); // getMessage()
        this.codigoFonte = codigoFonte;
    }

    public String getCodigoFonte() {
        return this.codigoFonte;
    }
}
