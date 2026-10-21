package notations;

import parser.ASTNode;

/** Convierte un AST de expresión a notación infija, prefija y postfija. */
public class NotationConverter {

	/** Precedencia: mayor número = se evalúa primero. */
	private static int precedence(ASTNode n) {
		if (n.kind != ASTNode.Kind.BINARY)
			return 100; // hojas
		switch (n.value) {
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
	public static String toInfix(ASTNode n) {
		if (n.kind != ASTNode.Kind.BINARY)
			return n.value;
		int p = precedence(n);
		String l = toInfix(n.left());
		String r = toInfix(n.right());
		// Paréntesis si el hijo pesa menos; en el lado derecho también si empata
		// (para conservar a - (b - c) y a / (b * c)).
		if (precedence(n.left()) < p)
			l = "(" + l + ")";
		if (precedence(n.right()) <= p && n.right().kind == ASTNode.Kind.BINARY)
			r = "(" + r + ")";
		return l + " " + n.value + " " + r;
	}

	/** Prefija: operador, izquierda, derecha. */
	public static String toPrefix(ASTNode n) {
		if (n.kind != ASTNode.Kind.BINARY)
			return n.value;
		return n.value + " " + toPrefix(n.left()) + " " + toPrefix(n.right());
	}

	/** Postfija: izquierda, derecha, operador. */
	public static String toPostfix(ASTNode n) {
		if (n.kind != ASTNode.Kind.BINARY)
			return n.value;
		return toPostfix(n.left()) + " " + toPostfix(n.right()) + " " + n.value;
	}
}