import ir.IRGenerator;
import ir.Quadruple;
import ir.Triple;
import lexer.Lexer;
import lexer.Token;
import notations.NotationConverter;
import parser.ASTNode;
import parser.CompilerException;
import parser.Parser;
import symboltable.SymbolTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Interfaz gráfica de MiniComp: generador de código intermedio. */
public class Main extends JFrame {

	private static final int TAB_ERRORES = 4;

	private final JTextArea input = new JTextArea();
	private final JTabbedPane tabs = new JTabbedPane();
	private final JLabel status = new JLabel(" Listo.");

	private final DefaultTableModel symbolsModel = model("Identificador", "Tipo", "Valor inicial");
	private final DefaultTableModel notationsModel = model("Expresión", "Infija", "Prefija", "Postfija");
	private final DefaultTableModel quadsModel = model("#", "Operador", "Arg 1", "Arg 2", "Resultado");
	private final DefaultTableModel triplesModel = model("#", "Operador", "Arg 1", "Arg 2");
	private final JTextArea errors = new JTextArea();

	private final Map<String, String> examples = new LinkedHashMap<>();

	public Main() {
		super("MiniComp — Generador de código intermedio");
		loadExamples();
		buildUI();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(1150, 680);
		setLocationRelativeTo(null);
	}

	// ---------- ejemplos (las pruebas mínimas del PDF) ----------
	private void loadExamples() {
		examples.put("1. Precedencia: x = 4 + 3 * 2", "entero x;\nx = 4 + 3 * 2;");
		examples.put("2. Paréntesis: x = (4 + 3) * 2", "entero x;\nx = (4 + 3) * 2;");
		examples.put("3. Variables: total = precio * cantidad",
				"entero precio = 20;\nentero cantidad = 6;\nentero total;\ntotal = precio * cantidad;");
		examples.put("4. Condicional: si (total > 100)",
				"entero total = 120;\nsi (total > 100) {\n    total = total - 10;\n}");
		examples.put("5. Llamada: imprimir(total)", "entero total = 120;\nimprimir(total);");
		examples.put("6. Error: asignación sin expresión", "entero x;\nx = ;");
		examples.put("7. Programa completo del PDF",
				"entero a = 8;\nentero b = 3;\nentero resultado;\nresultado = a + b * 2;\n"
						+ "si (resultado > 10) {\n    resultado = resultado - 1;\n}\nimprimir(resultado);");
	}

	// ---------- interfaz ----------
	private void buildUI() {
		// Panel izquierdo: entrada
		input.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
		input.setTabSize(4);
		input.setMargin(new Insets(8, 8, 8, 8));
		input.setText(examples.get("7. Programa completo del PDF"));
		// Ctrl+Enter procesa
		input.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "procesar");
		input.getActionMap().put("procesar", new AbstractAction() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				process();
			}
		});

		JButton btnProcess = new JButton("Procesar  (Ctrl+Enter)");
		JButton btnClear = new JButton("Limpiar");
		JButton btnOpen = new JButton("Abrir .txt");
		JComboBox<String> combo = new JComboBox<>();
		combo.addItem("Cargar ejemplo…");
		for (String k : examples.keySet())
			combo.addItem(k);

		btnProcess.addActionListener(e -> process());
		btnClear.addActionListener(e -> {
			input.setText("");
			clearOutput();
			setStatus(" Listo.", null);
		});
		btnOpen.addActionListener(e -> openFile());
		combo.addActionListener(e -> {
			if (combo.getSelectedIndex() > 0) {
				input.setText(examples.get((String) combo.getSelectedItem()));
				process();
			}
		});

		JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
		toolbar.add(btnProcess);
		toolbar.add(btnOpen);
		toolbar.add(btnClear);

		JPanel comboRow = new JPanel(new BorderLayout());
		comboRow.setBorder(new EmptyBorder(0, 6, 6, 6));
		comboRow.add(combo, BorderLayout.CENTER);

		JPanel top = new JPanel(new BorderLayout());
		top.add(toolbar, BorderLayout.NORTH);
		top.add(comboRow, BorderLayout.SOUTH);

		JPanel left = new JPanel(new BorderLayout());
		left.setBorder(BorderFactory.createTitledBorder("Entrada (pseudolenguaje)"));
		left.add(top, BorderLayout.NORTH);
		left.add(new JScrollPane(input), BorderLayout.CENTER);

		// Panel derecho: resultados por secciones
		errors.setEditable(false);
		errors.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
		errors.setMargin(new Insets(10, 10, 10, 10));

		tabs.addTab("Tabla de símbolos", section("Tabla de símbolos", table(symbolsModel)));
		tabs.addTab("Notaciones", section("Notaciones: infija, prefija y postfija", table(notationsModel)));
		tabs.addTab("Cuádruplos", section("Cuádruplos (operador, arg1, arg2, resultado)", table(quadsModel)));
		tabs.addTab("Triplos", section("Triplos (operador, arg1, arg2)", table(triplesModel)));
		tabs.addTab("Errores", section("Errores y mensajes", new JScrollPane(errors)));

		JPanel right = new JPanel(new BorderLayout());
		right.setBorder(BorderFactory.createTitledBorder("Resultados"));
		right.add(tabs, BorderLayout.CENTER);

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
		split.setResizeWeight(0.35);

		status.setBorder(new EmptyBorder(4, 6, 4, 6));
		status.setOpaque(true);

		getContentPane().add(split, BorderLayout.CENTER);
		getContentPane().add(status, BorderLayout.SOUTH);
	}

	private JPanel section(String title, JComponent content) {
		JLabel label = new JLabel(title);
		label.setFont(label.getFont().deriveFont(Font.BOLD, 15f));
		label.setBorder(new EmptyBorder(8, 8, 6, 8));
		JPanel p = new JPanel(new BorderLayout());
		p.add(label, BorderLayout.NORTH);
		p.add(content, BorderLayout.CENTER);
		return p;
	}

	private static DefaultTableModel model(String... cols) {
		return new DefaultTableModel(cols, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};
	}

	private JScrollPane table(DefaultTableModel m) {
		JTable t = new JTable(m);
		t.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
		t.setRowHeight(26);
		t.getTableHeader().setReorderingAllowed(false);
		t.setFillsViewportHeight(true);
		return new JScrollPane(t);
	}

	// ---------- acciones ----------
	private void openFile() {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos de texto (.txt)", "txt"));
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			try {
				input.setText(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
				setStatus(" Archivo cargado: " + f.getName(), null);
			} catch (Exception ex) {
				showErrors(Collections.singletonList("No se pudo leer el archivo: " + ex.getMessage()));
			}
		}
	}

	/**
	 * Ejecuta todo el recorrido: lexer → parser → notaciones → código intermedio.
	 */
	private void process() {
		clearOutput();
		String src = input.getText();
		if (src.trim().isEmpty()) {
			showErrors(Collections.singletonList("La entrada está vacía."));
			return;
		}

		// 1. Análisis léxico
		Lexer lexer = new Lexer(src);
		List<Token> tokens = lexer.tokenizar();
		if (!lexer.getErrores().isEmpty()) {
			showErrors(lexer.getErrores());
			return;
		}

		try {
			// 2. Análisis sintáctico
			ASTNode program = new Parser(tokens).parseProgram();

			// 3. Tabla de símbolos
			for (SymbolTable.Entry e : SymbolTable.fromAST(program).getEntries()) {
				symbolsModel.addRow(new Object[] { e.id, e.type, e.initialValue });
			}

			// 4. Notaciones (solo expresiones con operadores)
			List<ASTNode> exprs = new ArrayList<>();
			collectExpressions(program, exprs);
			for (ASTNode ex : exprs) {
				notationsModel.addRow(new Object[] { NotationConverter.toInfix(ex), NotationConverter.toInfix(ex),
						NotationConverter.toPrefix(ex), NotationConverter.toPostfix(ex) });
			}

			// 5. Código intermedio
			IRGenerator gen = new IRGenerator();
			gen.generate(program);
			int i = 1;
			for (Quadruple q : gen.getQuadruples()) {
				quadsModel.addRow(new Object[] { "(" + (i++) + ")", q.op, dash(q.arg1), dash(q.arg2), dash(q.result) });
			}
			i = 1;
			for (Triple t : gen.getTriples()) {
				triplesModel.addRow(new Object[] { "(" + (i++) + ")", t.op, dash(t.arg1), dash(t.arg2) });
			}

			errors.setText("Sin errores.");
			if (tabs.getSelectedIndex() == TAB_ERRORES)
				tabs.setSelectedIndex(0);
			setStatus(" ✔ Análisis completado sin errores.", new Color(0xDFF3E3));

		} catch (CompilerException ex) {
			clearOutput();
			showErrors(Collections.singletonList(ex.getMessage()));
		} catch (RuntimeException ex) {
			clearOutput();
			showErrors(Collections.singletonList("Error interno: " + ex));
		}
	}

	/**
	 * Reúne las expresiones con operador: lado derecho de asignaciones, condiciones
	 * y argumentos.
	 */
	private void collectExpressions(ASTNode n, List<ASTNode> out) {
		switch (n.kind) {
		case BLOCK:
			for (ASTNode c : n.children)
				collectExpressions(c, out);
			break;
		case IF:
			addIfOperation(n.children.get(0), out);
			collectExpressions(n.children.get(1), out);
			break;
		case DECL:
		case ASSIGN:
		case CALL:
			for (ASTNode c : n.children)
				addIfOperation(c, out);
			break;
		default:
			break;
		}
	}

	private void addIfOperation(ASTNode expr, List<ASTNode> out) {
		if (expr.kind == ASTNode.Kind.BINARY)
			out.add(expr);
	}

	private static String dash(String s) {
		return s == null ? "-" : s;
	}

	// ---------- helpers de salida ----------
	private void clearOutput() {
		symbolsModel.setRowCount(0);
		notationsModel.setRowCount(0);
		quadsModel.setRowCount(0);
		triplesModel.setRowCount(0);
		errors.setText("");
	}

	private void showErrors(List<String> messages) {
		StringBuilder sb = new StringBuilder();
		for (String m : messages)
			sb.append("• ").append(m).append("\n");
		errors.setText(sb.toString());
		tabs.setSelectedIndex(TAB_ERRORES);
		setStatus(" ✖ Se encontraron " + messages.size() + " error(es).", new Color(0xFADBD8));
	}

	private void setStatus(String text, Color bg) {
		status.setText(text);
		status.setBackground(bg == null ? UIManager.getColor("Panel.background") : bg);
		status.setForeground(Color.DARK_GRAY);
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) {
		}
		SwingUtilities.invokeLater(() -> new Main().setVisible(true));
	}
}