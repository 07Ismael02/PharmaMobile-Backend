package pe.edu.upeu.PharmaBackend.security;

public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() { super("Credenciales inválidas"); }
}
