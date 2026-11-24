package co.edu.ucc.asgard.motor.estructuras;

import co.edu.ucc.asgard.modelo.DiaRutina;
import co.edu.ucc.asgard.modelo.Prescripcion;
import co.edu.ucc.asgard.modelo.Rutina;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Matriz 2D de la rutina (RF03): fila = dia de la semana (0 lunes ... 6 domingo), columna = posicion del ejercicio
 * en ese dia. El acceso a una celda es O(1).
 */
public class PlanSemanal {

    private static final int DIAS = 7;

    private final Prescripcion[][] celdas;
    private final String[] nombres;

    public PlanSemanal(Rutina rutina) {
        celdas = new Prescripcion[DIAS][];
        nombres = new String[DIAS];
        for (int i = 0; i < DIAS; i++) {
            celdas[i] = new Prescripcion[0];
        }
        if (rutina == null) {
            return;
        }
        for (DiaRutina dia : rutina.getDias()) {
            List<Prescripcion> ordenadas = new ArrayList<>(dia.getPrescripciones());
            ordenadas.sort(Comparator.comparingInt(Prescripcion::getOrden));
            int fila = fila(dia.getDia());
            celdas[fila] = ordenadas.toArray(new Prescripcion[0]);
            nombres[fila] = dia.getNombre();
        }
    }

    /** Prescripcion de una celda; vacio si la posicion no existe. */
    public Optional<Prescripcion> obtener(DayOfWeek dia, int posicion) {
        Prescripcion[] fila = celdas[fila(dia)];
        return posicion >= 0 && posicion < fila.length ? Optional.of(fila[posicion]) : Optional.empty();
    }

    /** Coloca una prescripcion en una celda, ampliando la fila si hace falta. */
    public void colocar(DayOfWeek dia, int posicion, Prescripcion prescripcion) {
        int f = fila(dia);
        if (posicion < 0) {
            throw new IndexOutOfBoundsException("Posicion negativa: " + posicion);
        }
        if (posicion >= celdas[f].length) {
            Prescripcion[] ampliada = new Prescripcion[posicion + 1];
            System.arraycopy(celdas[f], 0, ampliada, 0, celdas[f].length);
            celdas[f] = ampliada;
        }
        celdas[f][posicion] = prescripcion;
    }

    /** Todas las prescripciones de un dia, en orden. */
    public List<Prescripcion> delDia(DayOfWeek dia) {
        List<Prescripcion> resultado = new ArrayList<>();
        for (Prescripcion p : celdas[fila(dia)]) {
            if (p != null) {
                resultado.add(p);
            }
        }
        return Collections.unmodifiableList(resultado);
    }

    public boolean esDiaDeEntrenamiento(DayOfWeek dia) {
        return !delDia(dia).isEmpty();
    }

    public String nombreDe(DayOfWeek dia) {
        return nombres[fila(dia)];
    }

    /** Primera prescripcion del ejercicio en la semana (recorre la matriz fila por fila). */
    public Optional<Prescripcion> buscarEjercicio(String ejercicioId) {
        for (Prescripcion[] fila : celdas) {
            for (Prescripcion p : fila) {
                if (p != null && p.getEjercicioId().equals(ejercicioId)) {
                    return Optional.of(p);
                }
            }
        }
        return Optional.empty();
    }

    /** Ids de los ejercicios distintos que tiene la rutina. */
    public List<String> ejercicios() {
        List<String> ids = new ArrayList<>();
        for (Prescripcion[] fila : celdas) {
            for (Prescripcion p : fila) {
                if (p != null && !ids.contains(p.getEjercicioId())) {
                    ids.add(p.getEjercicioId());
                }
            }
        }
        return ids;
    }

    private static int fila(DayOfWeek dia) {
        return dia.getValue() - 1;
    }
}
