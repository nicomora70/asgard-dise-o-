package co.edu.ucc.asgard.repositorio;

import co.edu.ucc.asgard.modelo.Socio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SocioRepository extends MongoRepository<Socio, String> {

    Optional<Socio> findByDocumentoIdentidad(String documentoIdentidad);

    boolean existsByDocumentoIdentidad(String documentoIdentidad);

    long countByEntrenadorId(String entrenadorId);

    List<Socio> findByEntrenadorId(String entrenadorId);
}
