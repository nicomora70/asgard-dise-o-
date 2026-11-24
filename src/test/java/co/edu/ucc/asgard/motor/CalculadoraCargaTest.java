package co.edu.ucc.asgard.motor;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.ucc.asgard.config.ParametrosMotor;
import co.edu.ucc.asgard.modelo.Ejercicio;
import co.edu.ucc.asgard.modelo.Prescripcion;
import co.edu.ucc.asgard.modelo.Serie;
import co.edu.ucc.asgard.modelo.enums.DecisionCarga;
import co.edu.ucc.asgard.modelo.enums.GrupoMuscular;
import co.edu.ucc.asgard.modelo.enums.Semaforo;
import co.edu.ucc.asgard.modelo.enums.TipoEjercicio;
import java.util.List;
import org.junit.jupiter.api.Test;

class CalculadoraCargaTest {

    private final CalculadoraCarga calculadora = new CalculadoraCarga(ParametrosMotor.porDefecto());
    private final Prescripcion prescripcion = new Prescripcion(1, "press", 3, 8, 12, 8, 50, 120);

    @Test
    void e1RMUsaLaFormulaDeEpley() {
        assertThat(calculadora.e1RM(100, 10)).isEqualTo(133.33);
        assertThat(calculadora.e1RM(100, 1)).isEqualTo(100);
        assertThat(calculadora.e1RM(100, 0)).isZero();
    }

    @Test
    void subeCuandoTodasLasSeriesLleganAlMaximoDentroDelRpe() {
        assertThat(calculadora.decidir(series(12, 12, 12), prescripcion, 0)).isEqualTo(DecisionCarga.SUBIR);
    }

    @Test
    void mantieneSiFaltanSeriesOSiElRpeSuperaElObjetivo() {
        assertThat(calculadora.decidir(series(12, 12), prescripcion, 0)).isEqualTo(DecisionCarga.MANTENER);
        List<Serie> duras = series(12, 12, 12);
        duras.get(2).setRpe(9.5);
        assertThat(calculadora.decidir(duras, prescripcion, 0)).isEqualTo(DecisionCarga.MANTENER);
        assertThat(calculadora.decidir(series(10, 9, 8), prescripcion, 0)).isEqualTo(DecisionCarga.MANTENER);
    }

    @Test
    void bajaSoloTrasDosSesionesSeguidasPorDebajoDelMinimo() {
        assertThat(calculadora.decidir(series(8, 7, 6), prescripcion, 0)).isEqualTo(DecisionCarga.MANTENER);
        assertThat(calculadora.decidir(series(8, 7, 6), prescripcion, 1)).isEqualTo(DecisionCarga.BAJAR);
    }

    @Test
    void semaforoVerdeAmarilloYRojo() {
        assertThat(calculadora.semaforo(List.of(100.0))).isEqualTo(Semaforo.VERDE);
        assertThat(calculadora.semaforo(List.of(100.0, 102.0, 104.0))).isEqualTo(Semaforo.VERDE);
        assertThat(calculadora.semaforo(List.of(100.0, 104.0, 103.0, 104.0, 102.0))).isEqualTo(Semaforo.AMARILLO);
        assertThat(calculadora.semaforo(List.of(100.0, 104.0, 95.0))).isEqualTo(Semaforo.ROJO);
    }

    @Test
    void siguienteCargaRedondeaAlDiscoMasCercano() {
        assertThat(calculadora.siguienteCarga(60, DecisionCarga.SUBIR, 2.5)).isEqualTo(62.5);
        assertThat(calculadora.siguienteCarga(62.5, DecisionCarga.DELOAD, 2.5)).isEqualTo(56.5);
        assertThat(calculadora.siguienteCarga(60, DecisionCarga.BAJAR, 2.5)).isEqualTo(57.0);
        assertThat(calculadora.siguienteCarga(60, DecisionCarga.MANTENER, 2.5)).isEqualTo(60.0);
    }

    @Test
    void incrementoSegunTipoYTren() {
        assertThat(calculadora.incremento(ejercicio(TipoEjercicio.COMPUESTO, GrupoMuscular.PECHO, 0))).isEqualTo(2.5);
        assertThat(calculadora.incremento(ejercicio(TipoEjercicio.COMPUESTO, GrupoMuscular.CUADRICEPS, 0)))
                .isEqualTo(5.0);
        assertThat(calculadora.incremento(ejercicio(TipoEjercicio.AISLAMIENTO, GrupoMuscular.BICEPS, 0)))
                .isEqualTo(1.0);
        assertThat(calculadora.incremento(ejercicio(TipoEjercicio.COMPUESTO, GrupoMuscular.ESPALDA, 2))).isEqualTo(2);
    }

    private static List<Serie> series(int... repeticiones) {
        List<Serie> series = new java.util.ArrayList<>();
        for (int reps : repeticiones) {
            Serie serie = new Serie();
            serie.setPeso(60);
            serie.setRepeticiones(reps);
            serie.setRpe(8.0);
            series.add(serie);
        }
        return series;
    }

    private static Ejercicio ejercicio(TipoEjercicio tipo, GrupoMuscular grupo, double incremento) {
        Ejercicio ejercicio = new Ejercicio();
        ejercicio.setTipo(tipo);
        ejercicio.setGrupoMuscular(grupo);
        ejercicio.setIncrementoKg(incremento);
        return ejercicio;
    }
}
