# Interface Gráfica (Swing)

Protótipo da interface projetada nos croquis (`front/Tela_1..4`), implementado em **Java Swing** com o tema [FlatLaf](https://www.formdev.com/flatlaf/) (único jar extra, só para o visual).

## Como executar

Sem argumentos, o `App` abre a interface gráfica. Com argumentos, continua funcionando como CLI (`play` / `export`).

```bash
mvn compile exec:java -Dexec.mainClass=com.player.App
```

Ou pela IDE: rodar `com.player.App` sem argumentos. As músicas ficam salvas na pasta `musics/` do diretório de execução, que está no `.gitignore`.

## Telas

| Croqui | Implementação | Comportamento |
|--------|---------------|---------------|
| Tela 1 | `StartPanel` | "Digitar Texto" pede o nome e cria uma música vazia. "Fazer Upload" importa um `.txt` com o nome do arquivo. |
| Tela 2 | `EditorPanel` | Editor com rolagem e botão **salvar**, que grava o texto na música aberta. |
| Tela 3 | `MusicListPanel` | Clique com o botão direito: **renomear**, **deletar** (com confirmação) e **exportar música** (seletor de arquivo `.mid`). |
| Tela 4 | `EditorPanel` + `PlaybackBar` | Durante a reprodução, o caractere tocado em **cada linha (voz)** fica destacado em vermelho, acompanhando a duração real de cada nota. Comandos sem som (`?`, `!`, espaço...) não ganham destaque; uma voz que já terminou fica sem destaque. |
| Barra inferior | `PlaybackBar` | Progresso (arrastável), Oitava −/+, voltar ao início, play/pause, próxima música, BPM −/+ e volume. |

Mudar oitava, BPM ou volume durante a reprodução reinterpreta o texto e **retoma do mesmo ponto**.

## Arquitetura (Clean Architecture)

As dependências apontam só para dentro: a tela conhece o presenter, o presenter conhece os casos de uso, e os casos de uso conhecem o domínio. O domínio não sabe que existe Swing.

```text
 Frameworks / UI        ui.swing.*  (MainWindow, StartPanel, EditorPanel, PlaybackBar, MusicListPanel)
        │               FileMusicRepository, JFuguePlayer
        ▼
 Adaptadores            ui.MainPresenter  ◄──  ui.MainView (interface)
        │
        ▼
 Casos de uso           usecase.MusicLibrary, Player, InputValidator
        │
        ▼
 Entidades / domínio    Music, PlayerConfig, Interpreter, DomainMapper, Command
```

- **Reuso:** nenhuma regra foi reescrita. Tocar, pausar e exportar usam o `Player`, a validação usa o `InputValidator`, e a persistência usa o `MusicRepository`.
- A camada `ui.swing` não tem lógica. Por isso ela é a única excluída do gate de cobertura do JaCoCo (`pom.xml`), e todo o comportamento é testado em `MainPresenterTest` com uma view falsa.

## Padrões de projeto usados

| Padrão | Onde | Para quê |
|--------|------|----------|
| **MVP (Passive View)** | `MainView` + `MainPresenter` + `MainWindow` | A tela só desenha e repassa eventos. O estado e as decisões ficam no presenter, que pode ser testado sem abrir janela. |
| **Facade** | `MusicLibrary` | Um ponto único para os casos de uso da lista (criar, importar, salvar, renomear, deletar e listar). |
| **Observer** | listeners Swing e `javax.swing.Timer` → `presenter.onTick()` | A tela avisa o presenter dos eventos. O timer consulta o progresso a cada 100 ms. |
| **Adapter** | `JFuguePlayer` (já existia) | Isola o JFugue atrás de `AudioEngine`. Foram adicionados apenas `getTickPosition`, `getTickLength` e `isActive`. |
| **Command** | `Command` do interpretador (já existia) | Não foi alterado. |

## Mudanças no backend

1. **`FileMusicRepository`** passou a persistir o nome em `<id>.name`, ao lado de `<id>.txt`. Antes o nome era derivado do id e o "renomear" se perderia.
2. **`Interpreter.interpret(text, settings)`**: o BPM vem dos settings. A oitava desloca **todas** as vozes em relação ao valor base, então cada voz mantém o seu registro (o soprano continua acima do baixo). O volume vira o volume do canal MIDI (`:CE(7,v)`) de cada voz, funcionando como volume geral. Não dá para mexer na intensidade das notas, porque o comando espaço a dobra até 127 e anularia o controle. `interpret(text)` continua igual.
3. **`Player`** passou a aplicar a própria configuração (`configure`) ao tocar e exportar. Antes ela era guardada mas ignorada. Também foram adicionados `seek` e as consultas de progresso.
4. **`AudioEngine`** ganhou `getTickPosition`, `getTickLength` e `isActive`.
5. **`Interpreter.timeline(text)`** registra, para cada caractere que gera nota ou pausa, o tempo musical (em semínimas) em que ele começa e termina (`Timeline`). O tempo musical não depende do BPM e corresponde aos ticks da sequência MIDI, então o destaque segue a posição real da música. Para isso, `MusicalDuration` ganhou `getBeats()` e `fromJfugueToken()`.
6. **`JFuguePlayer.play`** monta a sequência com um parser novo a cada chamada e inicia o sequenciador sem abrir thread. O parser compartilhado do JFugue não zera o tempo das vozes além da `V0`, então a segunda reprodução de uma música com várias vozes começava atrasada e com o BPM antigo. Além disso, as threads de reinícios seguidos (cliques no BPM) disputavam o mesmo parser.

## Suposições e limitações

- "Próxima" (`▶|`) abre a próxima música da lista. "Anterior" (`|◀`) volta ao início da música atual.
- Trocar de música descarta edições não salvas.
