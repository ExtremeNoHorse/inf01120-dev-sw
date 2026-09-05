# Guia de Desenvolvimento

Este projeto usa vários checks automáticos de qualidade que rodam no CI (GitHub Actions) e podem — devem — rodar também na IDE de cada dev. Este guia cobre setup inicial, uso no dia a dia e como replicar os mesmos avisos que aparecem na pipeline dentro do IntelliJ IDEA.

---

## 1. Setup inicial (uma vez por clone)

### 1.1. Instalar JDK 25

O projeto usa Java 25 (LTS). Instalar via uma distribuição pública, por exemplo:

**macOS (Homebrew):**
```bash
brew install --cask temurin@25
```

**macOS/Linux (SDKMAN):**
```bash
curl -s "https://get.sdkman.io" | bash
source ~/.sdkman/bin/sdkman-init.sh
sdk install java 25-tem
sdk default java 25-tem
```

**Windows:** baixar em https://adoptium.net/temurin/releases/?version=25.

Confirmar:
```bash
java -version   # deve mostrar 25
```

### 1.2. Habilitar o hook de pre-commit

Faz com que Spotless + Checkstyle rodem automaticamente antes de cada commit:
```bash
git config core.hooksPath .githooks
```

### 1.3. Não é preciso instalar Maven separadamente

O IntelliJ traz um Maven bundled que já resolve tudo. Se preferir CLI, instalar Maven 3.9+ pela sua ferramenta padrão (`brew install maven`, `sdk install maven`, etc). O projeto ativa flags do Maven automaticamente via `.mvn/maven.config`.

---

## 2. Ferramentas de qualidade

Todas rodam no CI e podem rodar localmente. Ordem de execução em `mvn verify`:

| Ferramenta | O que faz | Config | Bloqueia build? |
|---|---|---|---|
| **Spotless** | Formata o código no padrão Google Java Format. Verifica que está formatado. | `pom.xml` (bloco `spotless-maven-plugin`) | sim |
| **Checkstyle** | Regras estáticas de estilo, naming, complexidade, javadoc, etc. | `checkstyle.xml` | sim (severidade `error`) |
| **SpotBugs** | Análise estática de bytecode para bugs comuns (null derefs, resource leaks, comparações erradas). | `pom.xml` (bloco `spotbugs-maven-plugin`) | sim |
| **JaCoCo** | Cobertura de testes. Falha o build se cair abaixo de um piso. | `pom.xml` (`jacoco.line.coverage.min`) | sim quando piso > 0 |

Além delas, no CI:
- **GitHub Actions** roda tudo acima em cada push e PR para `main`.
- **Pre-commit hook** roda Spotless + Checkstyle nos `.java` staged.

### 2.1. Comandos comuns

| Tarefa | Comando |
|---|---|
| Compilar tudo | `mvn compile` |
| Rodar testes | `mvn test` |
| Quality gate completo (idêntico ao CI) | `mvn verify` |
| Formatar código automaticamente | `mvn spotless:apply` |
| Só checar formatação | `mvn spotless:check` |
| Só checar estilo | `mvn checkstyle:check` |
| Só análise de bugs | `mvn spotbugs:check` |
| Abrir análise de bugs em GUI | `mvn spotbugs:gui` |
| Ver relatório de cobertura | abrir `target/site/jacoco/index.html` depois de `mvn verify` |

Regra prática: **antes de abrir PR, rodar `mvn verify` local**. Se passar local, passa no CI.

Se Spotless reclamar → `mvn spotless:apply` e commit de novo. Ele arruma sozinho.
Se Checkstyle reclamar → ler mensagem, corrigir no código (Spotless não conserta regras de Checkstyle, só formatação).
Se SpotBugs reclamar → `mvn spotbugs:gui` para navegar a árvore de findings.

### 2.2. Ajustar o piso de cobertura

Editar `<jacoco.line.coverage.min>` no `pom.xml`. Começou em `0.00` (permite base vazia) e deve subir conforme o código cresce, por exemplo `0.70` quando houver testes reais.

---

## 3. Configurar IntelliJ IDEA para ver os mesmos avisos da pipeline

Todos os passos abaixo são para que os erros que quebram o build no CI apareçam sublinhados no editor **enquanto você digita**, sem precisar rodar `mvn` manualmente.

### 3.1. Apontar Maven para as configs do projeto

`⌘,` (Preferences) → **Build, Execution, Deployment → Build Tools → Maven**:

- **Maven home path**: `Bundled (Maven 3)` (default; ok)
- **User settings file**: marcar `Override` e apontar para `<PROJECT_DIR>/.mvn/settings.xml`
  - Necessário quando a máquina tem um `~/.m2/settings.xml` global que redireciona resolvers para mirrors internos. O `.mvn/settings.xml` do projeto força Maven Central + JitPack.
- **Use settings from .mvn/maven.config**: marcar.
- Aplicar. No painel Maven (direita da IDE), clicar 🔄 **Reload All Maven Projects**.

Se aparecerem erros “Dependency X not found” mesmo depois do Reload:
```bash
# limpar cache Maven poluído por resolvers antigos
rm -rf ~/.m2/repository/com/puppycrawl ~/.m2/repository/xalan ~/.m2/repository/xerces
find ~/.m2/repository -name "*.lastUpdated" -delete
```
Depois: `File → Invalidate Caches → Invalidate and Restart`.

### 3.2. Configurar o SDK

`File → Project Structure → SDKs → +` → apontar para a instalação do JDK 25 (Temurin ou similar).
- Escolher um nome de SDK genérico (por ex. `temurin-25`) que faça sentido para qualquer dev do time.
- Em `Project`, definir Language level = `SDK default (25)`.

### 3.3. Code style — Google Java Format (o mesmo que o Spotless aplica)

Já está versionado em `.idea/codeStyles/Project.xml`. Ao abrir o projeto pela primeira vez, o IntelliJ carrega automaticamente.

Confirmar em `⌘,` → **Editor → Code Style → Java**: no dropdown do topo deve estar `Project` e no seletor de scheme `GoogleStyle`.

Assim `⌘⌥L` (Reformat Code) gera saída que passa `mvn spotless:check`.

### 3.4. Plugin do Checkstyle (para ver violações no editor)

1. `⌘,` → **Plugins → Marketplace** → buscar `CheckStyle-IDEA` → **Install** → reiniciar IntelliJ.
2. `⌘,` → **Tools → Checkstyle**:
   - **Checkstyle version**: `14.1.0` (mesma do `pom.xml`, propriedade `checkstyle.version`).
   - **Scan Scope**: `Only Java sources (including tests)`.
   - **Treat Checkstyle errors as warnings**: desmarcado.
3. Em **Configuration File** → `+`:
   - Description: `Projeto TTM`
   - `Use a local Checkstyle file` → `$PROJECT_DIR$/checkstyle.xml` → Next → Finish.
   - Marcar como **Active** (checkbox à esquerda).
4. Uso:
   - Aba **CheckStyle** (View → Tool Windows → CheckStyle): botão ▶ para escanear módulo/arquivo.
   - Violações também aparecem sublinhadas no editor.

### 3.5. Plugin do SpotBugs (opcional, mas recomendado)

1. `⌘,` → **Plugins → Marketplace** → `SpotBugs IntelliJ IDEA` → **Install** → reiniciar.
2. Menu **Analyze → SpotBugs → Analyze Project Files** ou clicar direito em pacote → **SpotBugs → Analyze**.
3. Resultados na aba **SpotBugs Bug Report**.

Não há config específica a apontar; o plugin usa defaults compatíveis com o Maven plugin. Se quiser paridade exata, `pom.xml` está com `effort=Max, threshold=Low` (mais estrito).

### 3.6. Inspetor de código do IntelliJ

Perfil de inspeções compartilhado já vem versionado em `.idea/inspectionProfiles/Project_Default.xml`. Ele adiciona regras que o Checkstyle não cobre (dataflow, null analysis, uso de padrões modernos, etc). Nada a fazer além de abrir o projeto.

Rodar sob demanda: **Analyze → Inspect Code → Whole project → OK**.

### 3.7. Cobertura JaCoCo no IntelliJ

Depois de rodar `mvn verify`, existe `target/site/jacoco/index.html`. Abrir no navegador para ver linha por linha o que está coberto.

Para rodar testes com cobertura direto no IntelliJ:
- Botão direito em `src/test/java` → **Run 'All Tests' with Coverage**.
- IntelliJ exibe a % ao lado dos números de linha (números com fundo verde/vermelho).

Nota: a % que aparece no IntelliJ é do runner interno, não do JaCoCo. Para o gate real, sempre confiar em `mvn verify`.

---

## 4. Fluxo recomendado por PR

1. Criar branch a partir de `main`.
2. Escrever código + testes.
3. `mvn spotless:apply` para formatar.
4. `mvn verify` — se falhar, corrigir. Se passar local, passa no CI.
5. `git commit` — o hook de pre-commit roda Spotless + Checkstyle nos arquivos staged. Se pintar vermelho, corrigir e re-commit.
6. `git push` + abrir PR.
7. Aguardar CI verde. Só depois pedir review.

---

## 5. Troubleshooting

**Erro `Dependency X not found` no IntelliJ mas `mvn verify` na CLI funciona.**
IntelliJ está usando `~/.m2/settings.xml` global com algum mirror que sobrescreve o resolver. Ver §3.1.

**Warning `sun.misc.Unsafe` no output do Maven.**
Aviso do Maven 3.9 embedded do IntelliJ contra JDK 25. Já silenciado via `.mvn/jvm.config`. Não afeta build.

**Checkstyle plugin do IntelliJ mostra violações diferentes do `mvn checkstyle:check`.**
Versão do plugin diferente do pom. Alinhar em `⌘,` → **Tools → Checkstyle** → **Checkstyle version** = `14.1.0`.

**Spotless reclama de indentação da `pom.xml`.**
Não. O Spotless neste projeto só formata Java. Se a IDE reformatou o `pom.xml`, ok — Spotless ignora.

**Coverage sempre 0% mesmo com testes.**
Faltando `mvn verify` (JaCoCo só gera relatório em `verify`). `mvn test` sozinho não basta.
