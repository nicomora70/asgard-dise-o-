package co.edu.ucc.asgard.servicio;

import co.edu.ucc.asgard.config.ParametrosSesiones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Reloj del sistema (actor temporal): cada dia a medianoche manda a la cola las sesiones que los socios no
 * realizaron (RF04). La consulta de la sesion del dia hace la misma revision, asi que el sistema funciona aunque el
 * servicio haya estado suspendido.
 */
@Component
public class PlanificadorSesiones {

    private static final Logger LOG = LoggerFactory.getLogger(PlanificadorSesiones.class);

    private final SesionService sesionService;
    private final ParametrosSesiones parametros;

    public PlanificadorSesiones(SesionService sesionService, ParametrosSesiones parametros) {
        this.sesionService = sesionService;
        this.parametros = parametros;
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "${asgard.sesiones.zona-horaria:America/Bogota}")
    public void revisarSesionesDelDia() {
        if (!parametros.planificadorHabilitado()) {
            return;
        }
        int revisados = sesionService.encolarPendientesTodos();
        LOG.info("Planificador de sesiones: {} socios revisados", revisados);
    }
}
