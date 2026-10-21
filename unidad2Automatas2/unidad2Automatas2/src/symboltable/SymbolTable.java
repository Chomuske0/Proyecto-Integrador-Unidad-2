package symboltable;

import notations.NotationConverter;
import parser.ASTNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Tabla de símbolos: identificador, tipo y valor inicial (si existe). */
public class SymbolTable {

	public static class Entry {
		public final String id, type, initialValue;

		Entry(String id, String type, String initialValue) {
			this.id = id;
			this.type = type;
			this.initialValue = initialValue;
		}
	}

	private final Map<String, Entry> entries = new LinkedHashMap<>();

	public List<Entry> getEntries() {
		return new ArrayList<>(entries.values());
	}

	public boolean contains(String id) {
		return entries.containsKey(id);
	}

	/** Recorre el AST y registra cada declaración (nodo DECL). */
	public static SymbolTable fromAST(ASTNode root) {
		SymbolTable table = new SymbolTable();
		table.collect(root);
		return table;
	}

	private void collect(ASTNode n) {
		if (n.kind == ASTNode.Kind.DECL) {
			String init = "-";
			if (!n.children.isEmpty()) {
				// Constante directa o expresión (se muestra en infija)
				init = NotationConverter.toInfix(n.children.get(0));
			}
			entries.put(n.value, new Entry(n.value, n.varType, init));
		}
		for (ASTNode child : n.children)
			collect(child);
	}
}