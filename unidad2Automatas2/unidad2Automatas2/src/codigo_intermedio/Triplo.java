package codigo_intermedio;

/**
 * Triplo: (operador, arg1, arg2).
 * No tiene campo de resultado: el resultado de una operación se referencia
 * por el número de su instrucción, escrito como "(n)".
 */
public class Triplo {
	public final String op, arg1, arg2;

	public Triplo(String op, String arg1, String arg2) {
		this.op = op;
		this.arg1 = arg1;
		this.arg2 = arg2;
	}

	private static String f(String s) {
		return s == null ? "-" : s;
	}

	@Override
	public String toString() {
		return "(" + op + ", " + f(arg1) + ", " + f(arg2) + ")";
	}
}
