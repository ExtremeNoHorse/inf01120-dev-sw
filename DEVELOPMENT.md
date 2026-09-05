# Guia de Desenvolvimento

Este projeto usa vários checks automáticos de qualidade que rodam no CI (GitHub Actions) e podem — devem — rodar também na IDE de cada dev. Este guia cobre setup inicial, uso no dia a dia e como replicar os mesmos avisos que aparecem na pipeline dentro do IntelliJ IDEA.

> **Atalhos:** o guia usa os atalhos padrão do IntelliJ para Windows/Linux (`Ctrl`, `Alt`, `Shift`). No macOS, trocar `Ctrl` por `⌘` e `Alt` por `⌥`. Por exemplo, `Ctrl+Alt+S` (abrir Settings) equivale a `⌘,` no macOS.

---

## 1. Setup inicial (uma vez por clone)

### 1.1. Instalar JDK 25

O projeto usa Java 25 (LTS). Distribuição recomendada: Eclipse Temurin.

**Windows:**

Opção A — instalador `.msi`:
1. Baixar em https://adoptium.net/temurin/releases/?version=25 → arquitetura `x64` (ou `aarch64` para Snapdragon), pacote `JDK`, formato `.msi`.
2. Rodar o instalador. Marcar as opções `Set JAVA_HOME variable` e `Add to PATH` durante a instalação.

Opção B — winget (Windows 10/11):
```powershell
winget install EclipseAdoptium.Temurin.25.JDK
```

Opção C — Chocolatey:
```powershell
choco install temurin25
```

**Linux:**

Opção A — SDKMAN (recomendado, não precisa root):
```bash
curl -s "https://get.sdkman.io" | bash
source ~/.sdkman/bin/sdkman-init.sh
sdk install java 25-tem
sdk default java 25-tem
```

Opção B — pacote da distribuição:

Ubuntu/Debian:
```bash
sudo apt update
sudo apt install temurin-25-jdk
```
(pode precisar adicionar o repo Adoptium primeiro: https://adoptium.net/installation/linux/)

Fedora/RHEL:
```bash
sudo dnf install temurin-25-jdk
```

Arch:
```bash
sudo pacman -S jdk-temurin
```

**macOS:**
```bash
brew install --cask temurin@25
```

**Confirmar em qualquer SO:**
```bash
java -version   # deve mostrar 25
```

Se `java` não for reconhecido no Windows, reabrir o terminal (para pegar o `PATH` novo) ou reiniciar a máquina.

### 1.2. Habilitar o hook de pre-commit

Faz com que Spotless + Checkstyle rodem automaticamente antes de cada commit:
```bash
git config core.hooksPath .githooks
```

**Windows:** o hook é um script `bash`. Funciona em Git Bash, WSL e no PowerShell desde que o Git for Windows esteja instalado (ele traz `bash` embutido). Sem Git for Windows, o hook é ignorado silenciosamente e os checks só rodam no CI.

### 1.3. Não é preciso instalar Maven separadamente

O IntelliJ traz um Maven bundled que já resolve tudo. Se preferir CLI:

- **Windows (Chocolatey):** `choco install maven`
- **Windows (winget):** `winget install Apache.Maven`
- **Linux (apt):** `sudo apt install maven`
- **Linux (SDKMAN):** `sdk install maven`
- **macOS (Homebrew):** `brew install maven`

O projeto já ativa as flags certas do Maven via `.mvn/maven.config` — nada a configurar.

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

- Se Spotless reclamar → `mvn spotless:apply` e commit de novo. Ele arruma sozinho.
- Se Checkstyle reclamar → ler mensagem, corrigir no código (Spotless não conserta regras de Checkstyle, só formatação).
- Se SpotBugs reclamar → `mvn spotbugs:gui` para navegar a árvore de findings.

### 2.2. Ajustar o piso de cobertura

Editar `<jacoco.line.coverage.min>` no `pom.xml`. Começou em `0.00` (permite base vazia) e deve subir conforme o código cresce, por exemplo `0.70` quando houver testes reais.

---

## 3. Configurar IntelliJ IDEA para ver os mesmos avisos da pipeline

Todos os passos abaixo servem para que os erros que quebram o build no CI apareçam sublinhados no editor **enquanto você digita**, sem precisar rodar `mvn` manualmente.

> No IntelliJ, o menu **Preferences/Settings** abre com `Ctrl+Alt+S` (Windows/Linux) ou `⌘,` (macOS). Daqui em diante o guia usa `Settings` genericamente.

### 3.1. Apontar Maven para as configs do projeto

`Settings → Build, Execution, Deployment → Build Tools → Maven`:

- **Maven home path**: `Bundled (Maven 3)` (default; ok)
- **User settings file**: marcar `Override` e apontar para o `.mvn/settings.xml` **dentro da pasta do projeto**.
  - Windows: algo como `C:\Users\<seu-usuário>\dev\inf01120-dev-sw\.mvn\settings.xml`
  - Linux/macOS: algo como `/home/<seu-usuário>/dev/inf01120-dev-sw/.mvn/settings.xml`
  - Necessário quando a máquina tem um `~/.m2/settings.xml` global que redireciona resolvers para mirrors internos. O `.mvn/settings.xml` do projeto força Maven Central + JitPack.
- **Use settings from .mvn/maven.config**: marcar.
- Aplicar. No painel Maven (lado direito da IDE), clicar 🔄 **Reload All Maven Projects**.

Se aparecerem erros “Dependency X not found” mesmo depois do Reload, limpar o cache poluído do Maven:

**Windows (PowerShell):**
```powershell
Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\com\puppycrawl" -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\xalan" -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\xerces" -ErrorAction SilentlyContinue
Get-ChildItem "$env:USERPROFILE\.m2\repository" -Recurse -Filter "*.lastUpdated" | Remove-Item
```

**Linux/macOS:**
```bash
rm -rf ~/.m2/repository/com/puppycrawl ~/.m2/repository/xalan ~/.m2/repository/xerces
find ~/.m2/repository -name "*.lastUpdated" -delete
```

Depois: `File → Invalidate Caches → Invalidate and Restart`.

### 3.2. Configurar o SDK

`File → Project Structure → SDKs → +` → apontar para a instalação do JDK 25.

Caminho típico do JDK Temurin 25:
- Windows: `C:\Program Files\Eclipse Adoptium\jdk-25.<patch>-hotspot`
- Linux (apt): `/usr/lib/jvm/temurin-25-jdk-amd64`
- Linux (SDKMAN): `~/.sdkman/candidates/java/25-tem`
- macOS: `/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home`

Escolher um **nome de SDK genérico** (por exemplo `temurin-25`) que faça sentido para qualquer dev do time. Em `Project`, definir Language level = `SDK default (25)`.

### 3.3. Code style — Google Java Format (o mesmo que o Spotless aplica)

Já está versionado em `.idea/codeStyles/Project.xml`. Ao abrir o projeto pela primeira vez, o IntelliJ carrega automaticamente.

Confirmar em `Settings → Editor → Code Style → Java`: no dropdown do topo deve estar `Project` e no seletor de scheme `GoogleStyle`.

Assim `Ctrl+Alt+L` (Windows/Linux) ou `⌘⌥L` (macOS) — **Reformat Code** — gera saída que passa `mvn spotless:check`.

### 3.4. Plugin do Checkstyle (para ver violações no editor)

1. `Settings → Plugins → Marketplace` → buscar `CheckStyle-IDEA` → **Install** → reiniciar IntelliJ.
2. `Settings → Tools → Checkstyle`:
   - **Checkstyle version**: `14.1.0` (mesma do `pom.xml`, propriedade `checkstyle.version`).
   - **Scan Scope**: `Only Java sources (including tests)`.
   - **Treat Checkstyle errors as warnings**: desmarcado.
3. Em **Configuration File** → `+`:
   - Description: `Projeto TTM`
   - `Use a local Checkstyle file` → `$PROJECT_DIR$/checkstyle.xml` → Next → Finish.
   - Marcar como **Active** (checkbox à esquerda).
4. Uso:
   - Aba **CheckStyle** (`View → Tool Windows → CheckStyle`): botão ▶ para escanear módulo/arquivo.
   - Violações também aparecem sublinhadas no editor.

### 3.5. Plugin do SpotBugs (opcional, mas recomendado)

1. `Settings → Plugins → Marketplace` → `SpotBugs IntelliJ IDEA` → **Install** → reiniciar.
2. Menu `Analyze → SpotBugs → Analyze Project Files` ou clicar direito em pacote → `SpotBugs → Analyze`.
3. Resultados na aba **SpotBugs Bug Report**.

Não há config específica a apontar; o plugin usa defaults compatíveis com o Maven plugin. Se quiser paridade exata, `pom.xml` está com `effort=Max, threshold=Low` (mais estrito).

### 3.6. Inspetor de código do IntelliJ

Perfil de inspeções compartilhado já vem versionado em `.idea/inspectionProfiles/Project_Default.xml`. Ele adiciona regras que o Checkstyle não cobre (dataflow, null analysis, uso de padrões modernos, etc). Nada a fazer além de abrir o projeto.

Rodar sob demanda: `Analyze → Inspect Code → Whole project → OK`.

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
Versão do plugin diferente da do `pom.xml`. Alinhar em `Settings → Tools → Checkstyle` → **Checkstyle version** = `14.1.0`.

**Spotless reclama de indentação do `pom.xml`.**
Não deve acontecer. O Spotless neste projeto só formata Java. Se a IDE reformatou o `pom.xml`, ok — Spotless ignora.

**Coverage sempre 0% mesmo com testes.**
Faltando `mvn verify` (JaCoCo só gera relatório em `verify`). `mvn test` sozinho não basta.

**Windows: `mvn` não é reconhecido no PowerShell.**
Após instalar Maven, reabrir o terminal (o `PATH` só recarrega em sessões novas). Se persistir, verificar `System Properties → Environment Variables → Path` contém a pasta `bin` do Maven.

**Windows: hook de pre-commit não executa.**
Requer Git for Windows (traz `bash` embutido). Baixar em https://git-scm.com/download/win. Reabrir o terminal depois de instalar.
