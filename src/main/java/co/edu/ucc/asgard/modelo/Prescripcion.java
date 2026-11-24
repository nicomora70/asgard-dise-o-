package co.edu.ucc.asgard.modelo;

/**
 * Lo que la rutina indica para un ejercicio en un dia: series, rango de repeticiones, RPE objetivo y carga inicial.
 * Se guarda embebida en {@link DiaRutina}.
 */
public class Prescripcion {

    private int orden;
    private String ejercicioId;
    private int series;
    private int repsMin;
    private int repsMax;
    private double rpeObjetivo;
    /** Carga con la que se empieza (kg) cuando aun no hay historial. */
    private double cargaInicial;
    private int descansoSegundos;

    public Prescripcion() {
    }

    public Prescripcion(int orden, String ejercicioId, int series, int repsMin, int repsMax, double rpeObjetivo,
            double cargaInicial, int descansoSegundos) {
        this.orden = orden;
        this.ejercicioId = ejercicioId;
        this.series = series;
        this.repsMin = repsMin;
        this.repsMax = repsMax;
        this.rpeObjetivo = rpeObjetivo;
        this.cargaInicial = cargaInicial;
        this.descansoSegundos = descansoSegundos;
    }

    /** Indica si un numero de repeticiones esta dentro del rango prescrito. */
    public boolean enRango(int reps) {
        return reps >= repsMin && reps <= repsMax;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public String getEjercicioId() {
        return ejercicioId;
    }

    public void setEjercicioId(String ejercicioId) {
        this.ejercicioId = ejercicioId;
    }

    public int getSeries() {
        return series;
    }

    public void setSeries(int series) {
        this.series = series;
    }

    public int getRepsMin() {
        return repsMin;
    }

    public void setRepsMin(int repsMin) {
        this.repsMin = repsMin;
    }

    public int getRepsMax() {
        return repsMax;
    }

    public void setRepsMax(int repsMax) {
        this.repsMax = repsMax;
    }

    public double getRpeObjetivo() {
        return rpeObjetivo;
    }

    public void setRpeObjetivo(double rpeObjetivo) {
        this.rpeObjetivo = rpeObjetivo;
    }

    public double getCargaInicial() {
        return cargaInicial;
    }

    public void setCargaInicial(double cargaInicial) {
        this.cargaInicial = cargaInicial;
    }

    public int getDescansoSegundos() {
        return descansoSegundos;
    }

    public void setDescansoSegundos(int descansoSegundos) {
        this.descansoSegundos = descansoSegundos;
    }
}
