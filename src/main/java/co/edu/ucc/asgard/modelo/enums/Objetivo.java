package co.edu.ucc.asgard.modelo.enums;

/**
 * Objetivo de entrenamiento del socio. Define el rango de repeticiones por defecto cuando un ejercicio no tiene
 * prescripcion en la rutina.
 */
public enum Objetivo {
    FUERZA(3, 6),
    HIPERTROFIA(8, 12),
    RESISTENCIA(12, 20);

    private final int repsMin;
    private final int repsMax;

    Objetivo(int repsMin, int repsMax) {
        this.repsMin = repsMin;
        this.repsMax = repsMax;
    }

    public int getRepsMin() {
        return repsMin;
    }

    public int getRepsMax() {
        return repsMax;
    }
}
