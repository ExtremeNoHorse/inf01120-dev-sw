# TTM - Text-To-Music (README refatorado, Fase 1)

> Versao refatorada. Original preservado em `README.md`. Alinha diagrama,
> mockups Figma e requisitos a partir dos resumos de arquitetura da
> disciplina (Software Quality R01, Modularidade R02, TADs R03, OOSC Cap.5).

## Autores
- Andre Schaidhauer Luckmann
- Arthur Von Groll dos Santos
- Carlos Eduardo Boesel Morari
- Giovani Ferreira de Alencoi

## Visao geral

TTM le um `.txt`, mapeia cada caractere para um `Command` musical (nota ou
alteracao de config) e reproduz via JFugue. Cada musica e persistida em
disco e pode ser reexportada como MIDI.

- Entrada: arquivo texto (`.txt`).
- Saida: audio ao vivo + arquivo MIDI opcional.

## Arquitetura

Nucleo desacoplado de UI, audio e persistencia por interfaces. Uma unica
classe UI (`App`) hoje serve CLI; substituir por GUI no futuro nao toca
`Player`, `Interpreter`, `DomainMapper`, `Music`, `MusicRepository`.

```
   App (main + CLI)
     |
     +--> InputValidator
     +--> MusicRepository  <----+   FileMusicRepository
     +--> Player                 (interface)
            |
            +--> Interpreter
            |     +--> DomainMapper
            |     |     +--> Command (Note | ConfigUpdate)
            |     +--> PlayerConfig
            |
            +--> AudioEngine  <-- JFuguePlayer
```

Diagrama completo: [`docs/design/diagram.mmd`](design/diagram.mmd).
Original preservado: [`../classes/diagram.mmd`](../classes/diagram.mmd).
Decisoes: [`docs/adr/`](adr/).

## Terminologia

| Termo | Significado |
|-------|-------------|
| **Music** | Unidade persistida: id, nome, texto-fonte. |
| **PlayerConfig** | Bpm, oitava, volume, instrumento correntes + pattern acumulado. |
| **Command** | Efeito de um caractere: `Note` (inclui silencio) ou `ConfigUpdate`. |
| **AudioEngine** | Interface do motor de audio. `JFuguePlayer` implementa. |
| **Recording** | Arquivo MIDI exportado a partir de uma Music. |
| ~~Session~~ | Termo abandonado. Substituido por **Music** (alinhado ao mockup). |

## Requisitos Funcionais (revisados)

1. Listar musicas salvas ("Minhas musicas" no mockup).
2. Criar nova musica via upload de `.txt` (tela 2 do mockup).
3. Tocar musica: interpretar caracteres, emitir audio, notificar posicao
   atual (highlight do caractere corrente na tela 1).
4. Renomear musica. *(Editor de conteudo embutido = fase futura.)*
5. Deletar musica.
6. Configurar playback ao vivo: BPM, oitava, volume, instrumento.
7. Exportar gravacao como arquivo MIDI.

*Removido:* modo interativo por teclado (RF7 antigo). Ver
`docs/adr/0004-drop-interactive-mode.md`.

## Requisitos Nao Funcionais (revisados)

1. **Modularidade** - responsabilidades definidas por dominio; detalhes
   trocaveis atras de `MusicRepository` e `AudioEngine`.
2. **Modulo de erros** - `TtmException` com enum `ErrorCode`.
3. **Testabilidade** - cada modulo testavel isoladamente; interfaces
   permitem fakes sem tocar JFugue nem filesystem.
4. **SOLID** - `Command` aberto-fechado; dependencias por interface;
   `Interpreter` e `Player` com uma responsabilidade cada.
5. **Repositorio GitHub + PRs + quality gates** - mantido.
6. **CI** - spotless / checkstyle / spotbugs / testes / coverage.
7. **Backend agnostico de UI** - `App` e um dos possiveis consumidores;
   nenhuma logica de negocio em UI.

## Ancora com os resumos de arquitetura

| Guideline | Aplicacao |
|-----------|-----------|
| **Correctness > tudo** (R01) | `InputValidator` na entrada; erros modelados por `TtmException + ErrorCode`. |
| **Extendibility / Descentralizacao** (R01) | Nucleo isolado por interfaces. |
| **Reusability** (R01) | `Command`, `DomainMapper`, `Player` reutilizaveis por outras UIs. |
| **Mapeamento direto** (R02) | Nomes = termos do enunciado (Music, Note, Instrument, PlayerConfig). |
| **Poucas interfaces** (R02) | 2 interfaces (`MusicRepository`, `AudioEngine`) + 1 (`Command`). |
| **Info hiding** (R02) | JFugue so aparece em `JFuguePlayer`. |
| **Aberto-fechado** (R02) | Novo `Command` = nova classe. |
| **Escolha unica** (R02) | Mapa char->Command mora so em `DomainMapper.table`. |
| **TADs** (R03) | `Music` e `PlayerConfig` agrupam dados+operacoes; impl de repo/engine trocavel sem afetar clientes. |
| **Decompor por objetos** (Cap5) | Music, Command, PlayerConfig no centro; play/save/export sao operacoes sobre eles. |

## Estrutura de pastas alvo

```
src/main/java/br/ufrgs/inf01120/ttm/
    App.java, InputValidator.java
    music/Music.java, MusicRepository.java, FileMusicRepository.java
    player/Player.java, Interpreter.java, DomainMapper.java, PlayerConfig.java
    player/AudioEngine.java, JFuguePlayer.java
    command/Command.java, Note.java, ConfigUpdate.java
    error/TtmException.java, ErrorCode.java

src/test/java/... (espelha)

docs/
    README.md            (este arquivo)
    design/diagram.mmd   (diagrama refatorado)
    adr/0001..0005-*.md

README.md               (original, preservado)
classes/diagram.mmd     (original, preservado)
```

## Como rodar

```
mvn verify
java -jar target/ttm.jar list
java -jar target/ttm.jar create <arquivo.txt>
java -jar target/ttm.jar play <musicId>
java -jar target/ttm.jar delete <musicId>
java -jar target/ttm.jar export <musicId> <destino.mid>
```

## Ver tambem

- [`../DEVELOPMENT.md`](../DEVELOPMENT.md) - setup local e tooling.
- [`design/diagram.mmd`](design/diagram.mmd) - diagrama refatorado.
- [`../classes/diagram.mmd`](../classes/diagram.mmd) - diagrama original.
- [`../front/`](../front/) - mockups Figma.
- [`adr/`](adr/) - Architecture Decision Records.
