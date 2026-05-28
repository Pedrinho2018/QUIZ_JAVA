package web;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import model.Jogador;
import model.Pergunta;
import model.Placar;
import model.Rodada;
import model.Rodada.ResultadoResposta;

public final class ServidorWeb {
    private static final String COOKIE_NOME = "empresa_segura_sid";
    private static final Path DIRETORIO_PUBLICO = Paths.get("web");
    private static final Map<String, SessaoQuiz> SESSOES = new ConcurrentHashMap<String, SessaoQuiz>();

    private ServidorWeb() {
    }

    public static void iniciar(int porta) {
        try (ServerSocket servidor = new ServerSocket(porta)) {
            String url = "http://127.0.0.1:" + porta;
            System.out.println("Servidor web iniciado em " + url);
            abrirNavegador(url);

            while (true) {
                Socket cliente = servidor.accept();
                Thread thread = new Thread(() -> processarCliente(cliente));
                thread.setDaemon(true);
                thread.start();
            }
        } catch (IOException erro) {
            throw new IllegalStateException("Nao foi possivel iniciar o servidor web.", erro);
        }
    }

    private static void processarCliente(Socket cliente) {
        try (Socket socket = cliente) {
            socket.setSoTimeout(5000);
            BufferedInputStream entrada = new BufferedInputStream(socket.getInputStream());
            BufferedOutputStream saida = new BufferedOutputStream(socket.getOutputStream());

            Requisicao requisicao = lerRequisicao(entrada);
            if (requisicao == null) {
                return;
            }

            Resposta resposta = tratarRequisicao(requisicao);
            enviarResposta(saida, resposta);
        } catch (SocketTimeoutException ignorado) {
        } catch (Exception erro) {
            erro.printStackTrace();
        }
    }

    private static Requisicao lerRequisicao(InputStream entrada) throws IOException {
        String linhaInicial = lerLinha(entrada);
        if (linhaInicial == null || linhaInicial.trim().isEmpty()) {
            return null;
        }

        String[] partes = linhaInicial.split(" ");
        if (partes.length < 2) {
            return null;
        }

        Map<String, String> cabecalhos = new HashMap<String, String>();
        String linhaCabecalho;
        while ((linhaCabecalho = lerLinha(entrada)) != null && !linhaCabecalho.isEmpty()) {
            int separador = linhaCabecalho.indexOf(':');
            if (separador > 0) {
                String chave = linhaCabecalho.substring(0, separador).trim().toLowerCase();
                String valor = linhaCabecalho.substring(separador + 1).trim();
                cabecalhos.put(chave, valor);
            }
        }

        int tamanhoCorpo = 0;
        if (cabecalhos.containsKey("content-length")) {
            tamanhoCorpo = Integer.parseInt(cabecalhos.get("content-length"));
        }

        byte[] corpo = new byte[tamanhoCorpo];
        int lidos = 0;
        while (lidos < tamanhoCorpo) {
            int atual = entrada.read(corpo, lidos, tamanhoCorpo - lidos);
            if (atual < 0) {
                break;
            }
            lidos += atual;
        }

        return new Requisicao(partes[0], partes[1], cabecalhos, corpo);
    }

    private static Resposta tratarRequisicao(Requisicao requisicao) throws IOException {
        try {
            if ("/api/start".equals(requisicao.caminho())) {
                return iniciarQuiz(requisicao);
            }
            if ("/api/state".equals(requisicao.caminho())) {
                return consultarEstado(requisicao);
            }
            if ("/api/answer".equals(requisicao.caminho())) {
                return responderPergunta(requisicao);
            }
            if ("/api/next".equals(requisicao.caminho())) {
                return avancarFluxo(requisicao);
            }
            if ("/api/restart".equals(requisicao.caminho())) {
                return reiniciarQuiz(requisicao);
            }
            return servirArquivoEstatico(requisicao);
        } catch (IllegalArgumentException erro) {
            return Resposta.json(400, "{\"erro\":\"" + escaparJson(erro.getMessage()) + "\"}");
        } catch (Exception erro) {
            erro.printStackTrace();
            return Resposta.json(500, "{\"erro\":\"Erro interno do servidor.\"}");
        }
    }

    private static Resposta iniciarQuiz(Requisicao requisicao) throws IOException {
        exigirMetodo(requisicao, "POST");
        Map<String, String> form = lerFormulario(requisicao.corpoTexto());
        String nome = valorObrigatorio(form, "nome");
        String setor = valorObrigatorio(form, "setor");

        SessaoQuiz sessao = new SessaoQuiz(new Jogador(nome, setor), Rodada.criarRodadaPadrao());
        String sessaoId = requisicao.cookie(COOKIE_NOME);
        if (sessaoId == null || sessaoId.trim().isEmpty()) {
            sessaoId = UUID.randomUUID().toString();
        }
        SESSOES.put(sessaoId, sessao);

        Resposta resposta = Resposta.json(200, montarEstadoJson("quiz", sessao));
        resposta.cabecalhos.put("Set-Cookie", COOKIE_NOME + "=" + sessaoId + "; Path=/; HttpOnly; SameSite=Lax");
        return resposta;
    }

    private static Resposta consultarEstado(Requisicao requisicao) {
        exigirMetodo(requisicao, "GET");
        SessaoQuiz sessao = obterSessao(requisicao);
        if (sessao == null) {
            return Resposta.json(200, "{\"screen\":\"welcome\"}");
        }
        return Resposta.json(200, montarEstadoJson(sessao.getTelaAtual(), sessao));
    }

    private static Resposta responderPergunta(Requisicao requisicao) throws IOException {
        exigirMetodo(requisicao, "POST");
        SessaoQuiz sessao = obterSessaoObrigatoria(requisicao);
        if (sessao.getUltimoResultado() != null) {
            return Resposta.json(200, montarEstadoJson("feedback", sessao));
        }

        Pergunta pergunta = sessao.getRodada().getPerguntaAtual();
        if (pergunta == null) {
            sessao.definirTelaAtual("result");
            return Resposta.json(200, montarEstadoJson("result", sessao));
        }

        long segundosDecorridos = Math.max(0L, (Instant.now().toEpochMilli() - sessao.getPerguntaIniciadaEm()) / 1000L);
        boolean tempoEsgotado = segundosDecorridos >= pergunta.getTempoLimiteSegundos();

        Map<String, String> form = lerFormulario(requisicao.corpoTexto());
        int indiceSelecionado = tempoEsgotado ? -1 : parseInt(form.get("indice"), -1);

        ResultadoResposta resultado = sessao.getRodada().responderAtual(indiceSelecionado, tempoEsgotado);
        sessao.setUltimoResultado(resultado);
        sessao.definirTelaAtual("feedback");
        return Resposta.json(200, montarEstadoJson("feedback", sessao));
    }

    private static Resposta avancarFluxo(Requisicao requisicao) {
        exigirMetodo(requisicao, "POST");
        SessaoQuiz sessao = obterSessaoObrigatoria(requisicao);

        if (sessao.getUltimoResultado() != null) {
            sessao.getRodada().avancarPergunta();
            sessao.setUltimoResultado(null);
        }

        if (sessao.getRodada().estaFinalizada()) {
            sessao.definirTelaAtual("result");
            return Resposta.json(200, montarEstadoJson("result", sessao));
        }

        sessao.registrarInicioPergunta();
        sessao.definirTelaAtual("quiz");
        return Resposta.json(200, montarEstadoJson("quiz", sessao));
    }

    private static Resposta reiniciarQuiz(Requisicao requisicao) {
        exigirMetodo(requisicao, "POST");
        String sessaoId = requisicao.cookie(COOKIE_NOME);
        if (sessaoId != null) {
            SESSOES.remove(sessaoId);
        }
        return Resposta.json(200, "{\"screen\":\"identify\"}");
    }

    private static Resposta servirArquivoEstatico(Requisicao requisicao) throws IOException {
        String caminhoBruto = requisicao.caminho();
        String caminhoRelativo = "/".equals(caminhoBruto) ? "index.html" : caminhoBruto.substring(1);
        Path arquivo = DIRETORIO_PUBLICO.resolve(caminhoRelativo).normalize();

        if (!arquivo.startsWith(DIRETORIO_PUBLICO) || !Files.exists(arquivo) || Files.isDirectory(arquivo)) {
            arquivo = DIRETORIO_PUBLICO.resolve("index.html");
        }

        byte[] bytes = Files.readAllBytes(arquivo);
        String tipo = descobrirMimeType(arquivo);
        return Resposta.arquivo(200, tipo, bytes);
    }

    private static String descobrirMimeType(Path arquivo) {
        String nome = arquivo.getFileName().toString().toLowerCase();
        if (nome.endsWith(".html")) {
            return "text/html";
        }
        if (nome.endsWith(".css")) {
            return "text/css";
        }
        if (nome.endsWith(".js")) {
            return "application/javascript";
        }
        String tipo = URLConnection.guessContentTypeFromName(nome);
        if (tipo != null) {
            return tipo;
        }
        return "application/octet-stream";
    }

    private static SessaoQuiz obterSessao(Requisicao requisicao) {
        String sessaoId = requisicao.cookie(COOKIE_NOME);
        return sessaoId == null ? null : SESSOES.get(sessaoId);
    }

    private static SessaoQuiz obterSessaoObrigatoria(Requisicao requisicao) {
        SessaoQuiz sessao = obterSessao(requisicao);
        if (sessao == null) {
            throw new IllegalArgumentException("Sessao do quiz nao encontrada. Reinicie o treinamento.");
        }
        return sessao;
    }

    private static void exigirMetodo(Requisicao requisicao, String metodoEsperado) {
        if (!metodoEsperado.equalsIgnoreCase(requisicao.metodo)) {
            throw new IllegalArgumentException("Metodo HTTP nao suportado.");
        }
    }

    private static Map<String, String> lerFormulario(String corpo) throws IOException {
        Map<String, String> valores = new HashMap<String, String>();
        if (corpo == null || corpo.trim().isEmpty()) {
            return valores;
        }
        String[] pares = corpo.split("&");
        for (String par : pares) {
            String[] partes = par.split("=", 2);
            String chave = URLDecoder.decode(partes[0], StandardCharsets.UTF_8.name());
            String valor = partes.length > 1 ? URLDecoder.decode(partes[1], StandardCharsets.UTF_8.name()) : "";
            valores.put(chave, valor);
        }
        return valores;
    }

    private static String valorObrigatorio(Map<String, String> form, String chave) {
        String valor = form.get(chave);
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o campo " + chave + ".");
        }
        return valor.trim();
    }

    private static int parseInt(String valor, int padrao) {
        if (valor == null) {
            return padrao;
        }
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException erro) {
            return padrao;
        }
    }

    private static void enviarResposta(OutputStream saida, Resposta resposta) throws IOException {
        StringBuilder cabecalho = new StringBuilder();
        cabecalho.append("HTTP/1.1 ").append(resposta.status).append(" ").append(statusTexto(resposta.status)).append("\r\n");
        cabecalho.append("Content-Type: ").append(resposta.contentType).append("\r\n");
        cabecalho.append("Content-Length: ").append(resposta.corpo.length).append("\r\n");
        cabecalho.append("Connection: close\r\n");
        for (Map.Entry<String, String> entry : resposta.cabecalhos.entrySet()) {
            cabecalho.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        cabecalho.append("\r\n");

        saida.write(cabecalho.toString().getBytes(StandardCharsets.UTF_8));
        saida.write(resposta.corpo);
        saida.flush();
    }

    private static String statusTexto(int status) {
        if (status == 200) {
            return "OK";
        }
        if (status == 400) {
            return "Bad Request";
        }
        if (status == 404) {
            return "Not Found";
        }
        return "Internal Server Error";
    }

    private static String lerLinha(InputStream entrada) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int atual;
        boolean houveConteudo = false;
        while ((atual = entrada.read()) != -1) {
            houveConteudo = true;
            if (atual == '\n') {
                break;
            }
            if (atual != '\r') {
                buffer.write(atual);
            }
        }
        if (!houveConteudo && buffer.size() == 0) {
            return null;
        }
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }

    private static String montarEstadoJson(String tela, SessaoQuiz sessao) {
        if ("welcome".equals(tela)) {
            return "{\"screen\":\"welcome\"}";
        }
        if ("identify".equals(tela)) {
            return "{\"screen\":\"identify\"}";
        }

        StringBuilder json = new StringBuilder();
        json.append("{");
        adicionarCampo(json, "screen", tela);

        if (sessao != null && sessao.getJogador() != null) {
            json.append(",\"player\":{");
            adicionarCampo(json, "name", sessao.getJogador().getNome());
            json.append(",");
            adicionarCampo(json, "department", sessao.getJogador().getSetor());
            json.append("}");
        }

        Rodada rodada = sessao == null ? null : sessao.getRodada();
        if (rodada != null) {
            json.append(",\"score\":").append(rodada.getPlacar().getPontuacao());
            json.append(",\"hits\":").append(rodada.getPlacar().getAcertos());
            json.append(",\"totalQuestions\":").append(rodada.getTotalPerguntas());
        }

        if ("quiz".equals(tela) && rodada != null) {
            Pergunta pergunta = rodada.getPerguntaAtual();
            if (pergunta != null) {
                long segundosDecorridos = Math.max(0L, (Instant.now().toEpochMilli() - sessao.getPerguntaIniciadaEm()) / 1000L);
                int tempoRestante = Math.max(0, pergunta.getTempoLimiteSegundos() - (int) segundosDecorridos);
                json.append(",\"question\":");
                json.append(montarPerguntaJson(pergunta, rodada.getIndiceAtual() + 1, rodada.getTotalPerguntas(), tempoRestante));
            }
        }

        if ("feedback".equals(tela) && sessao != null && sessao.getUltimoResultado() != null) {
            json.append(",\"feedback\":");
            json.append(montarFeedbackJson(sessao.getUltimoResultado()));
        }

        if ("result".equals(tela) && rodada != null) {
            json.append(",\"result\":");
            json.append(montarResultadoJson(sessao.getJogador(), rodada));
        }

        json.append("}");
        return json.toString();
    }

    private static String montarPerguntaJson(Pergunta pergunta, int numeroPergunta, int totalPerguntas, int tempoRestante) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        adicionarCampo(json, "category", pergunta.getCategoria());
        json.append(",");
        adicionarCampo(json, "level", pergunta.getNivel());
        json.append(",\"points\":").append(pergunta.getPontuacao());
        json.append(",\"number\":").append(numeroPergunta);
        json.append(",\"total\":").append(totalPerguntas);
        json.append(",\"timeLeft\":").append(tempoRestante);
        json.append(",");
        adicionarCampo(json, "situation", pergunta.getSituacao());
        json.append(",\"alternatives\":[");
        String[] alternativas = pergunta.getAlternativas();
        for (int i = 0; i < alternativas.length; i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append("\"").append(escaparJson(alternativas[i])).append("\"");
        }
        json.append("]}");
        return json.toString();
    }

    private static String montarFeedbackJson(ResultadoResposta resultado) {
        String status;
        String tone;
        String resumo;

        if (resultado.isTempoEsgotado()) {
            status = "Tempo esgotado";
            tone = "warning";
            resumo = "O tempo terminou antes da confirmacao da resposta.";
        } else if (resultado.isAcertou()) {
            status = "Resposta correta";
            tone = "success";
            resumo = "Voce somou " + resultado.getPergunta().getPontuacao() + " pontos nesta rodada.";
        } else {
            status = "Resposta incorreta";
            tone = "danger";
            resumo = "A alternativa correta era " + (char) ('A' + resultado.getPergunta().getIndiceCorreto()) + ".";
        }

        StringBuilder json = new StringBuilder();
        json.append("{");
        adicionarCampo(json, "status", status);
        json.append(",");
        adicionarCampo(json, "tone", tone);
        json.append(",");
        adicionarCampo(json, "summary", resumo);
        json.append(",");
        adicionarCampo(json, "explanation", resultado.getPergunta().getExplicacao());
        json.append(",\"currentScore\":").append(resultado.getPontuacaoAtual());
        json.append(",\"currentHits\":").append(resultado.getAcertosAtuais());
        json.append(",\"questionNumber\":").append(resultado.getNumeroPergunta());
        json.append(",\"correctOption\":\"").append((char) ('A' + resultado.getPergunta().getIndiceCorreto())).append("\"}");
        return json.toString();
    }

    private static String montarResultadoJson(Jogador jogador, Rodada rodada) {
        Placar placar = rodada.getPlacar();
        int pontuacaoMaxima = rodada.getPontuacaoMaxima();
        StringBuilder json = new StringBuilder();
        json.append("{");
        adicionarCampo(json, "name", jogador.getNome());
        json.append(",");
        adicionarCampo(json, "department", jogador.getSetor());
        json.append(",\"score\":").append(placar.getPontuacao());
        json.append(",\"hits\":").append(placar.getAcertos());
        json.append(",\"total\":").append(rodada.getTotalPerguntas());
        json.append(",\"maxScore\":").append(pontuacaoMaxima);
        json.append(",\"scorePercent\":").append(placar.getPercentual(pontuacaoMaxima));
        json.append(",");
        adicionarCampo(json, "classification", placar.getClassificacao(pontuacaoMaxima));
        json.append(",");
        adicionarCampo(json, "message", placar.getMensagemFinal(pontuacaoMaxima));
        json.append("}");
        return json.toString();
    }

    private static void adicionarCampo(StringBuilder json, String chave, String valor) {
        json.append("\"").append(escaparJson(chave)).append("\":\"").append(escaparJson(valor)).append("\"");
    }

    private static String escaparJson(String texto) {
        if (texto == null) {
            return "";
        }
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char caractere = texto.charAt(i);
            switch (caractere) {
                case '\\':
                    resultado.append("\\\\");
                    break;
                case '"':
                    resultado.append("\\\"");
                    break;
                case '\n':
                    resultado.append("\\n");
                    break;
                case '\r':
                    resultado.append("\\r");
                    break;
                case '\t':
                    resultado.append("\\t");
                    break;
                default:
                    if (caractere < 32) {
                        resultado.append(String.format("\\u%04x", (int) caractere));
                    } else {
                        resultado.append(caractere);
                    }
            }
        }
        return resultado.toString();
    }

    private static void abrirNavegador(String url) {
        if (!Desktop.isDesktopSupported()) {
            return;
        }
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception ignorado) {
        }
    }

    private static final class Requisicao {
        private final String metodo;
        private final String alvo;
        private final Map<String, String> cabecalhos;
        private final byte[] corpo;

        private Requisicao(String metodo, String alvo, Map<String, String> cabecalhos, byte[] corpo) {
            this.metodo = metodo;
            this.alvo = alvo;
            this.cabecalhos = cabecalhos;
            this.corpo = corpo;
        }

        private String caminho() {
            int query = alvo.indexOf('?');
            return query >= 0 ? alvo.substring(0, query) : alvo;
        }

        private String corpoTexto() {
            return new String(corpo, StandardCharsets.UTF_8);
        }

        private String cookie(String nome) {
            String linha = cabecalhos.get("cookie");
            if (linha == null) {
                return null;
            }
            String[] pares = linha.split(";");
            for (String par : pares) {
                String[] partes = par.trim().split("=", 2);
                if (partes.length == 2 && nome.equals(partes[0])) {
                    return partes[1];
                }
            }
            return null;
        }
    }

    private static final class Resposta {
        private final int status;
        private final String contentType;
        private final byte[] corpo;
        private final Map<String, String> cabecalhos;

        private Resposta(int status, String contentType, byte[] corpo) {
            this.status = status;
            this.contentType = contentType;
            this.corpo = corpo;
            this.cabecalhos = new HashMap<String, String>();
        }

        private static Resposta json(int status, String json) {
            Resposta resposta = new Resposta(status, "application/json; charset=UTF-8",
                    json.getBytes(StandardCharsets.UTF_8));
            resposta.cabecalhos.put("Cache-Control", "no-store");
            return resposta;
        }

        private static Resposta arquivo(int status, String tipo, byte[] bytes) {
            return new Resposta(status, tipo + "; charset=UTF-8", bytes);
        }
    }

    private static final class SessaoQuiz {
        private final Jogador jogador;
        private final Rodada rodada;
        private ResultadoResposta ultimoResultado;
        private long perguntaIniciadaEm;
        private String telaAtual;

        private SessaoQuiz(Jogador jogador, Rodada rodada) {
            this.jogador = jogador;
            this.rodada = rodada;
            this.telaAtual = "quiz";
            registrarInicioPergunta();
        }

        public Jogador getJogador() {
            return jogador;
        }

        public Rodada getRodada() {
            return rodada;
        }

        public ResultadoResposta getUltimoResultado() {
            return ultimoResultado;
        }

        public void setUltimoResultado(ResultadoResposta ultimoResultado) {
            this.ultimoResultado = ultimoResultado;
        }

        public long getPerguntaIniciadaEm() {
            return perguntaIniciadaEm;
        }

        public void registrarInicioPergunta() {
            perguntaIniciadaEm = Instant.now().toEpochMilli();
        }

        public String getTelaAtual() {
            return telaAtual;
        }

        public void definirTelaAtual(String telaAtual) {
            this.telaAtual = telaAtual;
        }
    }
}
