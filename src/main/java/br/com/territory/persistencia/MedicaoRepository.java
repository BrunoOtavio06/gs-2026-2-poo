package br.com.territory.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.territory.dominio.Medicao;

@Repository
public interface MedicaoRepository extends JpaRepository<Medicao, Long> {
    // Já tem: save(), findAll(), count()...

    // Histórico de um sensor, da leitura mais antiga para a mais recente
    // SELECT * FROM medicoes WHERE codigo_sensor = ? ORDER BY data_hora ASC
    List<Medicao> findByCodigoSensorOrderByDataHoraAsc(String codigoSensor);

    long countByCodigoSensor(String codigoSensor);
}
