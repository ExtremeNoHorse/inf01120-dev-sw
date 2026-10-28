# Mapa de Comandos e Regras de Mapeamento (Fase 2)

Tabela de referência para a sintaxe de entrada do interpretador musical na Fase 2.

---

## 1. Notas Musicais e Acidentes

As notas são identificadas pelas letras de `A` a `G`. O caractere `H` foi retirado das notas na Fase 2 e agora é tratado como consoante.

| Entrada | Nota Musical | Exemplo | Token JFugue Resultante (Voz 0, Semínima) |
|---------|--------------|---------|------------------------------------------|
| `A` | Lá | `A:3` | `A6qa100` |
| `B` | Si | `B:3` | `B6qa100` |
| `C` | Dó | `C:3` | `C6qa100` |
| `D` | Ré | `D:3` | `D6qa100` |
| `E` | Mi | `E:3` | `E6qa100` |
| `F` | Fá | `F:3` | `F6qa100` |
| `G` | Sol | `G:3` | `G6qa100` |

### Acidentes
- `#` (sustenido): eleva a nota em meio-tom (ex: `A#:3`, `C#:2`).
- `b` (bemol minúsculo): reduz a nota em meio-tom (ex: `Bb:3`, `Eb:4`).

---

## 2. Figuras de Duração

A duração é especificada pelo sufixo `:<número>` associado à nota (ex: `C:1`, `D:3`):

| Identificador | Figura | Valor Relativo | Token JFugue |
|---------------|--------|----------------|--------------|
| `:1` | Semibreve | 1 (tempo inteiro) | `w` |
| `:2` | Mínima | 1/2 | `h` |
| `:3` | Semínima | 1/4 (pulso padrão) | `q` |
| `:4` | Colcheia | 1/8 | `i` |
| `:5` | Semicolcheia | 1/16 | `s` |
| `:6` | Fusa | 1/32 | `t` |
| `:7` | Semifusa | 1/64 | `x` |

*Nota*: Se a nota for declarada sem sufixo de duração (ex: `C`), ela é emitida sem sufixo de tempo explícito, assumindo a duração padrão do sintetizador.

---

## 3. Vozes e Polifonia

Cada linha do arquivo de texto corresponde a uma voz independente, executada de forma simultânea.

| Linha | Voz | Oitava Base | Volume Base | Instrumento Base |
|-------|-----|-------------|-------------|-------------------|
| Linha 1 | `V0` (Soprano) | 6 | 100 | Cravo (GM #6) |
| Linha 2 | `V1` (Alto) | 5 | 80 | Órgão (GM #20) |
| Linha 3 | `V2` (Tenor) | 4 | 60 | Piano (GM #0) |
| Linha 4 | `V3` (Baixo) | 3 | 40 | Fagote (GM #70) |

*A partir da 5ª linha, o ciclo de parâmetros reinicia (`V4` usa oitava 6, volume 100, etc.).*

### Atraso de Entrada (Delay)
- Sintaxe: `[n]` no início da linha (ex: `[4] C:3 D:3`), com `n` entre 0 e 10.000.
- Efeito: Insere `n` tempos de silêncio (`Rq`) antes da primeira nota daquela voz.

---

## 4. Controles Musicais e Parâmetros

| Caractere | Ação | Escopo |
|-----------|------|--------|
| `?` | Aumenta a oitava atual em 1; acima de 9 retorna à oitava-base da voz. | Voz atual |
| `V` | Diminui a oitava atual em 1; abaixo de 0 retorna à oitava-base da voz. | Voz atual |
| `>` | Aumenta o BPM em 10 (acelera). | Global (todas as vozes) |
| `<` | Diminui o BPM em 10 (desacelera). | Global (todas as vozes) |
| ` ` (Espaço) | Dobra o volume da voz (com teto máximo de 127). | Voz atual |
| `!` | Troca para instrumento Bandoneon (General MIDI #24). | Voz atual |
| `;` | Troca para instrumento Tubular Bells (General MIDI #15). | Voz atual |
| `,` | Troca para instrumento Agogô (General MIDI #114). | Voz atual |
| Dígito ímpar (`1, 3, 5, 7, 9`) | Troca para instrumento Tubular Bells (General MIDI #15). | Voz atual |
| Dígito par (`0, 2, 4, 6, 8`) | Troca para instrumento = instrumento atual + valor do dígito. | Voz atual |

---

## 5. Regras de Repetição e Silêncio

| Caractere | Regra |
|-----------|-------|
| `a, b, c, d, e, f, g` (minúsculas isoladas) | Silêncio / Pausa (`Rq`). |
| Vogais `O, I, U` e consoantes não-nota (ex: `H, J, K, L, M, N...`) | Se o caractere anterior processado era uma nota, repete essa nota; caso contrário, gera pausa (`Rq`). |
| Outros caracteres | Se o caractere anterior era nota, repete a nota; caso contrário, pausa (`Rq`). |
