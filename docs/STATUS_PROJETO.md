# Status do Projeto - Empresa Segura

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

## Visao Geral

O projeto `Empresa Segura` e uma plataforma de quiz de conscientizacao em ciberseguranca com:

- `backend em Java orientado a objetos`
- `servidor HTTP proprio em Java`
- `frontend web em HTML, CSS e JavaScript`
- `execucao principal via Docker`

Hoje o sistema funciona como uma aplicacao web leve, sem banco de dados e sem framework pesado no backend.

## Objetivo Atual do Projeto

O sistema foi adaptado para entregar uma experiencia visual mais moderna no navegador, mantendo a regra de negocio em Java OOP.

O foco atual do projeto esta em:

- simulacao de golpes digitais em ambiente corporativo
- treinamento de resposta segura
- feedback imediato por pergunta
- classificacao final de risco
- execucao simples em Docker

## Stack Atual

### Backend

- `Java`
- `ServerSocket` para servidor HTTP proprio
- serializacao JSON manual
- controle de sessao em memoria por cookie

### Frontend

- `HTML`
- `CSS`
- `JavaScript` puro

### Infraestrutura

- `Docker`
- `Docker Compose`

## Estrutura Atual

```text
src/
  app/
    Main.java
    Autoteste.java
  model/
    Jogador.java
    Pergunta.java
    PerguntaFacil.java
    PerguntaDificil.java
    Placar.java
    Rodada.java
  web/
    ServidorWeb.java

web/
  index.html
  styles.css
  app.js

docker-compose.yml
Dockerfile
.env
README.md
```

## Como o Sistema Funciona Hoje

### Fluxo principal

1. O usuario acessa a aplicacao web.
2. A API retorna `welcome` quando nao existe sessao.
3. O usuario inicia a identificacao.
4. A aplicacao envia `nome` e `setor` para `/api/start`.
5. O backend cria uma sessao com cookie `empresa_segura_sid`.
6. A rodada com 100 perguntas e embaralhada e iniciada.
7. Cada resposta gera uma tela de feedback.
8. Ao final, o sistema mostra score, classificacao e mensagem final.

### Estado de sessao

As sessoes ficam em memoria no processo Java.

Isso significa:

- nao ha persistencia em banco
- reiniciar o container limpa as sessoes
- o sistema e apropriado para apresentacao, estudo e demonstracao

## API Atual

### `GET /api/state`

Retorna a tela atual da sessao.

Sem sessao:

```json
{"screen":"welcome"}
```

### `POST /api/start`

Cria a sessao do quiz com os dados do participante.

Campos esperados:

- `nome`
- `setor`

### `POST /api/answer`

Recebe a alternativa selecionada.

Campo esperado:

- `indice`

### `POST /api/next`

Avanca do feedback para a proxima pergunta ou para o resultado final.

### `POST /api/restart`

Encerra a sessao atual e volta para a identificacao.

## Regra de Negocio em Java

### `Jogador`

Guarda nome e setor do participante.

### `Pergunta`

Classe base da pergunta, com:

- situacao
- categoria
- alternativas
- resposta correta
- explicacao
- tempo limite
- pontuacao

### `PerguntaFacil`

Perguntas de menor complexidade.

### `PerguntaDificil`

Perguntas de maior complexidade.

### `Rodada`

Responsavel por:

- montar a rodada padrao
- embaralhar as perguntas
- controlar indice atual
- validar resposta
- produzir resultado da pergunta

### `Placar`

Responsavel por:

- pontuacao total
- quantidade de acertos
- classificacao final
- mensagem final

## Conteudo Atual do Quiz

Hoje a rodada padrao trabalha com 100 cenarios distribuidos em temas como:

- boleto falso
- pedido falso do chefe
- link suspeito
- senha compartilhada
- pendrive desconhecido
- engenharia social por telefone
- anexo suspeito
- fornecedor falso
- urgencia falsa para pagamento

## Interface Atual

### Tela inicial

- hero principal
- apresentacao do programa
- indicadores visuais de campanha

### Tela de identificacao

- painel lateral de jornada
- formulario de entrada
- validacao basica

### Tela do quiz

- cabecalho com progresso
- cenario em card
- pergunta e alternativas
- cronometro
- painel lateral de score

### Tela de feedback

- status da resposta
- explicacao educativa
- principios `Nunca`, `Desconfie`, `Faca sempre`
- botao de proxima pergunta com espera em cenarios de erro ou tempo esgotado

### Tela de resultado

- score final
- classificacao
- perfil de risco
- trilha recomendada

## Melhorias Ja Aplicadas

### Backend

- troca do servidor HTTP anterior por um servidor proprio com `ServerSocket`
- correcao de `Content-Type` para `html`, `css` e `js`
- suporte a porta por variavel `PORT`
- autoteste executavel por `--self-test`

### Frontend

- correcao preventiva de encoding com `fixEncoding()`
- barra fina para `Tempo esgotado`
- explicacao com botao travado por contagem regressiva
- progresso com `Pergunta X de 9`
- dicas com icones e bordas laterais
- painel lateral contextual
- reducão de flicker do cronometro
- responsividade para desktop, tablet e celular
- refinamento visual do quiz para toque e leitura

### Docker

- `Dockerfile` multi-stage
- `docker-compose.yml` com mapeamento de porta
- suporte a `APP_PORT` por `.env`

## Forma Recomendada de Execucao

O fluxo recomendado hoje e via Docker:

```powershell
docker compose up -d --build
```

Abrir:

```text
http://localhost:8080
```

Para parar:

```powershell
docker compose down
```

## Arquivos Mais Importantes Hoje

- [src/app/Main.java](/C:/Users/pedro/OneDrive/Desktop/progamas_resolvedores/QUIZ_JAVA/src/app/Main.java:1)
- [src/web/ServidorWeb.java](/C:/Users/pedro/OneDrive/Desktop/progamas_resolvedores/QUIZ_JAVA/src/web/ServidorWeb.java:1)
- [src/model/Rodada.java](/C:/Users/pedro/OneDrive/Desktop/progamas_resolvedores/QUIZ_JAVA/src/model/Rodada.java:1)
- [web/app.js](/C:/Users/pedro/OneDrive/Desktop/progamas_resolvedores/QUIZ_JAVA/web/app.js:1)
- [web/styles.css](/C:/Users/pedro/OneDrive/Desktop/progamas_resolvedores/QUIZ_JAVA/web/styles.css:1)
- [docker-compose.yml](/C:/Users/pedro/OneDrive/Desktop/progamas_resolvedores/QUIZ_JAVA/docker-compose.yml:1)
- [Dockerfile](/C:/Users/pedro/OneDrive/Desktop/progamas_resolvedores/QUIZ_JAVA/Dockerfile:1)

## Pontos de Atencao no Repositorio

Hoje existem sinais de historico anterior no workspace:

- pasta `build/` com classes compiladas
- pasta `out/` com artefatos antigos
- classes compiladas antigas de `view/` e `util/`
- arquivos `server_check.out.txt` e `server_check.err.txt`

Esses arquivos nao sao a fonte principal do sistema atual.

A fonte valida do projeto hoje esta em:

- `src/`
- `web/`
- arquivos Docker da raiz

## Limitacoes Atuais

- nao ha banco de dados
- sessoes nao persistem apos reinicio
- nao ha autenticacao real
- nao ha painel administrativo
- frontend ainda e monolitico em `app.js`
- serializacao JSON e manual

## Proximos Passos Recomendados

### Curto prazo

- limpar artefatos antigos do repositorio
- separar melhor o frontend em modulos
- revisar textos e acentuacao nativa no conteudo
- criar testes para endpoints web

### Medio prazo

- persistencia simples de sessoes ou resultados
- exportacao de resultado final
- dashboard com metricas agregadas
- melhoria de acessibilidade

### Longo prazo

- migrar frontend para componentes
- criar camada de API mais estruturada
- adicionar perfis de treinamento por departamento

## Resumo de Status

Estado atual do projeto:

- `funcional`
- `rodando em Docker`
- `backend Java OOP preservado`
- `frontend web adaptativo`
- `pronto para demonstracao`
- `ainda com espaco para limpeza estrutural`
