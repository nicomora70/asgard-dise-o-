package co.edu.ucc.asgard.excepcion;

/** La operacion viola una regla de negocio: la API responde 409. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
