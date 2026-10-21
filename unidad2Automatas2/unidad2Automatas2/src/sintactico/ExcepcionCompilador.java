package sintactico;

/** Error de análisis con mensaje claro para el usuario. */
public class ExcepcionCompilador extends RuntimeException {
	public ExcepcionCompilador(String message) {
		super(message);
	}
}
