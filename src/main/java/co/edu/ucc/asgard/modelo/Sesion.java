package co.edu.ucc.asgard.modelo;

import co.edu.ucc.asgard.modelo.enums.EstadoSesion;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/** Un dia de entrenamiento programado para el socio (coleccion "sesiones"). */
@Document(collection = "sesiones")
@CompoundIndex(name = "socio_fecha", def = "{'socioId': 1, 'fechaProgramada': 1}")
public class Sesion {

    @Id
    private String id;
    private String socioId;
    private String rutinaId;
    private DayOfWeek dia;
    private String nombreDia;
    private LocalDate fechaProgramada;
    private EstadoSesion estado = EstadoSesion.PENDIENTE;
    private Instant fechaInicio;
    private Instant fechaFin;

    /** Marca la sesion en curso al registrar la primera serie. */
    public void iniciar(Instant ahora) {
        if (estado == EstadoSesion.PENDIENTE) {
            estado = EstadoSesion.EN_CURSO;
            fechaInicio = ahora;
        }
    }

    public void completar(Instant ahora) {
        if (fechaInicio == null) {
            fechaInicio = ahora;
        }
        estado = EstadoSesion.COMPLETADA;
        fechaFin = ahora;
    }

    public void omitir() {
        estado = EstadoSesion.OMITIDA;
    }

    /** Una sesion cerrada (completada u omitida) ya no esta pendiente. */
    public boolean estaCerrada() {
        return estado == EstadoSesion.COMPLETADA || estado == EstadoSesion.OMITIDA;
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

    public String getRutinaId() {
        return rutinaId;
    }

    public void setRutinaId(String rutinaId) {
        this.rutinaId = rutinaId;
    }

    public DayOfWeek getDia() {
        return dia;
    }

    public void setDia(DayOfWeek dia) {
        this.dia = dia;
    }

    public String getNombreDia() {
        return nombreDia;
    }

    public void setNombreDia(String nombreDia) {
        this.nombreDia = nombreDia;
    }

    public LocalDate getFechaProgramada() {
        return fechaProgramada;
    }

    public void setFechaProgramada(LocalDate fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }

    public EstadoSesion getEstado() {
        return estado;
    }

    public void setEstado(EstadoSesion estado) {
        this.estado = estado;
    }

    public Instant getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Instant fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Instant getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Instant fechaFin) {
        this.fechaFin = fechaFin;
    }
}
