package lexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Recorre el texto de entrada carácter a carácter y produce una lista de
 * tokens.
 *
 * Detecta errores como: símbolos no admitidos, paréntesis desbalanceados y
 * cadenas vacías.
 */
public class Lexer {

	private final String fuente; // código fuente completo
	private int posicion; // índice actual
	private int linea; // línea actual (para mensajes de error)
	private final List<Token> tokens; // lista resultado
	private final List<String> errores; // errores encontrados

	// ── Constructor ─────────────────────────────────────────
	public Lexer(String fuente) {
		this.fuente = fuente;
		this.posicion = 0;
		this.linea = 1;
		this.tokens = new ArrayList<>();
		this.errores = new ArrayList<>();
	}

	// ── API pública ─────────────────────────────────────────

	/**
	 * Ejecuta el análisis léxico completo.
	 * 
	 * @return lista de tokens (incluyendo EOF al final)
	 */
	public List<Token> tokenizar() {
		while (!esFin()) {
			saltarEspacios();
			if (esFin())
				break;

			char c = actual();

			if (Character.isLetter(c) || c == '_') {
				leerPalabraOIdentificador();
			} else if (Character.isDigit(c)) {
				leerNumero();
			} else {
				leerSimbolo();
			}
		}

		tokens.add(new Token(TokenTipo.EOF, "EOF", linea));
		verificarBalanceoParentesis();
		return tokens;
	}

	public List<String> getErrores() {
		return errores;
	}

	// ── Métodos privados de lectura ──────────────────────────

	/** Lee una palabra clave o identificador. */
	private void leerPalabraOIdentificador() {
		int inicio = posicion;

		while (!esFin() && (Character.isLetterOrDigit(actual()) || actual() == '_')) {
			avanzar();
		}

		String lexema = fuente.substring(inicio, posicion);
		TokenTipo tipo = palabraClave(lexema);
		tokens.add(new Token(tipo, lexema, linea));
	}

	/** Lee un número entero o decimal. */
	private void leerNumero() {
		int inicio = posicion;
		boolean esDecimal = false;

		while (!esFin() && Character.isDigit(actual()))
			avanzar();

		// punto decimal
		if (!esFin() && actual() == '.' && posicion + 1 < fuente.length()
				&& Character.isDigit(fuente.charAt(posicion + 1))) {
			esDecimal = true;
			avanzar(); // consume el '.'
			while (!esFin() && Character.isDigit(actual()))
				avanzar();
		}

		String lexema = fuente.substring(inicio, posicion);
		TokenTipo tipo = esDecimal ? TokenTipo.NUMERO_DECIMAL : TokenTipo.NUMERO_ENTERO;
		tokens.add(new Token(tipo, lexema, linea));
	}

	/** Lee un símbolo de un carácter (o dos en ==, !=). */
	private void leerSimbolo() {
		char c = actual();
		avanzar();

		switch (c) {
		case '+':
			tokens.add(new Token(TokenTipo.MAS, "+", linea));
			break;
		case '-':
			tokens.add(new Token(TokenTipo.MENOS, "-", linea));
			break;
		case '*':
			tokens.add(new Token(TokenTipo.MULTIPLICACION, "*", linea));
			break;
		case '/':
			tokens.add(new Token(TokenTipo.DIVISION, "/", linea));
			break;
		case '>':
			tokens.add(new Token(TokenTipo.MAYOR, ">", linea));
			break;
		case '<':
			tokens.add(new Token(TokenTipo.MENOR, "<", linea));
			break;
		case '(':
			tokens.add(new Token(TokenTipo.PARENTESIS_ABR, "(", linea));
			break;
		case ')':
			tokens.add(new Token(TokenTipo.PARENTESIS_CIE, ")", linea));
			break;
		case '{':
			tokens.add(new Token(TokenTipo.LLAVE_ABR, "{", linea));
			break;
		case '}':
			tokens.add(new Token(TokenTipo.LLAVE_CIE, "}", linea));
			break;
		case ';':
			tokens.add(new Token(TokenTipo.PUNTO_COMA, ";", linea));
			break;
		case ',':
			tokens.add(new Token(TokenTipo.COMA, ",", linea));
			break;
		case '\n':
			linea++;
			break;

		case '=':
			// puede ser '=' o '=='
			if (!esFin() && actual() == '=') {
				avanzar();
				tokens.add(new Token(TokenTipo.IGUAL_IGUAL, "==", linea));
			} else {
				tokens.add(new Token(TokenTipo.IGUAL, "=", linea));
			}
			break;

		case '!':
			// debe ser '!='
			if (!esFin() && actual() == '=') {
				avanzar();
				tokens.add(new Token(TokenTipo.DIFERENTE, "!=", linea));
			} else {
				errores.add("ERROR léxico en línea " + linea + ": '!' sin '=' — símbolo no admitido.");
				tokens.add(new Token(TokenTipo.DESCONOCIDO, "!", linea));
			}
			break;

		default:
			// símbolo no reconocido
			errores.add("ERROR léxico en línea " + linea + ": símbolo no admitido '" + c + "'");
			tokens.add(new Token(TokenTipo.DESCONOCIDO, String.valueOf(c), linea));
		}
	}

	// ── Utilidades ───────────────────────────────────────────

	/** Salta espacios, tabulaciones y retornos de carro. */
	private void saltarEspacios() {
		while (!esFin()) {
			char c = actual();
			if (c == ' ' || c == '\t' || c == '\r') {
				avanzar();
			} else if (c == '\n') {
				linea++;
				avanzar();
			} else {
				break;
			}
		}
	}

	/** Determina si una palabra es clave del lenguaje o un identificador. */
	private TokenTipo palabraClave(String palabra) {
		switch (palabra) {
		case "entero":
			return TokenTipo.ENTERO;
		case "decimal":
			return TokenTipo.DECIMAL;
		case "si":
			return TokenTipo.SI;
		case "imprimir":
			return TokenTipo.IMPRIMIR;
		default:
			return TokenTipo.IDENTIFICADOR;
		}
	}

	/**
	 * Verifica que los paréntesis estén balanceados recorriendo los tokens ya
	 * generados.
	 */
	private void verificarBalanceoParentesis() {
		int contadorPar = 0;
		int contadorLlave = 0;

		for (Token t : tokens) {
			switch (t.getTipo()) {
			case PARENTESIS_ABR:
				contadorPar++;
				break;
			case PARENTESIS_CIE:
				contadorPar--;
				break;
			case LLAVE_ABR:
				contadorLlave++;
				break;
			case LLAVE_CIE:
				contadorLlave--;
				break;
			default:
				break;
			}

			if (contadorPar < 0) {
				errores.add("ERROR léxico en línea " + t.getLinea() + ": ')' sin '(' correspondiente.");
				contadorPar = 0;
			}
			if (contadorLlave < 0) {
				errores.add("ERROR léxico en línea " + t.getLinea() + ": '}' sin '{' correspondiente.");
				contadorLlave = 0;
			}
		}

		if (contadorPar > 0)
			errores.add("ERROR léxico: falta(n) " + contadorPar + " paréntesis de cierre ')'.");
		if (contadorLlave > 0)
			errores.add("ERROR léxico: falta(n) " + contadorLlave + " llave(s) de cierre '}'.");
	}

	private char actual() {
		return fuente.charAt(posicion);
	}

	private void avanzar() {
		posicion++;
	}

	private boolean esFin() {
		return posicion >= fuente.length();
	}
}