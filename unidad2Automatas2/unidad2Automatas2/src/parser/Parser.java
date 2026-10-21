package parser;

import lexer.Token;
import lexer.TokenTipo;

import java.util.*;

/**
 * Parser descendente recursivo.
 *
 * Gramática reducida: programa -> instruccion* EOF instruccion -> declaracion |
 * asignacion | si | llamada declaracion -> (entero|decimal) ID [= expr] ;
 * asignacion -> ID = expr ; si -> si ( expr ) { instruccion* } llamada -> ID (
 * [expr {, expr}] ) ; (imprimir u otra función) expr -> suma [ (<|>|==|!=) suma
 * ] suma -> term { (+|-) term } term -> factor { (*|/) factor } factor ->
 * NUMERO | ID | ( expr )
 *
 * La precedencia sale de la estructura: * y / se resuelven en "term", que está
 * más abajo que "suma", así que se agrupan primero.
 */
public class Parser {

	private final List<Token> tokens;
	private int pos = 0;

	public Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	// ---------- utilidades ----------
	private Token peek() {
		return tokens.get(pos);
	}

	private Token advance() {
		Token t = tokens.get(pos);
		if (t.getTipo() != TokenTipo.EOF)
			pos++;
		return t;
	}

	private boolean check(TokenTipo type) {
		return peek().getTipo() == type;
	}

	private boolean match(TokenTipo type) {
		if (check(type)) {
			advance();
			return true;
		}
		return false;
	}

	private Token expect(TokenTipo type, String message) {
		if (!check(type)) {
			throw new CompilerException(
					"Línea " + peek().getLinea() + ": " + message + " (se encontró '" + peek().getValor() + "')");
		}
		return advance();
	}

	// ---------- programa e instrucciones ----------
	public ASTNode parseProgram() {
		ASTNode program = new ASTNode(ASTNode.Kind.BLOCK, null);
		while (!check(TokenTipo.EOF)) {
			program.children.add(parseStatement());
		}
		return program;
	}

	private ASTNode parseStatement() {
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
			if (pos + 1 < tokens.size() && tokens.get(pos + 1).getTipo() == TokenTipo.PARENTESIS_ABR)
				return parseCall();
			return parseAssignment();
		case DESCONOCIDO:
			throw new CompilerException("Línea " + peek().getLinea() + ": Símbolo no admitido: '" + peek().getValor() + "'");
		default:
			throw new CompilerException("Línea " + peek().getLinea() + ": Instrucción no válida (se encontró '" + peek().getValor() + "')");
		}
	}

	private ASTNode parseDeclaration() {
		String type = advance().getValor();
		Token nameToken = expect(TokenTipo.IDENTIFICADOR, "Se esperaba un nombre de variable");
		String name = nameToken.getValor();
		ASTNode node = new ASTNode(ASTNode.Kind.DECL, name, nameToken.getLinea());
		node.varType = type;
		if (match(TokenTipo.IGUAL)) {
			node.children.add(parseExpression());
		}
		expect(TokenTipo.PUNTO_COMA, "Falta ';' al final de la declaración");
		return node;
	}

	private ASTNode parseAssignment() {
		Token idTok = advance();
		String name = idTok.getValor();
		expect(TokenTipo.IGUAL, "Se esperaba '=' después de '" + name + "'");
		ASTNode expr = parseExpression();
		expect(TokenTipo.PUNTO_COMA, "Falta ';' al final de la asignación");
		return new ASTNode(ASTNode.Kind.ASSIGN, name, idTok.getLinea(), expr);
	}

	private ASTNode parseIf() {
		Token ifTok = advance(); // 'si'
		expect(TokenTipo.PARENTESIS_ABR, "Se esperaba '(' después de 'si'");
		ASTNode cond = parseExpression();
		expect(TokenTipo.PARENTESIS_CIE, "Paréntesis desbalanceado: falta ')' en la condición");
		expect(TokenTipo.LLAVE_ABR, "Se esperaba '{' para abrir el bloque del 'si'");
		ASTNode block = new ASTNode(ASTNode.Kind.BLOCK, null, ifTok.getLinea());
		while (!check(TokenTipo.LLAVE_CIE) && !check(TokenTipo.EOF)) {
			block.children.add(parseStatement());
		}
		expect(TokenTipo.LLAVE_CIE, "Falta '}' para cerrar el bloque del 'si'");
		return new ASTNode(ASTNode.Kind.IF, null, ifTok.getLinea(), cond, block);
	}

	private ASTNode parseCall() {
		Token callTok = advance();
		String name = callTok.getValor();
		expect(TokenTipo.PARENTESIS_ABR, "Se esperaba '(' después de '" + name + "'");
		ASTNode call = new ASTNode(ASTNode.Kind.CALL, name, callTok.getLinea());
		if (!check(TokenTipo.PARENTESIS_CIE)) {
			do {
				call.children.add(parseExpression());
			} while (match(TokenTipo.COMA));
		}
		expect(TokenTipo.PARENTESIS_CIE, "Paréntesis desbalanceado: falta ')' en la llamada a '" + name + "'");
		expect(TokenTipo.PUNTO_COMA, "Falta ';' al final de la llamada");
		return call;
	}

	// ---------- expresiones ----------
	private ASTNode parseExpression() {
		ASTNode left = parseAdditive();
		if (check(TokenTipo.MENOR) || check(TokenTipo.MAYOR) || check(TokenTipo.IGUAL_IGUAL)
				|| check(TokenTipo.DIFERENTE)) {
			Token opTok = advance();
			ASTNode right = parseAdditive();
			return new ASTNode(ASTNode.Kind.BINARY, opTok.getValor(), opTok.getLinea(), left, right);
		}
		return left;
	}

	private ASTNode parseAdditive() {
		ASTNode left = parseTerm();
		while (check(TokenTipo.MAS) || check(TokenTipo.MENOS)) {
			Token opTok = advance();
			left = new ASTNode(ASTNode.Kind.BINARY, opTok.getValor(), opTok.getLinea(), left, parseTerm());
		}
		return left; // asociativo a la izquierda: a - b - c = (a - b) - c
	}

	private ASTNode parseTerm() {
		ASTNode left = parseFactor();
		while (check(TokenTipo.MULTIPLICACION) || check(TokenTipo.DIVISION)) {
			Token opTok = advance();
			left = new ASTNode(ASTNode.Kind.BINARY, opTok.getValor(), opTok.getLinea(), left, parseFactor());
		}
		return left;
	}

	private ASTNode parseFactor() {
		Token t = peek();
		switch (t.getTipo()) {
		case NUMERO_ENTERO:
		case NUMERO_DECIMAL:
			advance();
			return new ASTNode(ASTNode.Kind.NUMBER, t.getValor(), t.getLinea());
		case IDENTIFICADOR:
			Token idTok = advance();
			return new ASTNode(ASTNode.Kind.VAR, idTok.getValor(), idTok.getLinea());
		case PARENTESIS_ABR: {
			advance();
			ASTNode inner = parseExpression();
			expect(TokenTipo.PARENTESIS_CIE, "Paréntesis desbalanceado: falta ')'");
			return inner;
		}
		case PUNTO_COMA:
			throw new CompilerException("Línea " + t.getLinea() + ": Asignación sin expresión: falta el valor antes de ';'");
		case DESCONOCIDO:
			throw new CompilerException("Línea " + t.getLinea() + ": Símbolo no admitido: '" + t.getValor() + "'");
		default:
			throw new CompilerException("Línea " + t.getLinea() + ": Expresión inválida cerca de '" + t.getValor() + "'");
		}
	}
}