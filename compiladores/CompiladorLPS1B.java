// PARTE (b)
// Analisador Léxico + Sintático + ASA (Árvore Sintática Abstrata)
// com Geração de Código C nos métodos da ASA
// Linguagem: LPS1 (Linguagem de Programação Simples 1)
// Disciplina: Compiladores - CEUB/FATECS

import java.io.*;
import java.util.*;

public class CompiladorLPS1B {

    // ======================================================
    // DEFINIÇÃO DOS NÓS DA ASA
    // ======================================================

    static abstract class Node {
        abstract void genCode(int indent);

        static void printIndent(int level) {
            for (int i = 0; i < level; i++) System.out.print("    ");
        }
    }

    static class VarNode extends Node {
        char name;
        VarNode(char name) { this.name = name; }

        @Override
        void genCode(int indent) {
            System.out.print(name);
        }
    }

    static class NumNode extends Node {
        char digit;
        NumNode(char digit) { this.digit = digit; }

        @Override
        void genCode(int indent) {
            System.out.print(digit);
        }
    }

    static class ComparisonNode extends Node {
        Node left;
        String op;
        Node right;

        ComparisonNode(Node left, String op, Node right) {
            this.left  = left;
            this.op    = op;
            this.right = right;
        }

        @Override
        void genCode(int indent) {
            left.genCode(indent);
            System.out.print(" " + op + " ");
            right.genCode(indent);
        }
    }

    static class AssignNode extends Node {
        Node var, value;
        AssignNode(Node var, Node value) {
            this.var   = var;
            this.value = value;
        }

        @Override
        void genCode(int indent) {
            printIndent(indent);
            var.genCode(indent);
            System.out.print(" = ");
            value.genCode(indent);
            System.out.println(";");
        }
    }

    static class GetNode extends Node {
        Node var;
        GetNode(Node var) { this.var = var; }

        @Override
        void genCode(int indent) {
            printIndent(indent);
            System.out.print("{ gets(str); sscanf(str, \"%d\", &");
            var.genCode(indent);
            System.out.println("); }");
        }
    }

    static class ArithNode extends Node {
        String op;
        Node var, left, right;
        ArithNode(String op, Node var, Node left, Node right) {
            this.op    = op;
            this.var   = var;
            this.left  = left;
            this.right = right;
        }

        @Override
        void genCode(int indent) {
            printIndent(indent);
            var.genCode(indent);
            System.out.print(" = ");
            left.genCode(indent);
            System.out.print(" " + op + " ");
            right.genCode(indent);
            System.out.println(";");
        }
    }

    static class PrintNode extends Node {
        Node value;
        PrintNode(Node value) { this.value = value; }

        @Override
        void genCode(int indent) {
            printIndent(indent);
            System.out.print("printf(\"%d\\n\", ");
            value.genCode(indent);
            System.out.println(");");
        }
    }

    static class IfNode extends Node {
        Node comparison, command;
        IfNode(Node comparison, Node command) {
            this.comparison = comparison;
            this.command    = command;
        }

        @Override
        void genCode(int indent) {
            printIndent(indent);
            System.out.print("if ( ");
            comparison.genCode(indent);
            System.out.println(" ) {");
            command.genCode(indent + 1);
            printIndent(indent);
            System.out.println("}");
        }
    }

    static class WhileNode extends Node {
        Node comparison, command;
        WhileNode(Node comparison, Node command) {
            this.comparison = comparison;
            this.command    = command;
        }

        @Override
        void genCode(int indent) {
            printIndent(indent);
            System.out.print("while ( ");
            comparison.genCode(indent);
            System.out.println(" ) {");
            command.genCode(indent + 1);
            printIndent(indent);
            System.out.println("}");
        }
    }

    static class CompositeNode extends Node {
        List<Node> commands = new ArrayList<>();

        void add(Node cmd) { commands.add(cmd); }

        @Override
        void genCode(int indent) {
            for (Node cmd : commands) cmd.genCode(indent);
        }
    }

    static class ProgramNode extends Node {
        List<Node> commands = new ArrayList<>();

        void add(Node cmd) { commands.add(cmd); }

        @Override
        void genCode(int indent) {
            System.out.println("#include <stdio.h>");
            System.out.println();
            System.out.println("int main() {");
            System.out.println("    int a, b, c, d, e, f, g, h, i, j, k, l, m, "
                             + "n, o, p, q, r, s, t, u, v, w, x, y, z;");
            System.out.println("    char str[512]; /* auxiliar na leitura com G */");
            System.out.println();
            for (Node cmd : commands) cmd.genCode(indent);
            System.out.println("    return 0;");
            System.out.println("}");
        }
    }

    // ======================================================
    // ANALISADOR LÉXICO
    // ======================================================

    static final int TK_VAR    = 0;
    static final int TK_NUM    = 1;
    static final int TK_ASSIGN = 2;
    static final int TK_GET    = 3;
    static final int TK_ADD    = 4;
    static final int TK_SUB    = 5;
    static final int TK_MULT   = 6;
    static final int TK_DIV    = 7;
    static final int TK_MOD    = 8;
    static final int TK_PRINT  = 9;
    static final int TK_IF     = 10;
    static final int TK_WHILE  = 11;
    static final int TK_LT     = 12;
    static final int TK_NE     = 13;
    static final int TK_LBRACE = 14;
    static final int TK_RBRACE = 15;
    static final int TK_EOF    = 16;
    static final int TK_ERROR  = 17;

    static int  tokenType;
    static char tokenValue;
    static int  lineNum = 1;
    static Reader reader;

    static int nextChar() throws IOException {
        int c;
        while ((c = reader.read()) != -1) {
            if (c == '\n') lineNum++;
            if (!Character.isWhitespace(c)) return c;
        }
        return -1;
    }

    static void makeToken(int c) {
        if (c == -1) { tokenType = TK_EOF; tokenValue = 0; return; }
        tokenValue = (char) c;
        if (Character.isLowerCase(c)) { tokenType = TK_VAR;    return; }
        if (Character.isDigit(c))     { tokenType = TK_NUM;    return; }
        switch (c) {
            case '=': tokenType = TK_ASSIGN; return;
            case 'G': tokenType = TK_GET;    return;
            case '+': tokenType = TK_ADD;    return;
            case '-': tokenType = TK_SUB;    return;
            case '*': tokenType = TK_MULT;   return;
            case '/': tokenType = TK_DIV;    return;
            case '%': tokenType = TK_MOD;    return;
            case 'P': tokenType = TK_PRINT;  return;
            case 'I': tokenType = TK_IF;     return;
            case 'W': tokenType = TK_WHILE;  return;
            case '<': tokenType = TK_LT;     return;
            case '#': tokenType = TK_NE;     return;
            case '{': tokenType = TK_LBRACE; return;
            case '}': tokenType = TK_RBRACE; return;
            default:  tokenType = TK_ERROR;  return;
        }
    }

    static void advance() throws IOException {
        makeToken(nextChar());
    }

    // ======================================================
    // ANALISADOR SINTÁTICO — CONSTRÓI A ASA
    // ======================================================

    static Node parseValue() throws IOException {
        if (tokenType == TK_VAR) {
            VarNode n = new VarNode(tokenValue); advance(); return n;
        } else if (tokenType == TK_NUM) {
            NumNode n = new NumNode(tokenValue); advance(); return n;
        } else if (tokenType == TK_EOF) {
            System.err.println("Erro lexico na linha " + lineNum
                + ": valor esperado (variavel ou numero), mas o arquivo terminou inesperadamente");
            System.exit(1);
        } else {
            System.err.println("Erro lexico na linha " + lineNum
                + ": valor esperado (variavel ou numero), encontrado '" + tokenValue + "'");
            System.exit(1);
        }
        return null;
    }

    static VarNode parseVariable() throws IOException {
        if (tokenType != TK_VAR) {
            if (tokenType == TK_NUM) {
                System.err.println("Erro sintatico na linha " + lineNum
                    + ": variavel esperada, mas foi encontrado o numero '" + tokenValue
                    + "' (variaveis devem ser letras minusculas)");
            } else if (tokenType == TK_EOF) {
                System.err.println("Erro sintatico na linha " + lineNum
                    + ": variavel esperada, mas o arquivo terminou inesperadamente");
            } else {
                System.err.println("Erro sintatico na linha " + lineNum
                    + ": variavel esperada, encontrado '" + tokenValue + "'");
            }
            System.exit(1);
        }
        VarNode n = new VarNode(tokenValue); advance(); return n;
    }

    static ComparisonNode parseComparison() throws IOException {
        VarNode left = parseVariable();
        String op;
        if      (tokenType == TK_ASSIGN) { op = "=="; advance(); }
        else if (tokenType == TK_LT)     { op = "<";  advance(); }
        else if (tokenType == TK_NE)     { op = "!="; advance(); }
        else if (tokenType == TK_EOF) {
            System.err.println("Erro sintatico na linha " + lineNum
                + ": operador de comparacao esperado ('=', '<' ou '#'), mas o arquivo terminou inesperadamente");
            System.exit(1);
            return null;
        } else {
            System.err.println("Erro sintatico na linha " + lineNum
                + ": operador de comparacao invalido '" + tokenValue
                + "' — use '=' (igual), '<' (menor que) ou '#' (diferente de)");
            System.exit(1);
            return null;
        }
        return new ComparisonNode(left, op, parseValue());
    }

    static AssignNode parseAssignCommand() throws IOException {
        advance(); // consome '='
        VarNode var   = parseVariable();
        Node    value = parseValue();
        return new AssignNode(var, value);
    }

    static GetNode parseGetCommand() throws IOException {
        advance(); // consome 'G'
        VarNode var = parseVariable();
        return new GetNode(var);
    }

    static ArithNode parseArithCommand(String op) throws IOException {
        advance(); // consome o símbolo da operação
        VarNode var   = parseVariable();
        Node    left  = parseValue();
        Node    right = parseValue();
        return new ArithNode(op, var, left, right);
    }

    static PrintNode parsePrintCommand() throws IOException {
        advance(); // consome 'P'
        Node value = parseValue();
        return new PrintNode(value);
    }

    static IfNode parseIfCommand() throws IOException {
        advance(); // consome 'I'
        ComparisonNode cmp = parseComparison();
        Node           cmd = parseCommand();
        return new IfNode(cmp, cmd);
    }

    static WhileNode parseWhileCommand() throws IOException {
        advance(); // consome 'W'
        ComparisonNode cmp = parseComparison();
        Node           cmd = parseCommand();
        return new WhileNode(cmp, cmd);
    }

    static Node parseCommand() throws IOException {
        switch (tokenType) {
            case TK_ASSIGN:  return parseAssignCommand();
            case TK_GET:     return parseGetCommand();
            case TK_ADD:     return parseArithCommand("+");
            case TK_SUB:     return parseArithCommand("-");
            case TK_MULT:    return parseArithCommand("*");
            case TK_DIV:     return parseArithCommand("/");
            case TK_MOD:     return parseArithCommand("%");
            case TK_PRINT:   return parsePrintCommand();
            case TK_IF:      return parseIfCommand();
            case TK_WHILE:   return parseWhileCommand();
            case TK_LBRACE: {
                int openLine = lineNum; // guarda a linha onde '{' foi encontrado
                advance(); // consome '{'
                CompositeNode comp = new CompositeNode();
                while (tokenType != TK_RBRACE && tokenType != TK_EOF) {
                    comp.add(parseCommand());
                }
                if (tokenType != TK_RBRACE) {
                    System.err.println("Erro sintatico: bloco '{' aberto na linha "
                        + openLine + " nunca foi fechado — '}' esperado");
                    System.exit(1);
                }
                advance(); // consome '}'
                return comp;
            }
            case TK_RBRACE:
                System.err.println("Erro sintatico na linha " + lineNum
                    + ": '}' encontrado sem '{' correspondente");
                System.exit(1);
                return null;
            case TK_EOF:
                System.err.println("Erro sintatico na linha " + lineNum
                    + ": comando esperado, mas o arquivo terminou inesperadamente");
                System.exit(1);
                return null;
            default:
                System.err.println("Erro sintatico na linha " + lineNum
                    + ": comando inexistente '" + tokenValue
                    + "' — comandos validos: '=' '+' '-' '*' '/' '%' 'G' 'P' 'I' 'W' '{'");
                System.exit(1);
                return null;
        }
    }

    static ProgramNode parseProgram() throws IOException {
        advance(); // lê o primeiro token
        ProgramNode prog = new ProgramNode();
        while (tokenType != TK_EOF) {
            prog.add(parseCommand());
        }
        return prog;
    }

    // ======================================================
    // MAIN
    // ======================================================

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Uso: java CompiladorLPS1B <arquivo.txt>");
            System.exit(1);
        }
        reader = new FileReader(args[0]);

        // Fase 1: construir a ASA
        ProgramNode ast = parseProgram();
        reader.close();

        // Fase 2: gerar código C percorrendo a ASA
        ast.genCode(1);

        System.err.println("Compilacao (via ASA) concluida com sucesso.");
    }
}