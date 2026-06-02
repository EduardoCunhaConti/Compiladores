// PARTE (a)
// Analisador Léxico + Sintático com Geração de Código C Inline
// Linguagem: LPS1 (Linguagem de Programação Simples 1)
// Disciplina: Compiladores - CEUB/FATECS

import java.io.*;

public class CompiladorLPS1A {

    // ======================================================
    // ANALISADOR LÉXICO
    // ======================================================

    static final int TK_VAR     = 0;  // letra minúscula
    static final int TK_NUM     = 1;  // dígito
    static final int TK_ASSIGN  = 2;  // '='
    static final int TK_GET     = 3;  // 'G'
    static final int TK_ADD     = 4;  // '+'
    static final int TK_SUB     = 5;  // '-'
    static final int TK_MULT    = 6;  // '*'
    static final int TK_DIV     = 7;  // '/'
    static final int TK_MOD     = 8;  // '%'
    static final int TK_PRINT   = 9;  // 'P'
    static final int TK_IF      = 10; // 'I'
    static final int TK_WHILE   = 11; // 'W'
    static final int TK_LT      = 12; // '<'
    static final int TK_NE      = 13; // '#' (diferente de)
    static final int TK_LBRACE  = 14; // '{'
    static final int TK_RBRACE  = 15; // '}'
    static final int TK_EOF     = 16;
    static final int TK_ERROR   = 17;

    static int  tokenType;
    static char tokenValue;
    static int  lineNum = 1;

    static Reader reader;

    // Lê o próximo caractere não-branco
    static int nextChar() throws IOException {
        int c;
        while ((c = reader.read()) != -1) {
            if (c == '\n') lineNum++;
            if (!Character.isWhitespace(c)) return c;
        }
        return -1;
    }

    // Classifica um caractere e define tokenType / tokenValue
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

    // Avança para o próximo token
    static void advance() throws IOException {
        makeToken(nextChar());
    }

    // ======================================================
    // INDENTAÇÃO
    // ======================================================

    static int indentLevel = 1;

    static void printIndent() {
        for (int i = 0; i < indentLevel; i++) System.out.print("    ");
    }

    // ======================================================
    // ANALISADOR SINTÁTICO + GERAÇÃO DE CÓDIGO INLINE
    // ======================================================

    // Value ::= Variable | Number
    static void parseValue() throws IOException {
        if (tokenType == TK_VAR || tokenType == TK_NUM) {
            System.out.print(tokenValue);
            advance();
        } else if (tokenType == TK_EOF) {
            System.err.println("Erro lexico na linha " + lineNum
                + ": valor esperado (variavel ou numero), mas o arquivo terminou inesperadamente");
            System.exit(1);
        } else {
            System.err.println("Erro lexico na linha " + lineNum
                + ": valor esperado (variavel ou numero), encontrado '" + tokenValue + "'");
            System.exit(1);
        }
    }

    // Variable ::= letra minúscula
    static char parseVariable() throws IOException {
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
        char v = tokenValue;
        advance();
        return v;
    }

    // Comparison ::= Variable Operator Value
    static void parseComparison() throws IOException {
        char var = parseVariable();
        String op;
        if      (tokenType == TK_ASSIGN) { op = "=="; advance(); }
        else if (tokenType == TK_LT)     { op = "<";  advance(); }
        else if (tokenType == TK_NE)     { op = "!="; advance(); }
        else if (tokenType == TK_EOF) {
            System.err.println("Erro sintatico na linha " + lineNum
                + ": operador de comparacao esperado ('=', '<' ou '#'), mas o arquivo terminou inesperadamente");
            System.exit(1);
            return;
        } else {
            System.err.println("Erro sintatico na linha " + lineNum
                + ": operador de comparacao invalido '" + tokenValue
                + "' — use '=' (igual), '<' (menor que) ou '#' (diferente de)");
            System.exit(1);
            return;
        }
        System.out.print(var + " " + op + " ");
        parseValue();
    }

    // AssignCommand ::= '=' Variable Value
    static void parseAssignCommand() throws IOException {
        advance(); // consome '='
        char var = parseVariable();
        printIndent();
        System.out.print(var + " = ");
        parseValue();
        System.out.println(";");
    }

    // GetCommand ::= 'G' Variable
    static void parseGetCommand() throws IOException {
        advance(); // consome 'G'
        char var = parseVariable();
        printIndent();
        System.out.println("{ gets(str); sscanf(str, \"%d\", &" + var + "); }");
    }

    // AddCommand ::= '+' Variable Value Value
    static void parseAddCommand() throws IOException {
        advance(); // consome '+'
        char var = parseVariable();
        printIndent();
        System.out.print(var + " = ");
        parseValue();
        System.out.print(" + ");
        parseValue();
        System.out.println(";");
    }

    // SubCommand ::= '-' Variable Value Value
    static void parseSubCommand() throws IOException {
        advance(); // consome '-'
        char var = parseVariable();
        printIndent();
        System.out.print(var + " = ");
        parseValue();
        System.out.print(" - ");
        parseValue();
        System.out.println(";");
    }

    // MultCommand ::= '*' Variable Value Value
    static void parseMultCommand() throws IOException {
        advance(); // consome '*'
        char var = parseVariable();
        printIndent();
        System.out.print(var + " = ");
        parseValue();
        System.out.print(" * ");
        parseValue();
        System.out.println(";");
    }

    // DivCommand ::= '/' Variable Value Value
    static void parseDivCommand() throws IOException {
        advance(); // consome '/'
        char var = parseVariable();
        printIndent();
        System.out.print(var + " = ");
        parseValue();
        System.out.print(" / ");
        parseValue();
        System.out.println(";");
    }

    // ModCommand ::= '%' Variable Value Value
    static void parseModCommand() throws IOException {
        advance(); // consome '%'
        char var = parseVariable();
        printIndent();
        System.out.print(var + " = ");
        parseValue();
        System.out.print(" % ");
        parseValue();
        System.out.println(";");
    }

    // PrintCommand ::= 'P' Value
    static void parsePrintCommand() throws IOException {
        advance(); // consome 'P'
        printIndent();
        System.out.print("printf(\"%d\\n\", ");
        parseValue();
        System.out.println(");");
    }

    // IfCommand ::= 'I' Comparison Command
    static void parseIfCommand() throws IOException {
        advance(); // consome 'I'
        printIndent();
        System.out.print("if ( ");
        parseComparison();
        System.out.println(" ) {");
        indentLevel++;
        parseCommand();
        indentLevel--;
        printIndent();
        System.out.println("}");
    }

    // WhileCommand ::= 'W' Comparison Command
    static void parseWhileCommand() throws IOException {
        advance(); // consome 'W'
        printIndent();
        System.out.print("while ( ");
        parseComparison();
        System.out.println(" ) {");
        indentLevel++;
        parseCommand();
        indentLevel--;
        printIndent();
        System.out.println("}");
    }

    // CompositeCommand ::= '{' Command { Command } '}'
    static void parseCompositeCommand() throws IOException {
        int openLine = lineNum; // guarda a linha onde '{' foi encontrado
        advance(); // consome '{'
        while (tokenType != TK_RBRACE && tokenType != TK_EOF) {
            parseCommand();
        }
        if (tokenType != TK_RBRACE) {
            System.err.println("Erro sintatico: bloco '{' aberto na linha "
                + openLine + " nunca foi fechado — '}' esperado");
            System.exit(1);
        }
        advance(); // consome '}'
    }

    // Command ::= AssignCommand | GetCommand | ... | CompositeCommand
    static void parseCommand() throws IOException {
        switch (tokenType) {
            case TK_ASSIGN:  parseAssignCommand();    break;
            case TK_GET:     parseGetCommand();       break;
            case TK_ADD:     parseAddCommand();       break;
            case TK_SUB:     parseSubCommand();       break;
            case TK_MULT:    parseMultCommand();      break;
            case TK_DIV:     parseDivCommand();       break;
            case TK_MOD:     parseModCommand();       break;
            case TK_PRINT:   parsePrintCommand();     break;
            case TK_IF:      parseIfCommand();        break;
            case TK_WHILE:   parseWhileCommand();     break;
            case TK_LBRACE:  parseCompositeCommand(); break;
            case TK_RBRACE:
                System.err.println("Erro sintatico na linha " + lineNum
                    + ": '}' encontrado sem '{' correspondente");
                System.exit(1);
                break;
            case TK_EOF:
                System.err.println("Erro sintatico na linha " + lineNum
                    + ": comando esperado, mas o arquivo terminou inesperadamente");
                System.exit(1);
                break;
            default:
                System.err.println("Erro sintatico na linha " + lineNum
                    + ": comando inexistente '" + tokenValue
                    + "' — comandos validos: '=' '+' '-' '*' '/' '%' 'G' 'P' 'I' 'W' '{'");
                System.exit(1);
        }
    }

    // Program ::= Command { Command }
    static void parseProgram() throws IOException {
        System.out.println("#include <stdio.h>");
        System.out.println();
        System.out.println("int main() {");
        System.out.println("    int a, b, c, d, e, f, g, h, i, j, k, l, m, "
                         + "n, o, p, q, r, s, t, u, v, w, x, y, z;");
        System.out.println("    char str[512]; /* auxiliar na leitura com G */");
        System.out.println();

        advance(); // lê o primeiro token

        while (tokenType != TK_EOF) {
            parseCommand();
        }

        System.out.println("    return 0;");
        System.out.println("}");
    }

    // ======================================================
    // MAIN
    // ======================================================

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Uso: java CompiladorLPS1A <arquivo.txt>");
            System.exit(1);
        }
        reader = new FileReader(args[0]);
        parseProgram();
        reader.close();
        System.err.println("Compilacao concluida com sucesso.");
    }
}