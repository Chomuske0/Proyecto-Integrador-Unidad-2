package parser;

/** Error de análisis con mensaje claro para el usuario. */
public class CompilerException extends RuntimeException {
	public CompilerException(String message) {
		super(message);
	}
}