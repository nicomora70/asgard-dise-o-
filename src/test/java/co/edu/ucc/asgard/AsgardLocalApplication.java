package co.edu.ucc.asgard;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import java.net.InetSocketAddress;
import org.springframework.boot.SpringApplication;

/**
 * Arranca la API con un MongoDB en memoria, sin instalar MongoDB: {@code ./mvnw spring-boot:test-run}.
 * Los datos se pierden al detener la aplicacion. Para trabajar con datos persistentes use
 * {@code ./mvnw spring-boot:run} con MongoDB local o la variable MONGODB_URI.
 */
public final class AsgardLocalApplication {

    private AsgardLocalApplication() {
    }

    public static void main(String[] args) {
        MongoServer mongo = new MongoServer(new MemoryBackend());
        InetSocketAddress direccion = mongo.bind();
        String uri = "mongodb://localhost:" + direccion.getPort() + "/asgard";
        System.setProperty("spring.mongodb.uri", uri);
        System.setProperty("spring.data.mongodb.uri", uri);
        // El servidor en memoria no implementa el comando "hello" del indicador de salud de MongoDB
        System.setProperty("management.health.mongodb.enabled", "false");
        Runtime.getRuntime().addShutdownHook(new Thread(mongo::shutdown));
        SpringApplication.from(AsgardApplication::main).run(args);
    }
}
