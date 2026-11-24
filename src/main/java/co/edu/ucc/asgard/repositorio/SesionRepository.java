package co.edu.ucc.asgard.repositorio;

import co.edu.ucc.asgard.modelo.Sesion;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SesionRepository extends MongoRepository<Sesion, String> {

    Optional<Sesion> findFirstBySocioIdAndFechaProgramada(String socioId, LocalDate fechaProgramada);

    List<Sesion> findBySocioIdOrderByFechaProgramadaDesc(String socioId);

    void deleteBySocioId(String socioId);
}
