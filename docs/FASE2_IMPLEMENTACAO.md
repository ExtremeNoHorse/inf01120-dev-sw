# TTM (Text-To-Music) — Documentação da Fase 2

Este documento descreve detalhadamente as novas funcionalidades, regras de mapeamento e melhorias arquiteturais implementadas na **Fase 2** do trabalho prático de INF01120 (Técnicas de Construção de Programas / Desenvolvimento de Software).

---

## Identificação

**Grupo 1** — André Schaidhauer Luckmann, Arthur Von Groll dos Santos, Carlos Eduardo Boesel Morari e Giovani Ferreira de Alencoi.

---

## 1. Resumo das Mudanças da Fase 2

### MUDANÇA 1 — Entrada via Arquivo TXT e Persistência
- O sistema permite carregar músicas a partir de arquivos `.txt` locais ou criá-las diretamente.
- A persistência é gerenciada por `MusicRepository` e sua implementação concreta `FileMusicRepository`.
- Um TXT importado mantém o caminho de origem e é sobrescrito ao salvar; deletar a cópia da biblioteca não apaga esse arquivo externo.
- Os nomes e caminhos de origem ficam num único `metadata.properties` na pasta das músicas, então renomear e o vínculo com o TXT persistem entre execuções.

### MUDANÇA 2 — Exportação de Arquivo MIDI
- O coordenador `Player` possui o método `export(Music music, Path targetPath)`.
- O caminho de destino e o nome do arquivo `.mid` são informados pelo usuário; a interface completa a extensão e pede confirmação antes de substituir um arquivo existente.
- A exportação usa a mesma configuração da reprodução (oitava, BPM e volume definidos em `Player.configure`).
- O arquivo é gerado através do motor JFugue (`MidiFileManager.savePatternToMidi`) utilizando *try-with-resources* para garantir fechamento determinístico de descritores no sistema operacional.

### MUDANÇA 3 — Catálogo de Instrumentos General MIDI
- A classe `PlayerConfig` disponibiliza constantes e um catálogo estático `getGeneralMidiInstruments()` com 26 instrumentos do padrão General MIDI (superando o requisito mínimo de 25).
- Instrumentos obrigatórios incluídos:
  - **Church Organ / Pipe Organ** (GM #20)
  - **Tubular Bells** (GM #15)
  - **Cravo / Harpsichord** (GM #6)
  - **Piano Acústico** (GM #0)
  - **Fagote / Bassoon** (GM #70)
  - **Bandoneon** (GM #24)
  - **Agogô** (GM #114)
  - **Ondas do Mar / Seashore** (GM #123)
  - **Gaita de Foles / Bagpipe** (GM #110)

### MUDANÇA 4 — Novo Mapeamento e Polifonia
- **Polifonia (Linhas como Vozes)**:
  - Cada linha do texto representa uma voz independente, com índice crescente (`V0`, `V1`, `V2`, ...), tocadas simultaneamente.
  - A partir da 5ª linha, o ciclo de configurações base se repete; os índices das vozes continuam crescendo (`V4`, `V5`, ...).
- **Configuração Base por Voz**:
  | Voz | Registro | Instrumento Base | Oitava Base | Volume Base |
  |-----|----------|-------------------|-------------|-------------|
  | Voz 0 | Soprano | Cravo (GM #6) | 6 | 100 |
  | Voz 1 | Contralto/Alto | Órgão (GM #20) | 5 | 80 |
  | Voz 2 | Tenor | Piano (GM #0) | 4 | 60 |
  | Voz 3 | Baixo | Fagote (GM #70) | 3 | 40 |
- **Atraso de Entrada (Delay)**:
  - Prefixo `[n]` no início da linha (ex: `[4] C:3`) insere `n` tempos de pausa (`Rq`) antes do início da voz correspondente. O intervalo aceito é de 0 a 10.000 beats.
- **Notas, Acidentes e Durações**:
  - Notas de `A` a `G` (Lá, Si, Dó, Ré, Mi, Fá, Sol).
  - A letra `H` foi retirada da lista de notas e passa a ser tratada como consoante.
  - Acidentes suportados: `#` (sustenido) e `b` (bemol).
  - Figuras de duração:
    - `1`: Semibreve (`w`)
    - `2`: Mínima (`h`)
    - `3`: Semínima (`q`)
    - `4`: Colcheia (`i`)
    - `5`: Semicolcheia (`s`)
    - `6`: Fusa (`t`)
    - `7`: Semifusa (`x`)
    - *Exemplos*: `A:3` (Lá semínima), `C#:2` (Dó sustenido mínima), `Bb:4` (Si bemol colcheia).
- **Controle de Oitava Relativa**:
  - `?`: aumenta a oitava atual em uma unidade; ao ultrapassar 9, retorna à oitava-base da voz.
  - `V`: diminui a oitava atual em uma unidade; ao ultrapassar 0, retorna à oitava-base da voz.
- **Andamento Global (BPM)**:
  - `>`: acelera o BPM global em 10 batidas por minuto.
  - `<`: desacelera o BPM global em 10 batidas por minuto.
- **Controle de Volume e Instrumento**:
  - ` ` (Espaço): dobra o volume da voz (com teto em 127).
  - `!`: troca instrumento da voz para Bandoneon (GM #24).
  - `;` ou dígito ímpar: troca instrumento para Tubular Bells (GM #15).
  - `,`: troca instrumento para Agogô (GM #114).
  - Dígito par: instrumento atual + valor do dígito.
- **Regras de Repetição e Silêncio**:
  - Minúsculas de notas (`a`, `b`, `c`, `d`, `e`, `f`, `g`): geram pausa / silêncio (`Rq`).
  - Vogais (`O`, `I`, `U`) e consoantes não-nota (incluindo `H`): se o caractere anterior processado era uma nota, repete a última nota tocada; caso contrário, emite silêncio (`Rq`).

---

## 2. Estrutura de Pacotes

```text
src/main/java/com/player/
├── App.java                               # Composition root e abertura da GUI
├── Player.java                            # Coordenador de playback e exportação MIDI
├── audio/
│   ├── AudioEngine.java                   # Interface de abstração do motor de áudio
│   └── JFuguePlayer.java                  # Implementação com JFugue ManagedPlayer
├── config/
│   └── PlayerConfig.java                  # Parâmetros musicais e configurações de voz
├── exception/
│   ├── ErrorCode.java                     # Códigos de erro de domínio
│   └── TtmException.java                  # Exceção base do sistema
├── interpreter/
│   ├── Interpreter.java                   # Interpretador de texto linha por linha / polifonia
│   ├── Timeline.java                      # Em que tempo cada caractere soa (destaque na GUI)
│   └── domain/
│       ├── DomainMapper.java              # Tabela e fábrica de mapeamento de comandos
│       ├── MusicalDuration.java           # Enum com as 7 durações musicais
│       └── commands/
│           ├── Command.java               # Interface do padrão Command
│           ├── ConfigUpdate.java          # Comandos de alteração de configuração/instrumento
│           └── Note.java                  # Comando de nota com suporte a acidentes e duração
├── ui/
│   ├── MainView.java                      # Contrato da tela principal (MVP, view passiva)
│   ├── MainPresenter.java                 # Estado da tela e chamadas aos casos de uso
│   └── swing/                             # Telas Swing dos croquis (sem regra de negócio)
├── usecase/
│   └── MusicLibrary.java                  # Facade: criar, importar, salvar, renomear, deletar
├── repository/
│   ├── FileMusicRepository.java           # Persistência em arquivos no filesystem
│   ├── Music.java                         # Entidade de música
│   └── MusicRepository.java               # Interface do repositório
└── validation/
    └── InputValidator.java                # Validador de entradas com regras por predicados
```

---

## 3. Garantia de Qualidade

Para verificar a conformidade estática e a integridade da suíte de testes:

```bash
mvn verify
```

O comando executa:
1. **Compilação e Testes Unitários**: a suíte cobre interpretação, persistência, presenter, exportação MIDI e regras de configuração.
2. **Spotless**: Validação de formatação segundo o *Google Java Format*.
3. **Checkstyle**: Verificação semântica estrita (`checkstyle.xml`).
4. **SpotBugs**: Detecção de bugs de bytecode em nível máximo de esforço (`Max`).
5. **JaCoCo**: o piso configurado é de 90% de cobertura de linhas.

---

## 4. Reuso e Suposições

- **JFugue:** biblioteca escolhida para converter o padrão interpretado em reprodução MIDI e arquivo MIDI padrão.
- **FlatLaf:** biblioteca escolhida somente para aplicar aparência consistente à interface Swing.
- A biblioteca local fica em `musics/`; arquivos importados continuam em seus caminhos originais.
- A reprodução audível depende de um dispositivo MIDI disponível na máquina de demonstração.
