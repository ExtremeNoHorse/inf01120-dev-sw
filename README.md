# Trabalho Prático INF01120 - Desenvolvimento de Software

## Autores
- André Schaidhauer Luckmann;
- Arthur Von Groll dos Santos;
- Carlos Eduardo Boesel Morari;
- Giovani Ferreira de Alencoi.

## TTM - (Text-To-Music)

O projeto consiste em um **gerador de sequências musicais a partir de um texto de entrada**. O sistema recebe um arquivo de texto e, com base em regras de mapeamento,
produzirá uma sequência de notas musicais que poderá ser reproduzida pelo JFugue. Os parâmetros são definidos através de um mapeamento de texto para informações musicais.

## Lista de Requisitos

### Lista de Requisitos Funcionais
1. Ao iniciar, o sistema deve exibir um menu que o usuário pode importar uma nova música ou acessar músicas anteriores já salvas.
2. Ao entrar em uma música, o usuário pode escolher ouvir a última gravação daquela música, se houver, gravar uma nova música.
3. Ao escolher ouvir a última gravação da música o sistema deve interpretar cada caracter, aplicando o mapeamento pelas regras definidas, gerando uma saída musical.
<!--4. Ao escolher editar a música, o usuário pode editar o arquivo de música daquela música.-->
4. Ao escolher gravar uma nova música, o usuário irá reproduzir o arquivo da música e salvá-lo em disco.
5. O usuário deve poder configurar alguns parâmetros iniciais (como BPM – batidas por minuto, instrumento inicial, oitava padrão, volume, etc).
<!--7. (a verificar) Haverá também um modo interativo, onde o usuário pressiona teclas do teclado e os sons tocam instantaneamente, podendo gravar a sequência ou não.-->

### Lista de Requisitos Não Funcionais
1. **Modularidade**: O sistema terá diferentes módulos com responsabilidades definidas pelo domínio da aplicação (leitura de arquivos, geração de música, player de música, validação de dados...).
<!--2. Haverão módulos dedicados a: validações de entradas e regras de negócio, mapeamento de entradas, código de erros e casos de uso.-->
2. O software deverá ser testável unitariamente em cada módulo, de forma que os testes sirvam como documentação e garantia de detecção de erros.
3. O software deverá seguir as boas práticas como princípios SOLID aliados a conceitos de Orientação à Objetos.
4. O grupo organizará o trabalho a partir de um repositório no GitHub e quality gates, além do processo de revisão de PRs para poder efetuar merges na branch principal.
5. A pipeline dos quality gates irá rodar os testes unitários, linters e verificações similares.
