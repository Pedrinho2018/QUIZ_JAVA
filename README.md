# Empresa Segura

## Identificacao academica

- `Disciplina`: Paradigmas de Linguagens de Programacao
- `Turma`: T01 `(2026.1 - 6N234)`
- `Avaliacao`: Avaliacao 2
- `Grupo`: 8

### Integrantes

- Renato Araujo
- Pedro Henrique F. Silva
- Pedro Henrique Figueiredo
- Oliabe

## Documentacao de status

Para uma visao completa do que esta acontecendo no projeto hoje, consulte:

- [docs/STATUS_PROJETO.md](/C:/Users/pedro/OneDrive/Desktop/progamas_resolvedores/QUIZ_JAVA/docs/STATUS_PROJETO.md:1)

## Descricao

Empresa Segura e um quiz corporativo sobre golpes digitais com **backend em Java orientado a objetos** e **interface web** em `HTML`, `CSS` e `JavaScript`.

O projeto foi mantido leve de proposito:

- sem Spring Boot
- com SQLite embarcado para persistir perguntas
- sem dependencias externas no backend
- com regras de negocio isoladas em classes Java

## Tecnologias

- `Java`
- `Servidor HTTP nativo do Java`
- `SQLite`
- `HTML`
- `CSS`
- `JavaScript`
- `Docker`

## Estrutura

```text
src/
  app/
    Main.java
    Autoteste.java
  model/
    Pergunta.java
    PerguntaFacil.java
    PerguntaDificil.java
    Jogador.java
    Rodada.java
    Placar.java
  repository/
    PerguntaRepository.java
  web/
    ServidorWeb.java
web/
  index.html
  styles.css
  app.js
```

## Regras de negocio

As classes principais continuam em Java OOP:

- `Pergunta`: abstracao da pergunta
- `PerguntaFacil`: vale `10` pontos
- `PerguntaDificil`: vale `20` pontos
- `Jogador`: dados do participante
- `Rodada`: fluxo do quiz e processamento das respostas
- `Placar`: pontuacao, acertos, classificacao e mensagem final

Classificacao final:

- `0 a 30`: `Alerta Vermelho`
- `31 a 60`: `Em Treinamento`
- `61 a 90`: `Colaborador Atento`
- `acima de 90`: `Guardiao Digital`

## Interface

A interface web segue a referencia visual do projeto com:

- tela de abertura em formato hero
- identificacao com painel lateral de etapas
- quiz em duas colunas
- feedback imediato apos cada resposta
- tela final com score, perfil de risco e trilha recomendada

## Como executar

As perguntas sao persistidas no arquivo `data/quiz.db`.

Na primeira execucao, o sistema cria a tabela `perguntas` e importa o conteudo de `data/perguntas_quiz_ciberseguranca_300.csv`.

### Mais simples

- `executar.bat`: compila e sobe o sistema
- `executar-docker.bat`: sobe o sistema em Docker
- `parar-docker.bat`: derruba o container Docker
- `testar.bat`: compila e roda o autoteste

### Compilar manualmente

```powershell
javac --release 8 -cp "lib\sqlite-jdbc.jar" -d build src\app\*.java src\model\*.java src\repository\*.java src\web\*.java
```

### Rodar manualmente

```powershell
java -cp "build;lib\sqlite-jdbc.jar" app.Main
```

Depois abra:

```text
http://localhost:8080
```

## Como executar com Docker

### Mais simples

```powershell
executar-docker.bat
```

Depois abra:

```text
http://localhost:8080
```

Para parar:

```powershell
parar-docker.bat
```

### Manualmente

```powershell
docker compose up --build
```

Para derrubar:

```powershell
docker compose down
```

### Se a porta 8080 estiver ocupada

Altere o arquivo `.env`:

```text
APP_PORT=8081
```

Depois suba novamente com:

```powershell
docker compose up --build
```

## Autoteste

O autoteste roda sem interface e valida:

- fluxo com pontuacao maxima
- fluxo sem pontuacao por timeout
- faixas de classificacao do placar

Execucao manual:

```powershell
java -cp "build;lib\sqlite-jdbc.jar" app.Main --self-test
```

## Objetivo academico

Este projeto foi feito para demonstrar:

- abstracao
- heranca
- encapsulamento
- polimorfismo
- separacao de responsabilidades
- integracao entre backend Java e interface web

# QUIZ_JAVA
