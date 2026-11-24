package co.edu.ucc.asgard.modelo;

import co.edu.ucc.asgard.modelo.enums.GrupoMuscular;
import co.edu.ucc.asgard.modelo.enums.Nivel;
import co.edu.ucc.asgard.modelo.enums.Objetivo;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Socio del gimnasio (coleccion "socios"). Embebe sus datos fisicos, la pila de series para deshacer (RF07) y la
 * cola de sesiones pendientes (RF04).
 */
@Document(collection = "socios")
public class Socio extends Persona {

    private Nivel nivel;
    private Objetivo objetivo;
    private LocalDate fechaIngreso;
    private DatosFisicos datosFisicos;
    private List<GrupoMuscular> lesiones = new ArrayList<>();
    @Indexed
    private String entrenadorId;
    /** Ids de las ultimas series registradas; la cima es la mas reciente (pila LIFO). */
    private List<String> pilaDeshacer = new ArrayList<>();
    /** Ids de las sesiones no realizadas; el frente es la mas antigua (cola FIFO). */
    private List<String> colaPendientes = new ArrayList<>();

    /** Indica si el socio tiene una lesion en el grupo muscular dado. */
    public boolean tieneLesion(GrupoMuscular grupo) {
        return lesiones != null && lesiones.contains(grupo);
    }

    public Nivel getNivel() {
        return nivel;
    }

    public void setNivel(Nivel nivel) {
        this.nivel = nivel;
    }

    public Objetivo getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Objetivo objetivo) {
        this.objetivo = objetivo;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public DatosFisicos getDatosFisicos() {
        return datosFisicos;
    }

    public void setDatosFisicos(DatosFisicos datosFisicos) {
        this.datosFisicos = datosFisicos;
    }

    public List<GrupoMuscular> getLesiones() {
        return lesiones;
    }

    public void setLesiones(List<GrupoMuscular> lesiones) {
        this.lesiones = lesiones == null ? new ArrayList<>() : new ArrayList<>(lesiones);
    }

    public String getEntrenadorId() {
        return entrenadorId;
    }

    public void setEntrenadorId(String entrenadorId) {
        this.entrenadorId = entrenadorId;
    }

    public List<String> getPilaDeshacer() {
        return pilaDeshacer;
    }

    public void setPilaDeshacer(List<String> pilaDeshacer) {
        this.pilaDeshacer = pilaDeshacer == null ? new ArrayList<>() : new ArrayList<>(pilaDeshacer);
    }

    public List<String> getColaPendientes() {
        return colaPendientes;
    }

    public void setColaPendientes(List<String> colaPendientes) {
        this.colaPendientes = colaPendientes == null ? new ArrayList<>() : new ArrayList<>(colaPendientes);
    }
}
