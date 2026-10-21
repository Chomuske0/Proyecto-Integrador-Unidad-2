package ir;

import parser.ASTNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Genera cuádruplos y triplos recorriendo el AST en postorden (primero los
 * hijos, luego el operador). Por eso en a + b * 2 la multiplicación sale antes
 * que la suma: está más abajo en el árbol.
 *
 * Usa una instancia nueva por cada programa/ejemplo, para que los temporales
 * (t1, t2...) y las etiquetas (L1...) reinicien.
 */
public class IRGenerator {

	private final List<Quadruple> quads = new ArrayList<>();
	private final List<Triple> triples = new ArrayList<>();
	private int tempCount = 0;
	private int labelCount = 0;

	/**
	 * Un operando tiene dos formas: la de cuádruplos (t1) y la de triplos ((1)).
	 */
	private static class Operand {
		final String quad, triple;

		Operand(String quad, String triple) {
			this.quad = quad;
			this.triple = triple;
		}
	}

	public List<Quadruple> getQuadruples() {
		return quads;
	}

	public List<Triple> getTriples() {
		return triples;
	}

	// ---------- API ----------
	public void generate(ASTNode node) {
		switch (node.kind) {
		case BLOCK:
			for (ASTNode child : node.children)
				generate(child);
			break;
		case DECL:
			// Sin valor inicial no genera código: solo va a la tabla de símbolos.
			if (!node.children.isEmpty())
				genAssign(node.value, node.children.get(0));
			break;
		case ASSIGN:
			genAssign(node.value, node.children.get(0));
			break;
		case IF:
			genIf(node);
			break;
		case CALL:
			genCall(node);
			break;
		default:
			throw new IllegalArgumentException("Nodo no es una instrucción: " + node.kind);
		}
	}

	// ---------- instrucciones ----------
	private void genAssign(String name, ASTNode expr) {
		Operand src = genExpr(expr);
		quads.add(new Quadruple("=", src.quad, null, name));
		triples.add(new Triple("=", src.triple, name));
	}

	private void genIf(ASTNode node) {
		Operand cond = genExpr(node.children.get(0));
		String endLabel = newLabel();
		quads.add(new Quadruple("IF_FALSE", cond.quad, null, endLabel));
		triples.add(new Triple("IF_FALSE", cond.triple, endLabel));

		generate(node.children.get(1)); // cuerpo del si

		quads.add(new Quadruple("LABEL", null, null, endLabel));
		triples.add(new Triple("LABEL", endLabel, null));
	}

	private void genCall(ASTNode call) {
		// Primero se evalúan todos los argumentos, luego PARAM y CALL.
		List<Operand> args = new ArrayList<>();
		for (ASTNode a : call.children)
			args.add(genExpr(a));

		for (Operand a : args) {
			quads.add(new Quadruple("PARAM", a.quad, null, null));
			triples.add(new Triple("PARAM", a.triple, null));
		}
		String n = String.valueOf(args.size());
		quads.add(new Quadruple("CALL", call.value, n, null));
		triples.add(new Triple("CALL", call.value, n));
	}

	// ---------- expresiones ----------
	private Operand genExpr(ASTNode n) {
		if (n.kind == ASTNode.Kind.NUMBER || n.kind == ASTNode.Kind.VAR) {
			return new Operand(n.value, n.value);
		}
		// BINARY: postorden
		Operand l = genExpr(n.left());
		Operand r = genExpr(n.right());

		String temp = "t" + (++tempCount);
		quads.add(new Quadruple(n.value, l.quad, r.quad, temp));

		triples.add(new Triple(n.value, l.triple, r.triple));
		String ref = "(" + triples.size() + ")"; // número de instrucción, base 1
		return new Operand(temp, ref);
	}

	private String newLabel() {
		return "L" + (++labelCount);
	}
}