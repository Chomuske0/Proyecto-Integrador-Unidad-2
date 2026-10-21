# MiniComp: Generador de Código Intermedio

- **LENGUAJES Y AUTÓMATAS II**
- **GRUPO:** 6N1
- **UNIDAD 2:** GENERACIÓN DE CÓDIGO INTERMEDIO

# Nombres de Equipo:
- Estrada Eligio Eric
- Rojas Hernandez Pamela 
- Rene Castañeda Blancas David 
- Morales Guzmán Frida Ivette 
# Número de Equipo:
3

---

# GENERADOR DE CÓDIGO INTERMEDIO, TABLA DE SÍMBOLOS, NOTACIONES, CUÁDRUPLOS Y TRIPLOS

## Descripción

El proyecto consiste en desarrollar una herramienta didáctica denominada **MiniComp**, implementada en Java, capaz de procesar instrucciones escritas en un pseudolenguaje estructurado y generar las distintas representaciones intermedias que utiliza un compilador antes de traducir el código a lenguaje máquina.

El sistema recibe el código fuente (ingresado directamente en un editor o cargado desde un archivo de texto `.txt`) y realiza el proceso completo de análisis y síntesis intermedia:
1. **Análisis léxico:** Reconoce y clasifica los componentes léxicos (palabras clave, identificadores, constantes numéricas enteras y decimales, operadores y delimitadores), reportando símbolos no válidos.
2. **Análisis sintáctico:** Emplea un parser descendente recursivo que valida la gramática del programa, delimitadores y estructura de bloques, construyendo un Árbol de Sintaxis Abstracta (AST) jerárquico respetando la precedencia de operadores.
3. **Análisis semántico:** Recorre el AST para comprobar que los identificadores utilizados hayan sido declarados, detectar variables duplicadas y validar que no haya autorreferencias en la inicialización.
4. **Tabla de símbolos:** Registra las variables validadas, su tipo de dato (`entero`, `decimal`) y su valor o expresión inicial.
5. **Conversor de notaciones:** Traduce cada expresión aritmética del programa a sus tres formas canónicas: **Infija** (con la cantidad mínima requerida de paréntesis), **Prefija** (polaca) y **Postfija** (polaca inversa).
6. **Generador de código intermedio:** Recorre el AST en postorden para producir código de tres direcciones en dos formatos estándar:
   - **Cuádruplos:** Instrucciones `(operador, argumento 1, argumento 2, resultado)` utilizando temporales explícitos (`t1, t2...`).
   - **Triplos:** Instrucciones `(operador, argumento 1, argumento 2)` referenciando resultados parciales mediante el índice de instrucción `(1), (2)...`.
   - Soporte para estructuras de control condicional (`si`) mediante etiquetas y saltos simbólicos (`IF_FALSE`, `LABEL`) y llamadas a funciones (`PARAM`, `CALL`).

Los resultados se visualizan a través de una interfaz gráfica desarrollada en Java Swing organizada por pestañas y tablas interactivas.

---

## Objetivo

Desarrollar en Java un software educativo capaz de analizar programas escritos en un pseudolenguaje definido, construir su tabla de símbolos, traducir expresiones a notaciones infija, prefija y postfija, y generar código intermedio coherente en forma de triplos y cuádruplos, incluyendo condicionales y llamadas a funciones, con un manejo claro de errores de entrada.

## Objetivos específicos

- Implementar un analizador léxico que tokenice el código fuente e identifique errores como caracteres inválidos y paréntesis desbalanceados.
- Implementar un analizador sintáctico descendente recursivo con comprobación estricta de variables no declaradas o duplicadas.
- Construir un Árbol de Sintaxis Abstracta (AST) que modele fielmente la precedencia de operadores aritméticos y relacionales.
- Generar y mantener una tabla de símbolos con identificadores, tipos y valores iniciales.
- Convertir expresiones aritméticas a notación infija, prefija y postfija sin ambigüedades.
- Generar código intermedio en formato de **cuádruplos** gestionando variables temporales de forma consistente.
- Generar código intermedio en formato de **triplos** mediante punteros y referencias relativas de instrucción.
- Traducir instrucciones de salto condicional (`si`) con etiquetas simbólicas.
- Representar llamadas a funciones del sistema (`imprimir`) o definidas por el usuario.
- Proveer una interfaz gráfica intuitiva que permita editar código, abrir archivos `.txt`, ejecutar pruebas precargadas y visualizar resultados sin necesidad de reiniciar la aplicación.

---

## Tecnología Utilizada

- **Lenguaje de Programación:** Java (JDK 11+).
- **Interfaz Gráfica:** Java Swing y AWT (`JFrame`, `JTextArea`, `JTabbedPane`, `JTable`, `DefaultTableModel`, `JSplitPane`, `JFileChooser`, `JComboBox`).
- **Arquitectura de Software:** Modular desacoplada dividida en paquetes especializados (`lexer`, `parser`, `semantic`, `symboltable`, `notations`, `ir`).

---

## Funcionamiento del Proyecto

El flujo de procesamiento del programa sigue las fases clásicas del front-end y mid-end de un compilador:

```text
[ Código Fuente (.txt o Teclado) ]
                │
                ▼
        [ Analizador Léxico ]  ───►  Convierte caracteres en Tokens y detecta símbolos no admitidos
                │ (Tokens)
                ▼
      [ Analizador Sintáctico ]  ───►  Valida gramática, delimitadores y construye el AST
                │ (AST)
                ▼
      [ Analizador Semántico ]  ───►  Valida variables declaradas/duplicadas y genera la Tabla de Símbolos
                │
                ├──────────────────────┬─────────────────────────┐
                ▼                      ▼                         ▼
      [ Tabla de Símbolos ]   [ Notaciones ]         [ Código Intermedio ]
                               (Infija/Pref/Post)      (Cuádruplos y Triplos)
                │                      │                         │
                └──────────────────────┼─────────────────────────┘
                                       ▼
                        [ Visualización en Pestañas GUI ]
```

---

## Gramática y Reglas del Lenguaje

El pseudolenguaje admitido por MiniComp se define mediante la siguiente gramática libre de contexto en notación EBNF:

```ebnf
programa        ::= instruccion* EOF
instruccion     ::= declaracion | asignacion | condicional | llamada

declaracion     ::= ("entero" | "decimal") IDENTIFICADOR [ "=" expresion ] ";"
asignacion      ::= IDENTIFICADOR "=" expresion ";"
condicional     ::= "si" "(" expresion ")" "{" instruccion* "}"
llamada         ::= IDENTIFICADOR "(" [ expresion ( "," expresion )* ] ")" ";"

expresion       ::= relacional
relacional      ::= aditiva [ ( "<" | ">" | "==" | "!=" ) aditiva ]
aditiva         ::= multiplicativa [ ( "+" | "-" ) multiplicativa ]*
multiplicativa  ::= factor [ ( "*" | "/" ) factor ]*
factor          ::= NUMERO_ENTERO 
                  | NUMERO_DECIMAL 
                  | IDENTIFICADOR 
                  | "(" expresion ")"
```

### Precedencia y Asociatividad de Operadores

| Nivel | Operadores | Descripción | Asociatividad |
|:-----:|:----------:|-------------|:-------------:|
| 1 (Más alto) | `( )` | Paréntesis de agrupación | No aplica |
| 2 | `*`, `/` | Multiplicación y división | Izquierda a derecha |
| 3 | `+`, `-` | Suma y resta | Izquierda a derecha |
| 4 (Más bajo) | `<`, `>`, `==`, `!=` | Comparadores relacionales | Izquierda a derecha |

### Limitaciones del Subconjunto
- No implementa estructuras de control cíclicas (`mientras`, `para`).
- No cuenta con bloque alternativo `sino` (else).
- Los tipos admitidos son exclusivamente escalares: `entero` y `decimal` (no se admiten arreglos ni cadenas de texto).
- Las funciones son tratadas como instrucciones de efecto colateral o procedimientos (`imprimir(...)`), no como expresiones que devuelvan valores dentro de operaciones compuestas.

---

## Tabla de Tokens Reconocidos

| Categoría | Tokens / Lexemas | Descripción |
|-----------|------------------|-------------|
| **Tipos de dato** | `entero`, `decimal` | Declaración de variables numéricas |
| **Palabras clave** | `si`, `imprimir` | Sentencia condicional y función de salida |
| **Identificadores** | `resultado`, `total`, `precio`, `a`, `b`... | Letras o guion bajo seguidos de alfanuméricos |
| **Constantes enteras** | `8`, `3`, `100`, `0` | Secuencia de dígitos |
| **Constantes decimales**| `3.14`, `12.5`, `0.5` | Dígitos con punto flotante |
| **Operadores aritméticos** | `+`, `-`, `*`, `/` | Operaciones básicas |
| **Operadores relacionales** | `<`, `>`, `==`, `!=` | Comparaciones booleanas |
| **Asignación** | `=` | Asignación de valor a variable |
| **Delimitadores** | `(`, `)`, `{`, `}`, `;`, `,` | Control de agrupación, bloques y fin de sentencia |

---

## Pruebas Mínimas Obligatorias

El sistema incluye las 6 pruebas fundamentales requeridas por la especificación del proyecto, accesibles directamente desde el menú desplegable de la interfaz:

### 1. Precedencia de Operadores
* **Código de prueba:**
  ```text
  entero x;
  x = 4 + 3 * 2;
  ```
* **Notaciones generadas:**
  - **Infija:** `4 + 3 * 2`
  - **Prefija:** `+ 4 * 3 2`
  - **Postfija:** `4 3 2 * +`
* **Cuádruplos:**
  1. `(*, 3, 2, t1)`
  2. `(+, 4, t1, t2)`
  3. `(=, t2, -, x)`
* **Triplos:**
  1. `(*, 3, 2)`
  2. `(+, 4, (1))`
  3. `(=, (2), x)`

---

### 2. Agrupación con Paréntesis
* **Código de prueba:**
  ```text
  entero x;
  x = (4 + 3) * 2;
  ```
* **Notaciones generadas:**
  - **Infija:** `(4 + 3) * 2`
  - **Prefija:** `* + 4 3 2`
  - **Postfija:** `4 3 + 2 *`
* **Cuádruplos:**
  1. `(+, 4, 3, t1)`
  2. `(*, t1, 2, t2)`
  3. `(=, t2, -, x)`
* **Triplos:**
  1. `(+, 4, 3)`
  2. `(*, (1), 2)`
  3. `(=, (2), x)`

---

### 3. Asignación con Múltiples Variables
* **Código de prueba:**
  ```text
  entero precio = 20;
  entero cantidad = 6;
  entero total;
  total = precio * cantidad;
  ```
* **Tabla de Símbolos:**
  | Identificador | Tipo | Valor inicial |
  |---------------|------|:-------------:|
  | `precio` | `entero` | `20` |
  | `cantidad` | `entero` | `6` |
  | `total` | `entero` | `-` |
* **Cuádruplos:**
  1. `(=, 20, -, precio)`
  2. `(=, 6, -, cantidad)`
  3. `(*, precio, cantidad, t1)`
  4. `(=, t1, -, total)`
* **Triplos:**
  1. `(=, 20, precio)`
  2. `(=, 6, cantidad)`
  3. `(*, precio, cantidad)`
  4. `(=, (3), total)`

---

### 4. Estructura Condicional (`si`)
* **Código de prueba:**
  ```text
  entero total = 120;
  si (total > 100) {
      total = total - 10;
  }
  ```
* **Cuádruplos:**
  1. `(=, 120, -, total)`
  2. `(>, total, 100, t1)`
  3. `(IF_FALSE, t1, -, L1)`
  4. `(-, total, 10, t2)`
  5. `(=, t2, -, total)`
  6. `(LABEL, -, -, L1)`
* **Triplos:**
  1. `(=, 120, total)`
  2. `(>, total, 100)`
  3. `(IF_FALSE, (2), L1)`
  4. `(-, total, 10)`
  5. `(=, (4), total)`
  6. `(LABEL, L1, -)`

---

### 5. Llamada a Funciones (`imprimir`)
* **Código de prueba:**
  ```text
  entero total = 120;
  imprimir(total);
  ```
* **Cuádruplos:**
  1. `(=, 120, -, total)`
  2. `(PARAM, total, -, -)`
  3. `(CALL, imprimir, 1, -)`
* **Triplos:**
  1. `(=, 120, total)`
  2. `(PARAM, total, -)`
  3. `(CALL, imprimir, 1)`

---

### 6. Pruebas de Detección y Manejo de Errores

El compilador valida sintáctica y semánticamente el código antes de emitir representaciones intermedias, informando la causa del error y la línea correspondiente:

| Tipo de Error | Código de Entrada | Mensaje Reportado |
|:--------------|:------------------|:------------------|
| **Asignación sin expresión** | `entero x; x = ;` | `• Línea 2: Asignación sin expresión: falta el valor antes de ';'` |
| **Paréntesis desbalanceado** | `entero x; x = (4 + 3;` | `• Línea 2: Paréntesis desbalanceado: falta ')' (se encontró ';')` |
| **Variable no declarada** | `resultado = a + 1;` | `• Línea 1: Identificador no declarado: 'resultado'` |
| **Doble declaración** | `entero a; entero a;` | `• Línea 1: La variable 'a' ya fue declarada` |
| **Símbolo no admitido** | `entero x = 10 @ 2;` | `• Línea 1: Símbolo no admitido '@'` |
| **Entrada vacía** | *(texto en blanco)* | `• La entrada está vacía.` |

---

### 7. Programa Completo Integrador

* **Código de entrada:**
  ```text
  entero a = 8;
  entero b = 3;
  entero resultado;
  resultado = a + b * 2;
  si (resultado > 10) {
      resultado = resultado - 1;
  }
  imprimir(resultado);
  ```

* **Cuádruplos generados:**
  | # | Operador | Arg 1 | Arg 2 | Resultado |
  |:-:|:--------:|:-----:|:-----:|:---------:|
  | (1) | `=` | `8` | `-` | `a` |
  | (2) | `=` | `3` | `-` | `b` |
  | (3) | `*` | `b` | `2` | `t1` |
  | (4) | `+` | `a` | `t1` | `t2` |
  | (5) | `=` | `t2` | `-` | `resultado` |
  | (6) | `>` | `resultado` | `10` | `t3` |
  | (7) | `IF_FALSE` | `t3` | `-` | `L1` |
  | (8) | `-` | `resultado` | `1` | `t4` |
  | (9) | `=` | `t4` | `-` | `resultado` |
  | (10)| `LABEL` | `-` | `-` | `L1` |
  | (11)| `PARAM` | `resultado` | `-` | `-` |
  | (12)| `CALL` | `imprimir` | `1` | `-` |

* **Triplos generados:**
  | # | Operador | Arg 1 | Arg 2 |
  |:-:|:--------:|:-----:|:-----:|
  | (1) | `=` | `8` | `a` |
  | (2) | `=` | `3` | `b` |
  | (3) | `*` | `b` | `2` |
  | (4) | `+` | `a` | `(3)` |
  | (5) | `=` | `(4)` | `resultado` |
  | (6) | `>` | `resultado` | `10` |
  | (7) | `IF_FALSE` | `(6)` | `L1` |
  | (8) | `-` | `resultado` | `1` |
  | (9) | `=` | `(8)` | `resultado` |
  | (10)| `LABEL` | `L1` | `-` |
  | (11)| `PARAM` | `resultado` | `-` |
  | (12)| `CALL` | `imprimir` | `1` |

---

## Código Intermedio: Cuádruplos vs. Triplos

| Característica | Cuádruplos | Triplos |
|:--------------|:-----------|:--------|
| **Estructura** | 4 campos: `(op, arg1, arg2, resultado)` | 3 campos: `(op, arg1, arg2)` |
| **Manejo de resultados** | Almacenados en variables temporales explícitas (`t1, t2...`). | Referenciados por la posición o índice de la instrucción previa `(n)`. |
| **Optimización y reordenamiento** | Muy flexible: mover instrucciones no invalida nombres de variables. | Más complejo: mover o eliminar instrucciones requiere actualizar los índices numéricos de referencia. |
| **Consumo de memoria** | Ocupa más espacio por el registro explícito del resultado. | Más compacto al prescindir del cuarto campo. |

---

## Instrucciones de Compilación y Ejecución

### Requisitos
- **Java Development Kit (JDK) 11 o superior**.

### 1. Desde Eclipse IDE
1. Abrir Eclipse y seleccionar **File > Import... > General > Existing Projects into Workspace**.
2. Seleccionar la carpeta `unidad2Automatas2/unidad2Automatas2`.
3. Ejecutar la clase `Main.java` como **Java Application**.

### 2. Desde Terminal
```bash
javac -encoding UTF-8 -d bin -sourcepath unidad2Automatas2/unidad2Automatas2/src unidad2Automatas2/unidad2Automatas2/src/Main.java
java -cp bin Main
```

---

## Conclusión

El proyecto **MiniComp** integra de forma práctica y visual las fases de análisis y síntesis de un compilador para la Unidad 2 de Lenguajes y Autómatas II. A través de la descomposición modular del código fuente, la construcción del AST y la traducción simultánea a notaciones matemáticas, cuádruplos y triplos, se proporciona una comprensión clara de los mecanismos que permiten transformar código de alto nivel en representaciones estructuradas aptas para la generación de código máquina y su posterior optimización.
