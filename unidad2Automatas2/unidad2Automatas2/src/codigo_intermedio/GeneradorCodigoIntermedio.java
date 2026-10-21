package codigo_intermedio;

import sintactico.NodoAST;

import java.util.ArrayList;
import java.util.List;

/**
 * Generador de Código Intermedio:
 * Produce cuádruplos y triplos recorriendo el AST en postorden (primero los
 * operandos hijos y luego el operador padre).
 */
public class GeneradorCodigoIntermedio {

	private final List<Cuadruplo> cuadruplos = new ArrayList<>();
	private final List<Triplo> triplos = new ArrayList<>();
	private int contadorTemporales = 0;
	private int contadorEtiquetas = 0;

	/**
	 * Un operando tiene dos representaciones:
	 *  - quad: forma de cuádruplos (ej. temporal 't1' o constante/variable 'a')
	 *  - triple: forma de triplos (ej. referencia por índice '(1)' o constante/variable 'a')
	 */
	private static class Operando {
		final String quad, triple;

		Operando(String quad, String triple) {
			this.quad = quad;
			this.triple = triple;
		}
	}

	public List<Cuadruplo> getCuadruplos() {
		return cuadruplos;
	}

	public List<Cuadruplo> getQuadruples() {
		return getCuadruplos();
	}

	public List<Triplo> getTriplos() {
		return triplos;
	}

	public List<Triplo> getTriples() {
		return getTriplos();
	}

	// ---------- API ----------
	public void generar(NodoAST nodo) {
		switch (nodo.tipo) {
		case BLOQUE:
			for (NodoAST hijo : nodo.hijos)
				generar(hijo);
			break;
		case DECLARACION:
			// Si tiene valor inicial genera asignación; si no, solo va a la tabla de símbolos
			if (!nodo.hijos.isEmpty())
				generarAsignacion(nodo.valor, nodo.hijos.get(0));
			break;
		case ASIGNACION:
			generarAsignacion(nodo.valor, nodo.hijos.get(0));
			break;
		case SI:
			generarSi(nodo);
			break;
		case LLAMADA:
			generarLlamada(nodo);
			break;
		default:
			throw new IllegalArgumentException("El nodo no es una instrucción válida: " + nodo.tipo);
		}
	}

	public void generate(NodoAST nodo) {
		generar(nodo);
	}

	// ---------- instrucciones ----------
	private void generarAsignacion(String nombre, NodoAST expr) {
		Operando src = generarExpr(expr);
		cuadruplos.add(new Cuadruplo("=", src.quad, null, nombre));
		triplos.add(new Triplo("=", src.triple, nombre));
	}

	private void generarSi(NodoAST nodo) {
		Operando cond = generarExpr(nodo.hijos.get(0));
		String finEtiqueta = nuevaEtiqueta();
		cuadruplos.add(new Cuadruplo("IF_FALSE", cond.quad, null, finEtiqueta));
		triplos.add(new Triplo("IF_FALSE", cond.triple, finEtiqueta));

		generar(nodo.hijos.get(1)); // cuerpo del bloque si

		cuadruplos.add(new Cuadruplo("LABEL", null, null, finEtiqueta));
		triplos.add(new Triplo("LABEL", finEtiqueta, null));
	}

	private void generarLlamada(NodoAST llamada) {
		// Primero se evalúan todos los argumentos, luego se emiten PARAM y CALL
		List<Operando> args = new ArrayList<>();
		for (NodoAST a : llamada.hijos)
			args.add(generarExpr(a));

		for (Operando a : args) {
			cuadruplos.add(new Cuadruplo("PARAM", a.quad, null, null));
			triplos.add(new Triplo("PARAM", a.triple, null));
		}
		String n = String.valueOf(args.size());
		cuadruplos.add(new Cuadruplo("CALL", llamada.valor, n, null));
		triplos.add(new Triplo("CALL", llamada.valor, n));
	}

	// ---------- expresiones ----------
	private Operando generarExpr(NodoAST n) {
		if (n.tipo == NodoAST.Tipo.NUMERO || n.tipo == NodoAST.Tipo.VARIABLE) {
			return new Operando(n.valor, n.valor);
		}
		// BINARIO: postorden (hijos primero, operador después)
		Operando l = generarExpr(n.izq());
		Operando r = generarExpr(n.der());

		String temporal = "t" + (++contadorTemporales);
		cuadruplos.add(new Cuadruplo(n.valor, l.quad, r.quad, temporal));

		triplos.add(new Triplo(n.valor, l.triple, r.triple));
		String referencia = "(" + triplos.size() + ")"; // índice de instrucción base 1
		return new Operando(temporal, referencia);
	}

	private String nuevaEtiqueta() {
		return "L" + (++contadorEtiquetas);
	}
}
