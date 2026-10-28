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
| Tela 2 | `EditorPanel` | Editor com rolagem e botão **salvar**; arquivos importados também sobrescrevem o TXT de origem. |
| Tela 3 | `MusicListPanel` | Clique com o botão direito seleciona o item e abre **renomear**, **deletar** (com confirmação) e **exportar música**; área vazia não abre menu. |
| Tela 4 | `EditorPanel` + `PlaybackBar` | Durante a reprodução, o caractere tocado em **cada linha (voz)** fica destacado em vermelho; o editor fica somente-leitura. Comandos sem som (`?`, `!`, espaço...) não ganham destaque; uma voz que já terminou fica sem destaque. |
| Barra inferior | `PlaybackBar` | Progresso (arrastável), Oitava −/+, voltar ao início, play/pause, stop, próxima música, BPM −/+ e volume. |

Mudar oitava, BPM ou volume durante a reprodução reinterpreta o texto e **retoma do mesmo ponto**.

## Croquis e justificativas

### Tela 1 — Início

![Tela 1](../front/Tela_1.png)

Oferece os dois modos de entrada exigidos: digitação e importação de TXT. A biblioteca permanece visível para situar a criação no conjunto de músicas salvas.

### Tela 2 — Editor

![Tela 2](../front/Tela_2.jpeg)

O editor ocupa a área principal para favorecer revisão do texto. Salvar mantém músicas criadas localmente e atualiza também a origem de músicas importadas.

### Tela 3 — Ações da biblioteca

![Tela 3](../front/Tela_3.jpeg)

O menu contextual atua somente no item selecionado, reduzindo o risco de renomear, deletar ou exportar a música errada.

### Tela 4 — Reprodução

![Tela 4](../front/Tela_4.png)

O editor é bloqueado durante reprodução e o destaque vermelho acompanha cada voz; a barra reúne progresso, pausa, parada, navegação e parâmetros globais.

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
- A exportação acrescenta `.mid` ao nome sem extensão e pede confirmação antes de substituir um arquivo existente.
- O áudio depende de dispositivo MIDI disponível na máquina que executa a aplicação.
