package co.edu.ucc.asgard.modelo;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Rutina semanal de un socio (coleccion "rutinas"). Un socio guarda el historial de sus rutinas y solo una esta
 * activa. Embebe sus dias y cada dia sus prescripciones (RF03).
 */
@Document(collection = "rutinas")
@CompoundIndex(name = "socio_activa", def = "{'socioId': 1, 'activa': 1}")
public class Rutina {

    @Id
    private String id;
    private String socioId;
    private String entrenadorId;
    private String nombre;
    private LocalDate fechaInicio;
    private int semanas;
    private boolean activa;
    private Instant fechaCreacion;
    private List<DiaRutina> dias = new ArrayList<>();

    /** Devuelve el dia de la rutina que corresponde al dia de la semana, si se entrena ese dia. */
    public Optional<DiaRutina> diaDe(DayOfWeek dia) {
        return dias.stream().filter(d -> d.getDia() == dia).findFirst();
    }

    /** La rutina pasa al historial: deja de ser la activa. */
    public void desactivar() {
        this.activa = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSocioId() {
        return socioId;
    }

    public void setSocioId(String socioId) {
        this.socioId = socioId;
    }

    public String getEntrenadorId() {
        return entrenadorId;
    }

    public void setEntrenadorId(String entrenadorId) {
        this.entrenadorId = entrenadorId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public int getSemanas() {
        return semanas;
    }

    public void setSemanas(int semanas) {
        this.semanas = semanas;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<DiaRutina> getDias() {
        return dias;
    }

    public void setDias(List<DiaRutina> dias) {
        this.dias = dias == null ? new ArrayList<>() : new ArrayList<>(dias);
    }
}
