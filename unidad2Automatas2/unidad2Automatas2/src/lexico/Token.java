package lexico;

/**
 * Representa un token individual: tipo, valor léxico y línea donde aparece.
 */
public class Token {

	private final TipoToken tipo;
	private final String valor;
	private final int linea;

	public Token(TipoToken tipo, String valor, int linea) {
		this.tipo = tipo;
		this.valor = valor;
		this.linea = linea;
	}

	// ── Getters ─────────────────────────────────────────────
	public TipoToken getTipo() {
		return tipo;
	}

	public String getValor() {
		return valor;
	}

	public int getLinea() {
		return linea;
	}

	@Override
	public String toString() {
		return String.format("Token(%-18s | %-15s | línea %d)", tipo, valor, linea);
	}
}
