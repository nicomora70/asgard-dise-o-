package co.edu.ucc.asgard.motor;

import co.edu.ucc.asgard.config.ParametrosMotor;
import co.edu.ucc.asgard.modelo.Ejercicio;
import co.edu.ucc.asgard.modelo.Prescripcion;
import co.edu.ucc.asgard.modelo.Serie;
import co.edu.ucc.asgard.modelo.enums.DecisionCarga;
import co.edu.ucc.asgard.modelo.enums.Semaforo;
import co.edu.ucc.asgard.modelo.enums.TipoEjercicio;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Reglas de calculo del motor (RF08, RF09): 1RM estimado, doble progresion, semaforo e incrementos de carga.
 * No depende de la base de datos ni de Spring Web.
 */
@Component
public class CalculadoraCarga {

    private final ParametrosMotor parametros;

    public CalculadoraCarga(ParametrosMotor parametros) {
        this.parametros = parametros;
    }

    /** 1RM estimado con la formula de Epley: peso x (1 + reps / 30). Con 1 repeticion el 1RM es el mismo peso. */
    public double e1RM(double peso, int reps) {
        if (peso <= 0 || reps <= 0) {
            return 0;
        }
        double estimado = reps == 1 ? peso : peso * (1 + reps / 30.0);
        return Math.round(estimado * 100.0) / 100.0;
    }

    /**
     * Doble progresion (RF08) a partir de las series de la ultima sesion del ejercicio.
     *
     * @param series        series de la ultima sesion
     * @param prescripcion  series, rango de repeticiones y RPE objetivo
     * @param fallosPrevios sesiones seguidas, antes de la ultima, con alguna serie por debajo de repsMin
     */
    public DecisionCarga decidir(List<Serie> series, Prescripcion prescripcion, int fallosPrevios) {
        if (series == null || series.isEmpty()) {
            return DecisionCarga.MANTENER;
        }
        boolean algunaBajoMinimo = series.stream().anyMatch(s -> s.getRepeticiones() < prescripcion.getRepsMin());
        if (algunaBajoMinimo) {
            return fallosPrevios + 1 >= parametros.fallosParaBajar() ? DecisionCarga.BAJAR : DecisionCarga.MANTENER;
        }
        boolean todasEnMaximo = series.size() >= prescripcion.getSeries()
                && series.stream().allMatch(s -> s.getRepeticiones() >= prescripcion.getRepsMax()
                        && (s.getRpe() == null || s.getRpe() <= prescripcion.getRpeObjetivo()));
        return todasEnMaximo ? DecisionCarga.SUBIR : DecisionCarga.MANTENER;
    }

    /**
     * Semaforo de progreso (RF09) sobre el 1RM estimado de cada sesion, en orden cronologico.
     * <ul>
     * <li>ROJO: la ultima sesion cae mas del umbral de retroceso frente al mejor registro anterior.</li>
     * <li>AMARILLO: las ultimas N sesiones no superan el mejor registro previo a ellas (estancado).</li>
     * <li>VERDE: mejora o se mantiene.</li>
     * </ul>
     */
    public Semaforo semaforo(List<Double> historialE1RM) {
        int n = historialE1RM == null ? 0 : historialE1RM.size();
        if (n < 2) {
            return Semaforo.VERDE;
        }
        double ultimo = historialE1RM.get(n - 1);
        double mejorPrevio = maximo(historialE1RM, 0, n - 1);
        if (ultimo < mejorPrevio * (1 - parametros.umbralRetroceso())) {
            return Semaforo.ROJO;
        }
        int ventana = parametros.sesionesEstancamiento();
        if (n > ventana) {
            double mejorAntes = maximo(historialE1RM, 0, n - ventana);
            double mejorVentana = maximo(historialE1RM, n - ventana, n);
            if (mejorVentana <= mejorAntes) {
                return Semaforo.AMARILLO;
            }
        }
        return Semaforo.VERDE;
    }

    /** Carga de la siguiente sesion segun la decision, redondeada al disco mas cercano. */
    public double siguienteCarga(double actual, DecisionCarga decision, double incremento) {
        double carga = switch (decision) {
            case SUBIR -> actual + incremento;
            case BAJAR -> actual * (1 - parametros.porcentajeBajada());
            case DELOAD -> actual * (1 - parametros.porcentajeDeload());
            case MANTENER, SUSTITUIR -> actual;
        };
        return redondear(Math.max(0, carga));
    }

    /** Incremento propio del ejercicio o, si no tiene, el de su tipo y tren (superior o inferior). */
    public double incremento(Ejercicio ejercicio) {
        if (ejercicio.getIncrementoKg() > 0) {
            return ejercicio.getIncrementoKg();
        }
        if (ejercicio.getTipo() == TipoEjercicio.AISLAMIENTO) {
            return parametros.incrementoAislamiento();
        }
        return ejercicio.getGrupoMuscular() != null && ejercicio.getGrupoMuscular().isTrenInferior()
                ? parametros.incrementoInferior() : parametros.incrementoSuperior();
    }

    /** Factor de prioridad del semaforo (RF11). */
    public double factorSemaforo(Semaforo semaforo) {
        return switch (semaforo) {
            case ROJO -> parametros.factorRojo();
            case AMARILLO -> parametros.factorAmarillo();
            case VERDE -> 1.0;
        };
    }

    public double redondear(double carga) {
        double paso = parametros.redondeoKg();
        return paso <= 0 ? carga : Math.round(carga / paso) * paso;
    }

    private static double maximo(List<Double> valores, int desde, int hasta) {
        double maximo = Double.NEGATIVE_INFINITY;
        for (int i = desde; i < hasta; i++) {
            maximo = Math.max(maximo, valores.get(i));
        }
        return maximo;
    }
}
