package co.edu.ucc.asgard.config;

import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Parametros de las sesiones y del planificador diario (prefijo "asgard.sesiones").
 *
 * @param zonaHoraria          zona horaria del gimnasio: define que dia es "hoy"
 * @param diasRevision         cuantos dias hacia atras se revisan para encolar sesiones no realizadas
 * @param planificadorHabilitado activa la tarea diaria del reloj del sistema
 */
@ConfigurationProperties(prefix = "asgard.sesiones")
public record ParametrosSesiones(String zonaHoraria, int diasRevision, boolean planificadorHabilitado) {

    public static ParametrosSesiones porDefecto() {
        return new ParametrosSesiones("America/Bogota", 14, true);
    }

    public ZoneId zona() {
        return ZoneId.of(zonaHoraria);
    }
}
