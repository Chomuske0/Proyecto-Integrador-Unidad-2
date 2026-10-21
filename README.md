# MiniComp — Generador de Código Intermedio
**Proyecto Integrador — Unidad 2 | Lenguajes y Autómatas II**

MiniComp es un prototipo didáctico de compilador que analiza un pseudolenguaje en español y genera representaciones intermedias del código fuente: **Tabla de Símbolos**, conversiones a notación **Infija, Prefija y Postfija**, así como **Cuádruplos** y **Triplos**.

---

## 📋 Características Principales

1. **Lectura y Validación Léxica/Sintáctica:**
   - Análisis léxico con reporte de línea y verificación de balanceo de delimitadores.
   - Parser descendente recursivo con Árbol de Sintaxis Abstracta (AST).
   - Detección de errores comunes: asignación sin expresión, identificadores no declarados, redeclaraciones y símbolos no permitidos.
2. **Tabla de Símbolos:**
   - Registro de variables con nombre, tipo de dato (`entero`, `decimal`) y valor inicial.
3. **Conversión de Notaciones:**
   - Conversión de expresiones binarias a notación **Infija** (paréntesis mínimos), **Prefija** y **Postfija** respetando la precedencia de operadores.
4. **Código Intermedio:**
   - **Cuádruplos:** Instrucciones de formato `(operador, arg1, arg2, resultado)` empleando variables temporales (`t1, t2...`).
   - **Triplos:** Instrucciones de formato `(operador, arg1, arg2)` referenciando resultados previos por número de instrucción `(1), (2)...`.
   - Soporte para saltos condicionales (`IF_FALSE`, etiquetas `LABEL`) y llamadas a funciones (`PARAM`, `CALL`).
5. **Interfaz Gráfica (Swing):**
   - Editor de entrada con atajo `Ctrl+Enter` para procesar.
   - Pestañas organizadas por resultados.
   - Selector de pruebas mínimas precargadas y botón para cargar archivos `.txt`.

---

## 📐 Gramática Soportada

```ebnf
programa        -> instruccion* EOF
instruccion     -> declaracion | asignacion | condicional | llamada
declaracion     -> ("entero" | "decimal") IDENTIFICADOR ["=" expresion] ";"
asignacion      -> IDENTIFICADOR "=" expresion ";"
condicional     -> "si" "(" expresion ")" "{" instruccion* "}"
llamada         -> IDENTIFICADOR "(" [expresion ("," expresion)*] ")" ";"

expresion       -> relacional
relacional      -> aditiva [("<" | ">" | "==" | "!=") aditiva]
aditiva         -> multiplicativa [("+" | "-") multiplicativa]*
multiplicativa  -> factor [("*" | "/") factor]*
factor          -> NUMERO_ENTERO | NUMERO_DECIMAL | IDENTIFICADOR | "(" expresion ")"
```

### Limitaciones Actuales
- No incluye bucles iterativos (`mientras`, `para`).
- No soporta bloques alternativos `sino` (else).
- Los tipos soportados se limitan a `entero` y `decimal`.

---

## 🚀 Requisitos y Ejecución

### Requisitos
- **Java Development Kit (JDK) 11 o superior**.

### Compilación y Ejecución desde Terminal

1. **Compilar el proyecto:**
   ```bash
   javac -d bin -sourcepath unidad2Automatas2/unidad2Automatas2/src unidad2Automatas2/unidad2Automatas2/src/Main.java
   ```

2. **Ejecutar la aplicación:**
   ```bash
   java -cp bin Main
   ```

3. **Ejecutar en Eclipse o IDE:**
   - Abrir Eclipse e importar el proyecto existente seleccionando la carpeta `unidad2Automatas2/unidad2Automatas2`.
   - Ejecutar la clase `Main.java` como *Java Application*.

---

## 📂 Estructura del Proyecto

```text
├── .gitignore
├── README.md
├── ejemplo.txt
├── Proyecto_Integrador_Unidad_2_Generacion_Codigo_Intermedio.pdf
├── Rubrica_Proyecto_Integrador_Unidad_2.pdf
└── unidad2Automatas2/
    └── unidad2Automatas2/
        ├── .classpath
        ├── .project
        └── src/
            ├── Main.java              # Ventana principal e integración
            ├── ir/                    # Generación de Cuádruplos y Triplos
            ├── lexer/                 # Tokenizador y analizador léxico
            ├── notations/             # Conversor Infijo / Prefijo / Postfijo
            ├── parser/                # Parser descendente recursivo y AST
            └── symboltable/           # Tabla de símbolos
```
