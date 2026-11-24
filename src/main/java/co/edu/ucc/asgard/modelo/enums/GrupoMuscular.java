package co.edu.ucc.asgard.modelo.enums;

/** Grupo muscular que trabaja un ejercicio. */
public enum GrupoMuscular {
    PECHO(false),
    ESPALDA(false),
    HOMBROS(false),
    BICEPS(false),
    TRICEPS(false),
    CUADRICEPS(true),
    ISQUIOTIBIALES(true),
    GLUTEOS(true),
    PANTORRILLAS(true),
    CORE(false);

    private final boolean trenInferior;

    GrupoMuscular(boolean trenInferior) {
        this.trenInferior = trenInferior;
    }

    /** Los ejercicios compuestos de tren inferior suben la carga con un incremento mayor (RF08). */
    public boolean isTrenInferior() {
        return trenInferior;
    }
}
