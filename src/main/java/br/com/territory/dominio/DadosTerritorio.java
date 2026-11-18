package br.com.territory.dominio;

import br.com.territory.excecao.DadosInvalidosException;

// Dados da região monitorada. Descreve o território, nunca pessoas:
// só números agregados (população), sem nomes, CPFs ou endereços.
public class DadosTerritorio {

    // 1. Atributos privados
    private String regiao;
    private int populacao;
    private int populacaoVulneravel; // idosos e crianças pequenas, os mais afetados pelo calor
    private String codigoAcesso;     // exigido para alterar os limiares de alerta

    // 2. Construtor
    public DadosTerritorio(String regiao, int populacao, String codigoAcesso) {
        this.regiao = regiao;
        this.setPopulacao(populacao);
        this.codigoAcesso = codigoAcesso;
    }

    // 3. Comportamento: confere o código sem nunca expor o código guardado
    // (por isso não existe getCodigoAcesso())
    public boolean validarCodigoAcesso(String codigoInformado) {
        return this.codigoAcesso.equals(codigoInformado);
    }

    // 4. Getters
    public String getRegiao() {
        return this.regiao;
    }

    public int getPopulacao() {
        return this.populacao;
    }

    public int getPopulacaoVulneravel() {
        return this.populacaoVulneravel;
    }

    // 5. Setters com validação
    public void setPopulacao(int populacao) {
        if (populacao < 0) {
            throw new DadosInvalidosException("a população não pode ser negativa: " + populacao);
        }
        if (populacao < this.populacaoVulneravel) {
            throw new DadosInvalidosException("a população não pode ser menor que a população vulnerável ("
                    + this.populacaoVulneravel + ")");
        }
        this.populacao = populacao;
    }

    public void setPopulacaoVulneravel(int populacaoVulneravel) {
        if (populacaoVulneravel < 0 || populacaoVulneravel > this.populacao) {
            throw new DadosInvalidosException(String.format(
                    "a população vulnerável deve ficar entre 0 e %d: %d", this.populacao, populacaoVulneravel));
        }
        this.populacaoVulneravel = populacaoVulneravel;
    }
}
