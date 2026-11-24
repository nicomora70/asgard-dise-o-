package co.edu.ucc.asgard.repositorio;

import co.edu.ucc.asgard.modelo.Serie;
import java.time.Instant;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SerieRepository extends MongoRepository<Serie, String> {

    /** Historial activo del socio (sin las series deshechas), del mas antiguo al mas reciente. */
    List<Serie> findBySocioIdAndDeshechaFalseOrderByFechaHoraAsc(String socioId);

    List<Serie> findBySocioIdAndDeshechaFalseAndFechaHoraBetweenOrderByFechaHoraDesc(String socioId, Instant desde,
            Instant hasta);

    long countBySesionIdAndEjercicioIdAndDeshechaFalse(String sesionId, String ejercicioId);

    void deleteBySocioId(String socioId);
}
