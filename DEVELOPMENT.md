# Guia de Desenvolvimento

## Requisitos
- JDK 25
- Maven 3.9+

## Configuração — habilitar o hook de pre-commit (uma vez por clone)
```bash
git config core.hooksPath .githooks
```

## Comandos comuns
| Tarefa | Comando |
|--------|---------|
| Compilar | `mvn compile` |
| Rodar testes | `mvn test` |
| Quality gate completo (testes + cobertura + estilo + bugs) | `mvn verify` |
| Formatar código automaticamente | `mvn spotless:apply` |
| Apenas verificar formatação | `mvn spotless:check` |
| Verificar estilo | `mvn checkstyle:check` |
| Análise estática de bugs | `mvn spotbugs:check` |
| Relatório de bugs (HTML/GUI) | `mvn spotbugs:gui` |
| Relatório de cobertura | abrir `target/site/jacoco/index.html` após `mvn verify` |

## Quality gates aplicados
- **Spotless** — Google Java Format. CI falha se código não estiver formatado.
- **Checkstyle** — ver `checkstyle.xml`. Warnings quebram o build.
- **SpotBugs** — effort=Max, threshold=Low.
- **JaCoCo** — cobertura de linhas ≥ 70% (`jacoco.line.coverage.min` no `pom.xml`).

Os quatro rodam em `mvn verify`, no hook de pre-commit (apenas Spotless + Checkstyle, para ser rápido) e no GitHub Actions em todo push e PR para `main`.

## Ajustar o limite de cobertura
Editar `<jacoco.line.coverage.min>` no `pom.xml`. Começar baixo enquanto a base de código é pequena, apertar conforme cresce.
