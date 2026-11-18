package br.com.territory.excecao;

// Lançada quando um dado do território não faz sentido:
// medição fora da faixa física, população negativa, limiar inválido...
public class DadosInvalidosException extends RuntimeException {

    public DadosInvalidosException(String motivo) {
        super("Dados inválidos: " + motivo); // getMessage()
    }
}
