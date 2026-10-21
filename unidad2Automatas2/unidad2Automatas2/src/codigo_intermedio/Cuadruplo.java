package codigo_intermedio;

/**
 * Cuádruplo: (operador, arg1, arg2, resultado).
 * Los campos vacíos se representan como "-".
 */
public class Cuadruplo {
	public final String op, arg1, arg2, resultado;
	public final String result; // alias por compatibilidad

	public Cuadruplo(String op, String arg1, String arg2, String resultado) {
		this.op = op;
		this.arg1 = arg1;
		this.arg2 = arg2;
		this.resultado = resultado;
		this.result = resultado;
	}

	private static String f(String s) {
		return s == null ? "-" : s;
	}

	@Override
	public String toString() {
		return "(" + op + ", " + f(arg1) + ", " + f(arg2) + ", " + f(resultado) + ")";
	}
}
