package br.com.territory.dominio;

// Contrato de comportamento: tudo que é Sensor sabe ler um valor,
// dizer se está funcionando e informar o próprio tipo.
public interface Sensor {

    // Constante de interface (public static final implícito):
    // em 5% das consultas o sensor simula uma falha de comunicação
    double CHANCE_FALHA = 0.05;

    double lerValor();

    boolean verificarFuncionamento();

    String retornarTipo();

    String retornarUnidade();

    String getCodigo();
}
