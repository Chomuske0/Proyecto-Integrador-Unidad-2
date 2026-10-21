package sintactico;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Nodo del árbol sintáctico abstracto (AST). */
public class NodoAST {

	public enum Tipo {
		NUMERO,        // valor = "2" o "3.5"
		VARIABLE,      // valor = nombre
		BINARIO,       // valor = operador; hijos: [izq, der]
		DECLARACION,   // valor = nombre; tipoDato = entero/decimal; hijos: [expr] (opcional)
		ASIGNACION,    // valor = nombre; hijos: [expr]
		SI,            // hijos: [condición, bloque]
		LLAMADA,       // valor = nombre de función; hijos: argumentos
		BLOQUE         // hijos: instrucciones
	}

	public final Tipo tipo;
	public final String valor;
	public String tipoDato; // solo para DECLARACION
	public int linea;
	public final List<NodoAST> hijos = new ArrayList<>();

	public NodoAST(Tipo tipo, String valor, int linea, NodoAST... subnodos) {
		this.tipo = tipo;
		this.valor = valor;
		this.linea = linea;
		this.hijos.addAll(Arrays.asList(subnodos));
	}

	public NodoAST(Tipo tipo, String valor, NodoAST... subnodos) {
		this(tipo, valor, 0, subnodos);
	}

	public NodoAST izq() {
		return hijos.get(0);
	}

	public NodoAST der() {
		return hijos.get(1);
	}

	// Alias auxiliares
	public NodoAST left() {
		return izq();
	}

	public NodoAST right() {
		return der();
	}

	@Override
	public String toString() {
		return tipo + (valor != null ? "(" + valor + ")" : "") + (hijos.isEmpty() ? "" : hijos.toString());
	}
}
