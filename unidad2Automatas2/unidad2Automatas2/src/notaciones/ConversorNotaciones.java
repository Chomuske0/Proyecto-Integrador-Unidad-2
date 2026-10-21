package notaciones;

import sintactico.NodoAST;

/** Convierte un AST de expresión a notación infija, prefija y postfija. */
public class ConversorNotaciones {

	/** Precedencia: mayor número = se evalúa primero. */
	private static int precedencia(NodoAST n) {
		if (n.tipo != NodoAST.Tipo.BINARIO)
			return 100; // hojas
		switch (n.valor) {
		case "*":
		case "/":
			return 3;
		case "+":
		case "-":
			return 2;
		default:
			return 1; // <, >, ==, !=
		}
	}

	/** Infija con los paréntesis mínimos necesarios. */
	public static String aInfija(NodoAST n) {
		if (n.tipo != NodoAST.Tipo.BINARIO)
			return n.valor;
		int p = precedencia(n);
		String l = aInfija(n.izq());
		String r = aInfija(n.der());
		// Paréntesis si el hijo pesa menos; en el lado derecho también si empata
		if (precedencia(n.izq()) < p)
			l = "(" + l + ")";
		if (precedencia(n.der()) <= p && n.der().tipo == NodoAST.Tipo.BINARIO)
			r = "(" + r + ")";
		return l + " " + n.valor + " " + r;
	}

	public static String toInfix(NodoAST n) {
		return aInfija(n);
	}

	/** Prefija: operador, izquierda, derecha. */
	public static String aPrefija(NodoAST n) {
		if (n.tipo != NodoAST.Tipo.BINARIO)
			return n.valor;
		return n.valor + " " + aPrefija(n.izq()) + " " + aPrefija(n.der());
	}

	public static String toPrefix(NodoAST n) {
		return aPrefija(n);
	}

	/** Postfija: izquierda, derecha, operador. */
	public static String aPostfija(NodoAST n) {
		if (n.tipo != NodoAST.Tipo.BINARIO)
			return n.valor;
		return aPostfija(n.izq()) + " " + aPostfija(n.der()) + " " + n.valor;
	}

	public static String toPostfix(NodoAST n) {
		return aPostfija(n);
	}
}
