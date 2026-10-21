package semantic;

import notations.NotationConverter;
import parser.ASTNode;
import parser.CompilerException;
import symboltable.SymbolTable;

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
public class SemanticAnalyzer {

	private final Set<String> declared = new HashSet<>();
	private final SymbolTable symbolTable = new SymbolTable();

	public SymbolTable analyze(ASTNode root) {
		check(root);
		return symbolTable;
	}

	private void check(ASTNode n) {
		if (n == null)
			return;

		switch (n.kind) {
		case BLOCK:
			for (ASTNode child : n.children) {
				check(child);
			}
			break;

		case DECL:
			// 1. Detectar redeclaración duplicada
			if (declared.contains(n.value)) {
				throw new CompilerException("Línea " + n.line + ": La variable '" + n.value + "' ya fue declarada");
			}

			// 2. Si tiene inicialización, se valida ANTES de agregar a 'declared'
			// para evitar autorreferencias: entero a = a + 1;
			if (!n.children.isEmpty()) {
				check(n.children.get(0));
			}

			declared.add(n.value);

			// 3. Registrar en la Tabla de Símbolos
			String initVal = "-";
			if (!n.children.isEmpty()) {
				initVal = NotationConverter.toInfix(n.children.get(0));
			}
			symbolTable.add(n.value, n.varType, initVal);
			break;

		case ASSIGN:
			if (!declared.contains(n.value)) {
				throw new CompilerException("Línea " + n.line + ": Identificador no declarado: '" + n.value + "'");
			}
			check(n.children.get(0));
			break;

		case IF:
			check(n.children.get(0)); // condición
			check(n.children.get(1)); // cuerpo del bloque
			break;

		case CALL:
			for (ASTNode arg : n.children) {
				check(arg);
			}
			break;

		case BINARY:
			check(n.left());
			check(n.right());
			break;

		case VAR:
			if (!declared.contains(n.value)) {
				throw new CompilerException("Línea " + n.line + ": Identificador no declarado: '" + n.value + "'");
			}
			break;

		case NUMBER:
			break;

		default:
			break;
		}
	}

	public SymbolTable getSymbolTable() {
		return symbolTable;
	}
}
