package co.edu.ucc.asgard.modelo;

import co.edu.ucc.asgard.modelo.enums.RegistradoPor;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Una serie registrada (coleccion "series"): peso, repeticiones y esfuerzo. Deshacer no la borra, la marca como
 * deshecha para conservar el historial (RT-02).
 */
@Document(collection = "series")
@CompoundIndex(name = "socio_ejercicio_fecha", def = "{'socioId': 1, 'ejercicioId': 1, 'fechaHora': -1}")
public class Serie {

    @Id
    private String id;
    private String socioId;
    private String sesionId;
    private String ejercicioId;
    /** Numero de la serie dentro de la sesion para ese ejercicio (1, 2, 3...). */
    private int numero;
    private double peso;
    private int repeticiones;
    /** Esfuerzo percibido de 1 a 10; es opcional. */
    private Double rpe;
    /** 1RM estimado con la formula de Epley. */
    private double e1RM;
    /** Carga que sugeria el motor cuando se registro la serie. */
    private Double cargaSugerida;
    private boolean superaSugerida;
    private Instant fechaHora;
    private RegistradoPor registradoPor = RegistradoPor.SOCIO;
    private String notas;
    private boolean deshecha;
    private Instant fechaDeshecha;

    /** Marca la serie como deshecha sin borrarla (RF07, RT-02). */
    public void deshacer(Instant ahora) {
        this.deshecha = true;
        this.fechaDeshecha = ahora;
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

    public String getSesionId() {
        return sesionId;
    }

    public void setSesionId(String sesionId) {
        this.sesionId = sesionId;
    }

    public String getEjercicioId() {
        return ejercicioId;
    }

    public void setEjercicioId(String ejercicioId) {
        this.ejercicioId = ejercicioId;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public int getRepeticiones() {
        return repeticiones;
    }

    public void setRepeticiones(int repeticiones) {
        this.repeticiones = repeticiones;
    }

    public Double getRpe() {
        return rpe;
    }

    public void setRpe(Double rpe) {
        this.rpe = rpe;
    }

    public double getE1RM() {
        return e1RM;
    }

    public void setE1RM(double e1RM) {
        this.e1RM = e1RM;
    }

    public Double getCargaSugerida() {
        return cargaSugerida;
    }

    public void setCargaSugerida(Double cargaSugerida) {
        this.cargaSugerida = cargaSugerida;
    }

    public boolean isSuperaSugerida() {
        return superaSugerida;
    }

    public void setSuperaSugerida(boolean superaSugerida) {
        this.superaSugerida = superaSugerida;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Instant fechaHora) {
        this.fechaHora = fechaHora;
    }

    public RegistradoPor getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(RegistradoPor registradoPor) {
        this.registradoPor = registradoPor;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public boolean isDeshecha() {
        return deshecha;
    }

    public void setDeshecha(boolean deshecha) {
        this.deshecha = deshecha;
    }

    public Instant getFechaDeshecha() {
        return fechaDeshecha;
    }

    public void setFechaDeshecha(Instant fechaDeshecha) {
        this.fechaDeshecha = fechaDeshecha;
    }
}
