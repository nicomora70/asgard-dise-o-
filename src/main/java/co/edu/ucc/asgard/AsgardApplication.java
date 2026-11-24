package co.edu.ucc.asgard;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Punto de entrada del backend de Asgard. */
@SpringBootApplication
@ConfigurationPropertiesScan
public class AsgardApplication {

    public static void main(String[] args) {
        // Las fechas se guardan en UTC (RNF-003); la zona del gimnasio se aplica de forma explicita en los servicios.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(AsgardApplication.class, args);
    }
}
