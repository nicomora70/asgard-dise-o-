package co.edu.ucc.asgard.modelo;

/**
 * Ejercicio que puede reemplazar a otro, con su grado de similitud (0 a 1). Se guarda embebido en el ejercicio y
 * forma las aristas del grafo de sustitutos (RF10).
 */
public class Sustituto {

    private String ejercicioId;
    private double similitud;

    public Sustituto() {
    }

    public Sustituto(String ejercicioId, double similitud) {
        this.ejercicioId = ejercicioId;
        this.similitud = similitud;
    }

    public String getEjercicioId() {
        return ejercicioId;
    }

    public void setEjercicioId(String ejercicioId) {
        this.ejercicioId = ejercicioId;
    }

    public double getSimilitud() {
        return similitud;
    }

    public void setSimilitud(double similitud) {
        this.similitud = similitud;
    }
}
