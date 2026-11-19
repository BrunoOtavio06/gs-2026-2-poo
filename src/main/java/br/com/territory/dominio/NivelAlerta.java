package br.com.territory.dominio;

// Os três níveis de alerta, do mais leve ao mais grave.
// Cada nível sabe a partir de quanto do limite ele vale e o que a equipe deve fazer.
public enum NivelAlerta {

    ATENCAO("ATENÇÃO", 1.00, "Acompanhar as próximas leituras e avisar a equipe de plantão."),
    ALERTA("ALERTA", 1.10, "Abrir os pontos de resfriamento e pedir que a população evite ligar aparelhos de alto consumo no pico."),
    CRITICO("CRÍTICO", 1.20, "Acionar a Defesa Civil e a concessionária de energia e visitar primeiro idosos e crianças.");

    private String descricao;
    private double fatorLimite; // 1.10 = o valor previsto passou 10% do limite
    private String orientacao;

    NivelAlerta(String descricao, double fatorLimite, String orientacao) {
        this.descricao = descricao;
        this.fatorLimite = fatorLimite;
        this.orientacao = orientacao;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public double getFatorLimite() {
        return this.fatorLimite;
    }

    public String getOrientacao() {
        return this.orientacao;
    }
}
