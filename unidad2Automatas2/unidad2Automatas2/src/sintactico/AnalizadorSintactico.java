package sintactico;

import lexico.TipoToken;
import lexico.Token;

import java.util.List;

/**
 * Analizador Sintáctico (Parser descendente recursivo).
 * 
 * Gramática:
 *   programa        -> instruccion* EOF
 *   instruccion     -> declaracion | asignacion | condicional | llamada
 *   declaracion     -> (entero | decimal) ID [= expr] ;
 *   asignacion      -> ID = expr ;
 *   condicional     -> si ( expr ) { instruccion* }
 *   llamada         -> ID ( [expr {, expr}] ) ;
 *   expr            -> relacional
 *   relacional      -> aditiva [ (< | > | == | !=) aditiva ]
 *   aditiva         -> multiplicativa [ (+ | -) multiplicativa ]*
 *   multiplicativa  -> factor [ (* | /) factor ]*
 *   factor          -> NUMERO | ID | ( expr )
 */
public class AnalizadorSintactico {

	private final List<Token> tokens;
	private int pos = 0;

	public AnalizadorSintactico(List<Token> tokens) {
		this.tokens = tokens;
	}

	// ---------- utilidades ----------
	private Token peek() {
		return tokens.get(pos);
	}

	private Token advance() {
		Token t = tokens.get(pos);
		if (t.getTipo() != TipoToken.EOF)
			pos++;
		return t;
	}

	private boolean check(TipoToken type) {
		return peek().getTipo() == type;
	}

	private boolean match(TipoToken type) {
		if (check(type)) {
			advance();
			return true;
		}
		return false;
	}

	private Token expect(TipoToken type, String message) {
		if (!check(type)) {
			throw new ExcepcionCompilador(
					"Línea " + peek().getLinea() + ": " + message + " (se encontró '" + peek().getValor() + "')");
		}
		return advance();
	}

	// ---------- programa e instrucciones ----------
	public NodoAST analizarPrograma() {
		NodoAST program = new NodoAST(NodoAST.Tipo.BLOQUE, null);
		while (!check(TipoToken.EOF)) {
			program.hijos.add(parseStatement());
		}
		return program;
	}

	// Alias por compatibilidad
	public NodoAST parseProgram() {
		return analizarPrograma();
	}

	private NodoAST parseStatement() {
		switch (peek().getTipo()) {
		case ENTERO:
		case DECIMAL:
			return parseDeclaration();
		case SI:
			return parseIf();
		case IMPRIMIR:
			return parseCall();
		case IDENTIFICADOR:
			// ID ( ... ) es llamada; ID = ... es asignación
			if (pos + 1 < tokens.size() && tokens.get(pos + 1).getTipo() == TipoToken.PARENTESIS_ABR)
				return parseCall();
			return parseAssignment();
		case DESCONOCIDO:
			throw new ExcepcionCompilador("Línea " + peek().getLinea() + ": Símbolo no admitido: '" + peek().getValor() + "'");
		default:
			throw new ExcepcionCompilador("Línea " + peek().getLinea() + ": Instrucción no válida (se encontró '" + peek().getValor() + "')");
		}
	}

	private NodoAST parseDeclaration() {
		String type = advance().getValor();
		Token nameToken = expect(TipoToken.IDENTIFICADOR, "Se esperaba un nombre de variable");
		String name = nameToken.getValor();
		NodoAST node = new NodoAST(NodoAST.Tipo.DECLARACION, name, nameToken.getLinea());
		node.tipoDato = type;
		if (match(TipoToken.IGUAL)) {
			node.hijos.add(parseExpression());
		}
		expect(TipoToken.PUNTO_COMA, "Falta ';' al final de la declaración");
		return node;
	}

	private NodoAST parseAssignment() {
		Token idTok = advance();
		String name = idTok.getValor();
		expect(TipoToken.IGUAL, "Se esperaba '=' después de '" + name + "'");
		NodoAST expr = parseExpression();
		expect(TipoToken.PUNTO_COMA, "Falta ';' al final de la asignación");
		return new NodoAST(NodoAST.Tipo.ASIGNACION, name, idTok.getLinea(), expr);
	}

	private NodoAST parseIf() {
		Token ifTok = advance(); // 'si'
		expect(TipoToken.PARENTESIS_ABR, "Se esperaba '(' después de 'si'");
		NodoAST cond = parseExpression();
		expect(TipoToken.PARENTESIS_CIE, "Paréntesis desbalanceado: falta ')' en la condición");
		expect(TipoToken.LLAVE_ABR, "Se esperaba '{' para abrir el bloque del 'si'");
		NodoAST block = new NodoAST(NodoAST.Tipo.BLOQUE, null, ifTok.getLinea());
		while (!check(TipoToken.LLAVE_CIE) && !check(TipoToken.EOF)) {
			block.hijos.add(parseStatement());
		}
		expect(TipoToken.LLAVE_CIE, "Falta '}' para cerrar el bloque del 'si'");
		return new NodoAST(NodoAST.Tipo.SI, null, ifTok.getLinea(), cond, block);
	}

	private NodoAST parseCall() {
		Token callTok = advance();
		String name = callTok.getValor();
		expect(TipoToken.PARENTESIS_ABR, "Se esperaba '(' después de '" + name + "'");
		NodoAST call = new NodoAST(NodoAST.Tipo.LLAMADA, name, callTok.getLinea());
		if (!check(TipoToken.PARENTESIS_CIE)) {
			do {
				call.hijos.add(parseExpression());
			} while (match(TipoToken.COMA));
		}
		expect(TipoToken.PARENTESIS_CIE, "Paréntesis desbalanceado: falta ')' en la llamada a '" + name + "'");
		expect(TipoToken.PUNTO_COMA, "Falta ';' al final de la llamada");
		return call;
	}

	// ---------- expresiones ----------
	private NodoAST parseExpression() {
		NodoAST left = parseAdditive();
		if (check(TipoToken.MENOR) || check(TipoToken.MAYOR) || check(TipoToken.IGUAL_IGUAL)
				|| check(TipoToken.DIFERENTE)) {
			Token opTok = advance();
			NodoAST right = parseAdditive();
			return new NodoAST(NodoAST.Tipo.BINARIO, opTok.getValor(), opTok.getLinea(), left, right);
		}
		return left;
	}

	private NodoAST parseAdditive() {
		NodoAST left = parseTerm();
		while (check(TipoToken.MAS) || check(TipoToken.MENOS)) {
			Token opTok = advance();
			left = new NodoAST(NodoAST.Tipo.BINARIO, opTok.getValor(), opTok.getLinea(), left, parseTerm());
		}
		return left; // asociativo a la izquierda: a - b - c = (a - b) - c
	}

	private NodoAST parseTerm() {
		NodoAST left = parseFactor();
		while (check(TipoToken.MULTIPLICACION) || check(TipoToken.DIVISION)) {
			Token opTok = advance();
			left = new NodoAST(NodoAST.Tipo.BINARIO, opTok.getValor(), opTok.getLinea(), left, parseFactor());
		}
		return left;
	}

	private NodoAST parseFactor() {
		Token t = peek();
		switch (t.getTipo()) {
		case NUMERO_ENTERO:
		case NUMERO_DECIMAL:
			advance();
			return new NodoAST(NodoAST.Tipo.NUMERO, t.getValor(), t.getLinea());
		case IDENTIFICADOR:
			Token idTok = advance();
			return new NodoAST(NodoAST.Tipo.VARIABLE, idTok.getValor(), idTok.getLinea());
		case PARENTESIS_ABR: {
			advance();
			NodoAST inner = parseExpression();
			expect(TipoToken.PARENTESIS_CIE, "Paréntesis desbalanceado: falta ')'");
			return inner;
		}
		case PUNTO_COMA:
			throw new ExcepcionCompilador("Línea " + t.getLinea() + ": Asignación sin expresión: falta el valor antes de ';'");
		case DESCONOCIDO:
			throw new ExcepcionCompilador("Línea " + t.getLinea() + ": Símbolo no admitido: '" + t.getValor() + "'");
		default:
			throw new ExcepcionCompilador("Línea " + t.getLinea() + ": Expresión inválida cerca de '" + t.getValor() + "'");
		}
	}
}
