package co.edu.ucc.asgard.repositorio;

import co.edu.ucc.asgard.modelo.Entrenador;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EntrenadorRepository extends MongoRepository<Entrenador, String> {

    boolean existsByDocumentoIdentidad(String documentoIdentidad);
}
