package br.com.territory.dominio;

import br.com.territory.excecao.DadosInvalidosException;

// Classe ABSTRATA: não existe um "componente genérico" no território.
// Existe um transformador, um poste, uma subestação... (as classes filhas).
public abstract class ComponenteTerritorial {

    // 1. Atributos sempre privados (Encapsulamento)
    private String id;
    private String nome;
    private String regiao;
    private String status;

    // 2. Construtor: todo componente nasce com id, nome e região
    public ComponenteTerritorial(String id, String nome, String regiao) {
        this.setId(id);
        this.setNome(nome);
        this.regiao = regiao;
        this.status = "NÃO VERIFICADO";
    }

    // 3. MÉTODO ABSTRATO: cada filha decide como verifica o próprio status
    public abstract String verificarStatus();

    // 4. Getters (apenas leitura)
    public String getId() {
        return this.id;
    }

    public String getNome() {
        return this.nome;
    }

    public String getRegiao() {
        return this.regiao;
    }

    public String getStatus() {
        return this.status;
    }

    // 5. Só as filhas atualizam o status, depois de verificar
    protected void atualizarStatus(String novoStatus) {
        this.status = novoStatus;
    }

    // 6. Setters privados (a engrenagem interna de validação)
    private void setId(String id) {
        if (id == null || id.isBlank()) {
            throw new DadosInvalidosException("o componente precisa de um id");
        }
        this.id = id;
    }

    private void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("o componente precisa de um nome");
        }
        this.nome = nome;
    }
}
