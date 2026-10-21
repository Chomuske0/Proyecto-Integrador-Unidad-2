package parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Nodo del árbol sintáctico. */
public class ASTNode {

	public enum Kind {
		NUMBER, // value = "2" o "3.5"
		VAR, // value = nombre
		BINARY, // value = operador; hijos: [izq, der]
		DECL, // value = nombre; varType = entero/decimal; hijos: [expr] (opcional)
		ASSIGN, // value = nombre; hijos: [expr]
		IF, // hijos: [condición, bloque]
		CALL, // value = nombre de función; hijos: argumentos
		BLOCK // hijos: instrucciones
	}

	public final Kind kind;
	public final String value;
	public String varType; // solo para DECL
	public int line;
	public final List<ASTNode> children = new ArrayList<>();

	public ASTNode(Kind kind, String value, int line, ASTNode... kids) {
		this.kind = kind;
		this.value = value;
		this.line = line;
		this.children.addAll(Arrays.asList(kids));
	}

	public ASTNode(Kind kind, String value, ASTNode... kids) {
		this(kind, value, 0, kids);
	}

	public ASTNode left() {
		return children.get(0);
	}

	public ASTNode right() {
		return children.get(1);
	}

	@Override
	public String toString() {
		return kind + (value != null ? "(" + value + ")" : "") + (children.isEmpty() ? "" : children.toString());
	}
}