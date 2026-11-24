package co.edu.ucc.asgard.repositorio;

import co.edu.ucc.asgard.modelo.Rutina;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RutinaRepository extends MongoRepository<Rutina, String> {

    Optional<Rutina> findFirstBySocioIdAndActivaTrue(String socioId);

    List<Rutina> findBySocioIdOrderByFechaCreacionDesc(String socioId);

    List<Rutina> findByActivaTrue();

    void deleteBySocioId(String socioId);
}
