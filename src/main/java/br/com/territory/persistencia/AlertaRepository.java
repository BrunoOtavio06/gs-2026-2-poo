package br.com.territory.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.territory.dominio.Alerta;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    // Alertas já emitidos, do mais recente para o mais antigo
    List<Alerta> findAllByOrderByDataHoraDesc();
}
