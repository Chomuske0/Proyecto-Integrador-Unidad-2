package tablasimbolos;

import notaciones.ConversorNotaciones;
import sintactico.NodoAST;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Tabla de símbolos: identificador, tipo y valor inicial (si existe). */
public class TablaSimbolos {

	public static class Entrada {
		public final String id;
		public final String tipo;
		public final String valorInicial;

		public Entrada(String id, String tipo, String valorInicial) {
			this.id = id;
			this.tipo = tipo;
			this.valorInicial = valorInicial;
		}

		// Alias por compatibilidad
		public String getType() { return tipo; }
		public String getInitialValue() { return valorInicial; }
	}

	private final Map<String, Entrada> entradas = new LinkedHashMap<>();

	public void agregar(String id, String tipo, String valorInicial) {
		entradas.put(id, new Entrada(id, tipo, valorInicial));
	}

	public void add(String id, String tipo, String valorInicial) {
		agregar(id, tipo, valorInicial);
	}

	public List<Entrada> getEntradas() {
		return new ArrayList<>(entradas.values());
	}

	public List<Entrada> getEntries() {
		return getEntradas();
	}

	public boolean contiene(String id) {
		return entradas.containsKey(id);
	}

	public boolean contains(String id) {
		return contiene(id);
	}

	/** Recorre el AST y registra cada declaración (nodo DECLARACION). */
	public static TablaSimbolos desdeAST(NodoAST raiz) {
		TablaSimbolos tabla = new TablaSimbolos();
		tabla.recolectar(raiz);
		return tabla;
	}

	public static TablaSimbolos fromAST(NodoAST raiz) {
		return desdeAST(raiz);
	}

	private void recolectar(NodoAST n) {
		if (n.tipo == NodoAST.Tipo.DECLARACION) {
			String init = "-";
			if (!n.hijos.isEmpty()) {
				init = ConversorNotaciones.aInfija(n.hijos.get(0));
			}
			entradas.put(n.valor, new Entrada(n.valor, n.tipoDato, init));
		}
		for (NodoAST hijo : n.hijos)
			recolectar(hijo);
	}
}
