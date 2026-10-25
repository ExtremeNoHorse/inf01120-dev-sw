# Arquitetura do Sistema TTM (Text-To-Music)

Este documento apresenta a fundamentação arquitetural do projeto TTM, os padrões de projeto adotados e a aplicação prática dos princípios SOLID e eliminação de *code smells*.

---

## 1. Visão Geral da Arquitetura

O sistema é estruturado em camadas desacopladas seguindo os princípios de modularidade e separação de responsabilidades:

1. **Camada de Aplicação / Entrada**:
   - `App`: *Composition Root* do sistema. Responsável por instanciar e injetar as dependências.
   - `InputValidator`: Valida o texto-fonte contra regras parametrizáveis antes da execução.
2. **Camada de Domínio / Interpretação**:
   - `Interpreter`: Lê o texto linha a linha, gerenciando as vozes polifônicas independentes e delays.
   - `DomainMapper`: Dicionário e resolvedor de comandos musicais.
   - `Command`: Interface que encapsula ações musicais sem acoplamento reverso.
   - `Note` e `ConfigUpdate`: Implementações concretas de comandos.
   - `MusicalDuration`: Enum que encapsula as figuras de tempo da partitura.
   - `PlayerConfig`: Encapsula o estado musical (oitavas, volumes, instrumentos e BPM).
3. **Camada de Áudio / Execução**:
   - `Player`: Orquestrador de alto nível que conecta interpretação, reprodução e exportação.
   - `AudioEngine`: Interface que abstrai o motor de áudio.
   - `JFuguePlayer`: Adaptador que isola a biblioteca externa JFugue.
4. **Camada de Persistência**:
   - `Music`: Entidade que representa uma composição textual e seu estado.
   - `MusicRepository`: Interface para operações de CRUD e leitura por caminho.
   - `FileMusicRepository`: Implementação baseada em arquivos no sistema de arquivos local (`java.nio.file`).
5. **Tratamento Centralizado de Erros**:
   - `TtmException`: Exceção não-checada de domínio.
   - `ErrorCode`: Enum que tipifica causas de erro (`INVALID_INPUT`, `PERSISTENCE_FAILURE`, etc.).

---

## 2. Princípios SOLID Aplicados

### S — Single Responsibility Principle (Responsabilidade Única)
- Cada classe possui uma única razão para mudar:
  - `Interpreter` apenas converte texto em *patterns* JFugue.
  - `Player` apenas coordena o ciclo de vida da reprodução e exportação.
  - `DomainMapper` apenas traduz caracteres e tokens em comandos.
  - `FileMusicRepository` apenas gerencia a leitura e escrita no disco.
  - `JFuguePlayer` apenas interage com o subsistema de áudio.

### O — Open/Closed Principle (Aberto para Extensão, Fechado para Modificação)
- Novas regras ou efeitos musicais são adicionados criando uma nova implementação de `Command` e registrando-a no `DomainMapper`, sem alterar o código do `Interpreter` ou do `Player`.
- Novos formatos de saída ou motores de áudio podem ser implementados satisfazendo `AudioEngine`, sem alterar as classes de domínio.

### L — Liskov Substitution Principle (Substituição de Liskov)
- `Interpreter` opera sobre a abstração `Command.execute(PlayerConfig)` sem conhecer se o comando é uma emissão de nota (`Note`) ou uma mudança de parâmetro (`ConfigUpdate`).
- `Player` consome qualquer implementação de `AudioEngine` (podendo receber mocks em testes unitários sem alterar o comportamento).

### I — Interface Segregation Principle (Segregação de Interfaces)
- Interfaces são coesas e específicas:
  - `Command`: possui apenas o método `execute(PlayerConfig)`.
  - `AudioEngine`: contrato focado estritamente em comandos de playback e seek.
  - `MusicRepository`: operações focadas no ciclo de vida de `Music`.

### D — Dependency Inversion Principle (Inversão de Dependência)
- Componentes de alto nível (`App`, `Player`) dependem de abstrações (`MusicRepository`, `AudioEngine`, `Command`), nunca de classes concretas acopladas a bibliotecas externas.

---

## 3. Prevenção e Eliminação de Code Smells (Fase 3 Checklist)

| Bad Smell | Como foi evitado / eliminado na arquitetura |
|-----------|--------------------------------------------|
| **Código Duplicado** | Lógica de cálculo de tokens JFugue centralizada em métodos dedicados; ciclo polifônico reaproveita `PlayerConfig.forVoice()`. |
| **Classe Grande (Large Class)** | Responsabilidades divididas entre `Interpreter`, `Player`, `PlayerConfig` e `AudioEngine`. |
| **Método Longo (Long Method)** | Métodos de parsing e registro quebrados em subrotinas curtas com responsabilidade única. |
| **Lista Longa de Parâmetros** | Parâmetros de oitava, volume e instrumento agrupados em `PlayerConfig`. |
| **Inveja de Recursos (Feature Envy)** | Modificações de oitava, volume e BPM pertencem à própria classe `PlayerConfig`. |
| **Obsessão por Tipos Primitivos** | Figuras de duração modeladas pelo enum `MusicalDuration`; erros modelados por `ErrorCode`. |
| **Switch Statements / If-Else Longos** | Polimorfismo através da interface `Command` e tabela de mapeamento do `DomainMapper`. |
| **Cirurgia com Espingarda (Shotgun Surgery)** | Adicionar um novo instrumento ou nota altera apenas o catálogo ou mapa de comandos. |
| **Classes Alternativas com Interfaces Diferentes** | `JFuguePlayer` padronizado sob o contrato unificado de `AudioEngine`. |
