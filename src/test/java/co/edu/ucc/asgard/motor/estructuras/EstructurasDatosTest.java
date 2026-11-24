package co.edu.ucc.asgard.motor.estructuras;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.ucc.asgard.modelo.DiaRutina;
import co.edu.ucc.asgard.modelo.Prescripcion;
import co.edu.ucc.asgard.modelo.Rutina;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

class EstructurasDatosTest {

    // ---------- Matriz 2D ----------

    @Test
    void planSemanalUbicaCadaPrescripcionPorDiaYPosicion() {
        Rutina rutina = new Rutina();
        rutina.setDias(List.of(
                new DiaRutina(DayOfWeek.MONDAY, "Torso", List.of(
                        new Prescripcion(2, "remo", 3, 8, 12, 8, 40, 90),
                        new Prescripcion(1, "press", 3, 8, 12, 8, 50, 120))),
                new DiaRutina(DayOfWeek.THURSDAY, "Pierna", List.of(
                        new Prescripcion(1, "sentadilla", 4, 5, 8, 8, 60, 180)))));

        PlanSemanal plan = new PlanSemanal(rutina);

        assertThat(plan.obtener(DayOfWeek.MONDAY, 0)).get().extracting(Prescripcion::getEjercicioId).isEqualTo("press");
        assertThat(plan.obtener(DayOfWeek.MONDAY, 1)).get().extracting(Prescripcion::getEjercicioId).isEqualTo("remo");
        assertThat(plan.obtener(DayOfWeek.MONDAY, 5)).isEmpty();
        assertThat(plan.esDiaDeEntrenamiento(DayOfWeek.TUESDAY)).isFalse();
        assertThat(plan.nombreDe(DayOfWeek.THURSDAY)).isEqualTo("Pierna");
        assertThat(plan.ejercicios()).containsExactly("press", "remo", "sentadilla");
        assertThat(plan.buscarEjercicio("sentadilla")).get().extracting(Prescripcion::getSeries).isEqualTo(4);
    }

    @Test
    void planSemanalAmpliaLaFilaAlColocar() {
        PlanSemanal plan = new PlanSemanal(null);
        plan.colocar(DayOfWeek.FRIDAY, 2, new Prescripcion(3, "curl", 3, 10, 15, 8, 10, 60));

        assertThat(plan.delDia(DayOfWeek.FRIDAY)).hasSize(1);
        assertThat(plan.obtener(DayOfWeek.FRIDAY, 2)).isPresent();
    }

    // ---------- Tabla hash ----------

    @Test
    void tablaHistorialGuardaReemplazaYSeRedimensiona() {
        TablaHistorial<String, Integer> tabla = new TablaHistorial<>(4);
        for (int i = 0; i < 100; i++) {
            tabla.poner("ejercicio-" + i, i);
        }
        tabla.poner("ejercicio-7", 700);

        assertThat(tabla.tamano()).isEqualTo(100);
        assertThat(tabla.capacidad()).isGreaterThan(100);
        assertThat(tabla.obtener("ejercicio-7")).contains(700);
        assertThat(tabla.obtener("ejercicio-99")).contains(99);
        assertThat(tabla.obtener("no-existe")).isEmpty();
        assertThat(tabla.claves()).hasSize(100);
    }

    // ---------- Grafo ----------

    @Test
    void grafoEsNoDirigidoYElBfsEligeElVecinoMasParecido() {
        GrafoEjercicios grafo = new GrafoEjercicios();
        grafo.agregarArista("sentadilla", "prensa", 0.8);
        grafo.agregarArista("sentadilla", "zancada", 0.6);
        grafo.agregarArista("prensa", "extension", 0.5);

        assertThat(grafo.vecinos("prensa")).extracting(GrafoEjercicios.Arista::destino)
                .contains("sentadilla", "extension");
        assertThat(grafo.buscarSustituto("sentadilla", id -> true)).contains("prensa");
        assertThat(grafo.buscarSustituto("sentadilla", id -> !id.equals("prensa"))).contains("zancada");
    }

    @Test
    void grafoPasaAlSiguienteNivelSiNingunVecinoSirve() {
        GrafoEjercicios grafo = new GrafoEjercicios();
        grafo.agregarArista("sentadilla", "prensa", 0.8);
        grafo.agregarArista("prensa", "extension", 0.5);

        assertThat(grafo.buscarSustituto("sentadilla", id -> id.equals("extension"))).contains("extension");
        assertThat(grafo.buscarSustituto("sentadilla", id -> false)).isEmpty();
        assertThat(grafo.buscarSustituto("no-existe", id -> true)).isEmpty();
    }

    // ---------- Max-Heap ----------

    @Test
    void monticuloExtraeDeMayorAMenor() {
        MonticuloMaximo<Integer> monticulo = new MonticuloMaximo<>(Comparator.naturalOrder());
        List.of(5, 1, 9, 3, 7, 9, 2).forEach(monticulo::insertar);

        List<Integer> extraidos = new ArrayList<>();
        while (!monticulo.estaVacio()) {
            extraidos.add(monticulo.extraerMaximo().orElseThrow());
        }

        assertThat(extraidos).containsExactly(9, 9, 7, 5, 3, 2, 1);
        assertThat(monticulo.extraerMaximo()).isEmpty();
    }

    // ---------- Pila LIFO ----------

    @Test
    void pilaDeshacerEsLifoYDescartaLoMasAntiguoAlLlenarse() {
        PilaDeshacer pila = PilaDeshacer.desdeLista(List.of("s1", "s2", "s3"), 3);
        pila.apilar("s4");

        assertThat(pila.aLista()).containsExactly("s2", "s3", "s4");
        assertThat(pila.desapilar()).contains("s4");
        assertThat(pila.desapilar()).contains("s3");
        assertThat(pila.cima()).contains("s2");
        assertThat(pila.tamano()).isEqualTo(1);
        assertThatThrownBy(() -> new PilaDeshacer(0)).isInstanceOf(IllegalArgumentException.class);
    }

    // ---------- Cola FIFO ----------

    @Test
    void colaSesionesEsFifo() {
        ColaSesiones<String> cola = ColaSesiones.desdeLista(List.of("lunes", "martes"));
        cola.encolar("jueves");

        assertThat(cola.verFrente()).contains("lunes");
        assertThat(cola.desencolar()).contains("lunes");
        assertThat(cola.contiene("jueves")).isTrue();
        assertThat(cola.aLista()).containsExactly("martes", "jueves");
        cola.desencolar();
        cola.desencolar();
        assertThat(cola.estaVacia()).isTrue();
        assertThat(cola.desencolar()).isEmpty();
    }
}
