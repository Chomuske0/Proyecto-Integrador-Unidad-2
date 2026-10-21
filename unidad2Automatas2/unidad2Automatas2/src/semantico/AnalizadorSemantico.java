package semantico;

import notaciones.ConversorNotaciones;
import sintactico.ExcepcionCompilador;
import sintactico.NodoAST;
import tablasimbolos.TablaSimbolos;

import java.util.HashSet;
import java.util.Set;

/**
 * Analizador Semántico:
 * ------------------------------------------------------------
 * Fase que se ejecuta DESPUÉS del Analizador Sintáctico.
 * Trabaja sobre el Árbol de Sintaxis Abstracta (AST) generado por el Parser.
 *
 * Responsabilidades:
 *  1. Comprobar que toda variable utilizada haya sido declarada previamente.
 *  2. Detectar declaraciones duplicadas en el mismo ámbito.
 *  3. Validar que una variable no se use en su propia expresión inicial (ej. "entero a = a + 1;").
 *  4. Construir y poblar la Tabla de Símbolos validada.
 */
public class AnalizadorSemantico {

	private final Set<String> declaradas = new HashSet<>();
	private final TablaSimbolos tablaSimbolos = new TablaSimbolos();

	public TablaSimbolos analizar(NodoAST raiz) {
		comprobar(raiz);
		return tablaSimbolos;
	}

	public TablaSimbolos analyze(NodoAST raiz) {
		return analizar(raiz);
	}

	private void comprobar(NodoAST n) {
		if (n == null)
			return;

		switch (n.tipo) {
		case BLOQUE:
			for (NodoAST hijo : n.hijos) {
				comprobar(hijo);
			}
			break;

		case DECLARACION:
			// 1. Detectar redeclaración duplicada
			if (declaradas.contains(n.valor)) {
				throw new ExcepcionCompilador("Línea " + n.linea + ": La variable '" + n.valor + "' ya fue declarada");
			}

			// 2. Si tiene inicialización, se valida ANTES de agregar a 'declaradas'
			// para evitar autorreferencias: entero a = a + 1;
			if (!n.hijos.isEmpty()) {
				comprobar(n.hijos.get(0));
			}

			declaradas.add(n.valor);

			// 3. Registrar en la Tabla de Símbolos
			String valorInicial = "-";
			if (!n.hijos.isEmpty()) {
				valorInicial = ConversorNotaciones.aInfija(n.hijos.get(0));
			}
			tablaSimbolos.agregar(n.valor, n.tipoDato, valorInicial);
			break;

		case ASIGNACION:
			if (!declaradas.contains(n.valor)) {
				throw new ExcepcionCompilador("Línea " + n.linea + ": Identificador no declarado: '" + n.valor + "'");
			}
			comprobar(n.hijos.get(0));
			break;

		case SI:
			comprobar(n.hijos.get(0)); // condición
			comprobar(n.hijos.get(1)); // cuerpo del bloque
			break;

		case LLAMADA:
			for (NodoAST arg : n.hijos) {
				comprobar(arg);
			}
			break;

		case BINARIO:
			comprobar(n.izq());
			comprobar(n.der());
			break;

		case VARIABLE:
			if (!declaradas.contains(n.valor)) {
				throw new ExcepcionCompilador("Línea " + n.linea + ": Identificador no declarado: '" + n.valor + "'");
			}
			break;

		case NUMERO:
			break;

		default:
			break;
		}
	}

	public TablaSimbolos getTablaSimbolos() {
		return tablaSimbolos;
	}

	public TablaSimbolos getSymbolTable() {
		return getTablaSimbolos();
	}
}
