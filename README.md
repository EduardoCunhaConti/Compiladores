# Compilador LPS1

Compilador para a linguagem LPS1 (Linguagem de Programação Simples 1),
desenvolvido em Java para a disciplina de Compiladores — CEUB/FATECS.

## Requisitos

- Java JDK 17 ou superior (testado com Java 23)
- Terminal (Prompt de Comando, PowerShell ou terminal do VS Code)

## Estrutura dos arquivos

## Estrutura dos arquivos

```
compilador-lps1/
├── compiladores/
│   ├── CompiladorLPS1A.java              # Parte (a): parser + geração inline
│   └── CompiladorLPS1B.java              # Parte (b): parser + ASA + geração nos métodos
├── testes/
│   ├── exemplo1.txt                      # Exemplo 1 do enunciado (múltiplos de p)
│   ├── exemplo2.txt                      # Exemplo 2 do enunciado (teste de primalidade)
│   ├── teste_soma.txt                    # Teste: soma de dois números
│   ├── teste_sub_div.txt                 # Teste: subtração e divisão
│   ├── teste_contador.txt                # Teste: contador com while
│   ├── teste_composto.txt                # Teste: bloco composto
│   ├── teste_if_while.txt                # Teste: if dentro de while
│   ├── teste_erro_bloco_nao_fechado.txt  # Erro: bloco '{' sem fechar
│   ├── teste_erro_chave_sem_abertura.txt # Erro: '}' sem '{'
│   ├── teste_erro_comando_inexistente.txt# Erro: comando inválido
│   ├── teste_erro_variavel_esperada.txt  # Erro: número onde se esperava variável
│   ├── teste_erro_operador_invalido.txt  # Erro: operador de comparação inválido
│   ├── teste_erro_arquivo_incompleto.txt # Erro: arquivo termina no meio de um comando
│   └── teste_erro_valor_invalido.txt     # Erro: valor inválido em expressão
└── README.md
```

## Como compilar os compiladores

Execute uma única vez para gerar os arquivos .class:

```bash
javac CompiladorLPS1A.java
javac CompiladorLPS1B.java
```

## Como usar

### Parte (a) — geração de código inline

```bash
java CompiladorLPS1A <arquivo.txt>
```

### Parte (b) — geração via ASA

```bash
java CompiladorLPS1B <arquivo.txt>
```

### Salvar a saída C em um arquivo

```bash
java CompiladorLPS1A exemplo1.txt > saida.c
java CompiladorLPS1B exemplo1.txt > saida.c
```

## Exemplos de uso

```bash
# Rodar o exemplo 1 com a parte (a)
java CompiladorLPS1A exemplo1.txt

# Rodar o exemplo 1 com a parte (b)
java CompiladorLPS1B exemplo1.txt

# Salvar saída e compilar o C gerado
java CompiladorLPS1A exemplo1.txt > saida.c
gcc -w -o prog saida.c
./prog
```

## Testando detecção de erros

```bash
java CompiladorLPS1A erro_bloco_nao_fechado.txt
java CompiladorLPS1A erro_chave_sem_abertura.txt
java CompiladorLPS1A erro_comando_inexistente.txt
java CompiladorLPS1A erro_variavel_esperada.txt
java CompiladorLPS1A erro_operador_invalido.txt
java CompiladorLPS1A erro_arquivo_incompleto.txt
java CompiladorLPS1A erro_valor_invalido.txt
```

## Linguagem LPS1 — referência rápida

| Comando       | Sintaxe LPS1      | Código C gerado                         |
|---------------|-------------------|-----------------------------------------|
| Atribuição    | `= a b`           | `a = b;`                                |
| Leitura       | `G a`             | `{ gets(str); sscanf(str, "%d", &a); }` |
| Soma          | `+ c a b`         | `c = a + b;`                            |
| Subtração     | `- c a b`         | `c = a - b;`                            |
| Multiplicação | `* c a b`         | `c = a * b;`                            |
| Divisão       | `/ c a b`         | `c = a / b;`                            |
| Módulo        | `% c a b`         | `c = a % b;`                            |
| Impressão     | `P a`             | `printf("%d\n", a);`                    |
| Decisão       | `I a < b Comando` | `if ( a < b ) { Comando }`              |
| Repetição     | `W a < b Comando` | `while ( a < b ) { Comando }`           |
| Bloco         | `{ Cmd1 Cmd2 }`   | `Cmd1 Cmd2` (executados em sequência)   |

## Operadores de comparação

| LPS1 | Significado  | C    |
|------|--------------|------|
| `=`  | igual        | `==` |
| `<`  | menor que    | `<`  |
| `#`  | diferente de | `!=` |

## Variáveis e números

- **Variáveis:** qualquer letra minúscula (`a` até `z`), declaradas automaticamente como `int`
- **Números:** dígito único (`0` a `9`) no código fonte; em execução podem ter mais dígitos