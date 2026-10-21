package lexico;

import java.util.ArrayList;
import java.util.List;

/**
 * Analizador Léxico:
 * Recorre el texto de entrada carácter a carácter y produce una lista de tokens.
 * Detecta errores léxicos como símbolos no admitidos en el alfabeto del lenguaje.
 */
public class AnalizadorLexico {

	private final String fuente; // código fuente completo
	private int posicion; // índice actual
	private int linea; // línea actual (para mensajes de error)
	private final List<Token> tokens; // lista resultado
	private final List<String> errores; // errores encontrados

	// ── Constructor ─────────────────────────────────────────
	public AnalizadorLexico(String fuente) {
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

		tokens.add(new Token(TipoToken.EOF, "EOF", linea));
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
		TipoToken tipo = palabraClave(lexema);
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
		TipoToken tipo = esDecimal ? TipoToken.NUMERO_DECIMAL : TipoToken.NUMERO_ENTERO;
		tokens.add(new Token(tipo, lexema, linea));
	}

	/** Lee un símbolo de un carácter (o dos en ==, !=). */
	private void leerSimbolo() {
		char c = actual();
		avanzar();

		switch (c) {
		case '+':
			tokens.add(new Token(TipoToken.MAS, "+", linea));
			break;
		case '-':
			tokens.add(new Token(TipoToken.MENOS, "-", linea));
			break;
		case '*':
			tokens.add(new Token(TipoToken.MULTIPLICACION, "*", linea));
			break;
		case '/':
			tokens.add(new Token(TipoToken.DIVISION, "/", linea));
			break;
		case '>':
			tokens.add(new Token(TipoToken.MAYOR, ">", linea));
			break;
		case '<':
			tokens.add(new Token(TipoToken.MENOR, "<", linea));
			break;
		case '(':
			tokens.add(new Token(TipoToken.PARENTESIS_ABR, "(", linea));
			break;
		case ')':
			tokens.add(new Token(TipoToken.PARENTESIS_CIE, ")", linea));
			break;
		case '{':
			tokens.add(new Token(TipoToken.LLAVE_ABR, "{", linea));
			break;
		case '}':
			tokens.add(new Token(TipoToken.LLAVE_CIE, "}", linea));
			break;
		case ';':
			tokens.add(new Token(TipoToken.PUNTO_COMA, ";", linea));
			break;
		case ',':
			tokens.add(new Token(TipoToken.COMA, ",", linea));
			break;
		case '\n':
			linea++;
			break;

		case '=':
			// puede ser '=' o '=='
			if (!esFin() && actual() == '=') {
				avanzar();
				tokens.add(new Token(TipoToken.IGUAL_IGUAL, "==", linea));
			} else {
				tokens.add(new Token(TipoToken.IGUAL, "=", linea));
			}
			break;

		case '!':
			// debe ser '!='
			if (!esFin() && actual() == '=') {
				avanzar();
				tokens.add(new Token(TipoToken.DIFERENTE, "!=", linea));
			} else {
				errores.add("Línea " + linea + ": Símbolo no admitido '!' (se esperaba '!=')");
				tokens.add(new Token(TipoToken.DESCONOCIDO, "!", linea));
			}
			break;

		default:
			// símbolo no reconocido
			errores.add("Línea " + linea + ": Símbolo no admitido '" + c + "'");
			tokens.add(new Token(TipoToken.DESCONOCIDO, String.valueOf(c), linea));
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
	private TipoToken palabraClave(String palabra) {
		switch (palabra) {
		case "entero":
			return TipoToken.ENTERO;
		case "decimal":
			return TipoToken.DECIMAL;
		case "si":
			return TipoToken.SI;
		case "imprimir":
			return TipoToken.IMPRIMIR;
		default:
			return TipoToken.IDENTIFICADOR;
		}
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
