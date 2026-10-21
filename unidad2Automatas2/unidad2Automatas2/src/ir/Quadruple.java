package ir;

/**
 * Cuádruplo: (operador, arg1, arg2, resultado). Los campos vacíos se muestran
 * como "-".
 */
public class Quadruple {
	public final String op, arg1, arg2, result;

	public Quadruple(String op, String arg1, String arg2, String result) {
		this.op = op;
		this.arg1 = arg1;
		this.arg2 = arg2;
		this.result = result;
	}

	private static String f(String s) {
		return s == null ? "-" : s;
	}

	@Override
	public String toString() {
		return "(" + op + ", " + f(arg1) + ", " + f(arg2) + ", " + f(result) + ")";
	}
}
