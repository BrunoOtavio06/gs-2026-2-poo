package br.com.territory.dominio;

import java.util.ArrayList;
import java.util.List;

import br.com.territory.excecao.DadosInvalidosException;

// Um Transformador É UM ComponenteTerritorial (herança)
// e TEM sensores instalados nele (associação).
public class Transformador extends ComponenteTerritorial {

    private int capacidadeKva;
    private List<Sensor> sensoresInstalados;

    public Transformador(String id, String regiao, int capacidadeKva) {
        super(id, "Transformador de distribuição", regiao); // a mãe resolve id, nome e região
        this.setCapacidadeKva(capacidadeKva);
        this.sensoresInstalados = new ArrayList<>();
    }

    public void instalarSensor(Sensor sensor) {
        this.sensoresInstalados.add(sensor);
    }

    // O transformador está bem quando todos os sensores dele respondem
    @Override
    public String verificarStatus() {
        int sensoresComFalha = 0;
        for (Sensor sensor : this.sensoresInstalados) {
            if (!sensor.verificarFuncionamento()) {
                sensoresComFalha++;
            }
        }

        if (sensoresComFalha == 0) {
            this.atualizarStatus("OPERANDO");
        } else {
            this.atualizarStatus("OPERANDO COM " + sensoresComFalha + " SENSOR(ES) SEM RESPOSTA");
        }
        return String.format("%s %s (%s, %d kVA): %s",
                this.getNome(), this.getId(), this.getRegiao(), this.capacidadeKva, this.getStatus());
    }

    public int getCapacidadeKva() {
        return this.capacidadeKva;
    }

    public List<Sensor> getSensoresInstalados() {
        return this.sensoresInstalados;
    }

    private void setCapacidadeKva(int capacidadeKva) {
        if (capacidadeKva <= 0) {
            throw new DadosInvalidosException("a capacidade do transformador deve ser positiva: " + capacidadeKva);
        }
        this.capacidadeKva = capacidadeKva;
    }
}
