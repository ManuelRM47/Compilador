package VMmachine;

public class UnderflowException extends RuntimeException {
    public UnderflowException(String message) {
        super(message);
    }
}