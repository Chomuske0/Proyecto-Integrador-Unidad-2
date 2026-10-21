package lexico;

public enum TipoToken {

	// Tipos de dato
	ENTERO, // palabra clave "entero"
	DECIMAL, // palabra clave "decimal"

	// Identificadores y literales
	IDENTIFICADOR, // nombres de variables: resultado, a, b...
	NUMERO_ENTERO, // literales enteros: 8, 3, 2...
	NUMERO_DECIMAL, // literales decimales: 3.14, 2.5...

	// Operadores aritméticos
	MAS, // +
	MENOS, // -
	MULTIPLICACION, // *
	DIVISION, // /

	// Operadores de comparación
	MAYOR, // >
	MENOR, // <
	IGUAL_IGUAL, // ==
	DIFERENTE, // !=

	// Asignación
	IGUAL, // =

	// Delimitadores
	PARENTESIS_ABR, // (
	PARENTESIS_CIE, // )
	LLAVE_ABR, // {
	LLAVE_CIE, // }
	PUNTO_COMA, // ;
	COMA, // ,

	// Palabras clave
	SI, // "si"
	IMPRIMIR, // "imprimir"

	// Control
	EOF, // fin de archivo / entrada
	DESCONOCIDO // símbolo no reconocido (para manejo de errores)
}
