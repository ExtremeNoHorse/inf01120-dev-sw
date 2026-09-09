# Trabalho Prático INF01120 - Desenvolvimento de Software

## Autores
- André Schaidhauer Luckmann;
- Arthur Von Groll dos Santos;
- Carlos Eduardo Boesel Morari;
- Giovani Ferreira de Alencoi.

## TTM - (Text-To-Music)

O projeto consiste em um **gerador de sequências musicais a partir de um texto de entrada**. O sistema recebe um arquivo de texto e, com base em regras de mapeamento,
produzirá uma sequência de notas musicais que poderá ser reproduzida pelo JFugue. Os parâmetros são definidos através de um mapeamento de texto para informações musicais.

## Glossário

- **Música (Music):** unidade persistida no sistema; composta por id, nome e texto-fonte.
- **Reproduzir (play):** tocar a música via JFugue interpretando o texto-fonte.
- **Exportar (export):** gerar um arquivo a partir do texto interpretado.
- **Playback:** estado corrente da reprodução (posição, parâmetros musicais ativos).

## Lista de Requisitos

### Lista de Requisitos Funcionais

1. Ao iniciar, o sistema exibe a lista de músicas salvas ("Minhas músicas") e permite criar uma nova música a partir do upload de um arquivo `.txt`.
2. O usuário pode selecionar uma música da lista para reproduzi-la. Durante a reprodução, o sistema interpreta o texto-fonte aplicando a tabela de mapeamento do enunciado e emite a saída musical via JFugue.
3. O usuário pode controlar o playback ao vivo: play, pause, resume, stop e navegar pela música.
4. O usuário pode configurar parâmetros musicais durante a reprodução: BPM, oitava, volume e instrumento inicial.
5. O usuário pode renomear e deletar uma música salva.
6. O usuário pode exportar a música interpretada como arquivo persistente em um caminho do filesystem local de sua escolha.

<!-- Fora de escopo Fase 1, mantidos como referência para fases futuras:
- Editor embutido do texto da música.
- Modo interativo por teclado (pressionar teclas toca instantaneamente).
- Destaque visual do trecho corrente durante o playback (sincronizado com a reprodução).
-->

### Lista de Requisitos Não Funcionais

1. **Modularidade por responsabilidade:** O código é organizado em módulos coesos alinhados ao diagrama de classes: entrada e validação (`InputValidator`), persistência (`MusicRepository` + `FileMusicRepository`), interpretação e mapeamento (`Interpreter` + `DomainMapper` + `Command`), execução de áudio (`Player` + `AudioEngine` + `JFuguePlayer`) e composição da aplicação (`App`). Interfaces isolam detalhes trocáveis (motor de áudio, meio de persistência).
2. **Tratamento de erros centralizado:** Erros de domínio são modelados por uma única hierarquia de exceptions e códigos internos, sem stack traces vazando para a UI.
3. **Testabilidade:** Cada módulo é coberto por testes unitários com cobertura de branches.
4. **Padronização de estilo automática.** Google Java Format é aplicado via Spotless no pre-commit hook local e no CI. Divergência de formatação quebra o build.
5. **Repositório GitHub + fluxo de PR.** Toda mudança na `main` passa por Pull Request. CI verde é pré-requisito de merge; revisão humana é convenção do grupo. Histórico de commits reflete o desenvolvimento progressivo.
6. **Pipeline CI/CD.** GitHub Actions (`.github/workflows/ci.yml`) executa Spotless, Checkstyle, SpotBugs e `mvn verify` (testes JUnit + gate JaCoCo) a cada push/PR contra `main`. Relatório de cobertura publicado como artefato.
