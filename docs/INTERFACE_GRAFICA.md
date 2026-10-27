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
| **Adapter** | `JFuguePlayer` (já existia) | Isola o JFugue atrás de `AudioEngine`, que ganhou as consultas de progresso. |
| **Command** | `Command` do interpretador (já existia) | Não foi alterado. |

## Mudanças no backend

A interface precisou de alguns ajustes no backend: persistir o nome, aplicar as configurações ao tocar e exportar, consultar o progresso, a `Timeline` do destaque e a correção do `JFuguePlayer`. Eles estão documentados junto de cada classe em [ARQUITETURA.md](ARQUITETURA.md) e nas mudanças 1 e 2 de [FASE2_IMPLEMENTACAO.md](FASE2_IMPLEMENTACAO.md).

## Suposições e limitações

- "Próxima" (`▶|`) abre a próxima música da lista. "Anterior" (`|◀`) volta ao início da música atual.
- Trocar de música descarta edições não salvas.
