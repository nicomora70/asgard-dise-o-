package co.edu.ucc.asgard.excepcion;

/** Los datos de la peticion no son validos: la API responde 400. */
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
