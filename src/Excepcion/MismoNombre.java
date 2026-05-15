package Excepcion;

public class MismoNombre extends RuntimeException {
    public MismoNombre(String message) {
        super(message);
    }
}
