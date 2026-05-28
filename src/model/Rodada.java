package model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Rodada {

    private static final Path ARQUIVO_PERGUNTAS_CSV = Paths.get("data", "perguntas_quiz_ciberseguranca_300.csv");

    private final List<Pergunta> perguntas;
    private final Placar placar;
    private int indiceAtual;

    public Rodada(List<Pergunta> perguntas) {
        this.perguntas = new ArrayList<Pergunta>(perguntas);
        this.placar = new Placar();
        this.indiceAtual = 0;
    }

    public static Rodada criarRodadaPadrao() {
        List<Pergunta> perguntas = carregarPerguntasCsv();
        Collections.shuffle(perguntas);
        return new Rodada(perguntas);
    }

    public static Rodada criarRodadaParaPerfil(String setor, String cargo) {
        return criarRodadaParaPerfil(setor, cargo, 20, "");
    }

    public static Rodada criarRodadaParaPerfil(String setor, String cargo, int quantidadePerguntas) {
        return criarRodadaParaPerfil(setor, cargo, quantidadePerguntas, "");
    }

    public static Rodada criarRodadaParaPerfil(String setor, String cargo, int quantidadePerguntas, String dificuldade) {
        List<Pergunta> perguntas = carregarPerguntasCsv();
        perguntas = filtrarPorDificuldade(perguntas, dificuldade);
        Collections.shuffle(perguntas);
        int limite = limitarQuantidade(quantidadePerguntas, perguntas.size());
        return new Rodada(perguntas.subList(0, limite));
    }

    private static List<Pergunta> filtrarPorDificuldade(List<Pergunta> perguntas, String dificuldade) {
        if (dificuldade == null || dificuldade.trim().isEmpty() || "Todas".equalsIgnoreCase(dificuldade.trim())) {
            return perguntas;
        }

        List<Pergunta> filtradas = new ArrayList<Pergunta>();
        for (Pergunta pergunta : perguntas) {
            if (dificuldade.trim().equalsIgnoreCase(pergunta.getNivel())) {
                filtradas.add(pergunta);
            }
        }
        return filtradas;
    }

    private static int limitarQuantidade(int quantidade, int maximo) {
        if (maximo <= 0) {
            throw new IllegalStateException("Nenhuma pergunta ativa foi encontrada no CSV.");
        }
        if (quantidade <= 0) {
            return Math.min(10, maximo);
        }
        return Math.max(1, Math.min(quantidade, maximo));
    }

    private static List<Pergunta> carregarPerguntasCsv() {
        if (!Files.exists(ARQUIVO_PERGUNTAS_CSV)) {
            throw new IllegalStateException("Arquivo de perguntas nao encontrado: " + ARQUIVO_PERGUNTAS_CSV);
        }

        List<Pergunta> perguntas = new ArrayList<Pergunta>();
        try {
            List<String> linhas = Files.readAllLines(ARQUIVO_PERGUNTAS_CSV, StandardCharsets.UTF_8);
            for (int i = 1; i < linhas.size(); i++) {
                String linha = linhas.get(i);
                if (linha == null || linha.trim().isEmpty()) {
                    continue;
                }

                List<String> colunas = separarCsv(linha);
                if (colunas.size() < 17 || !"sim".equalsIgnoreCase(colunas.get(16).trim())) {
                    continue;
                }

                perguntas.add(criarPerguntaCsv(colunas));
            }
        } catch (IOException erro) {
            throw new IllegalStateException("Nao foi possivel ler o arquivo de perguntas CSV.", erro);
        }

        if (perguntas.isEmpty()) {
            throw new IllegalStateException("O CSV nao possui perguntas ativas.");
        }
        return perguntas;
    }

    private static Pergunta criarPerguntaCsv(List<String> colunas) {
        String categoria = juntarCategoria(colunas.get(1), colunas.get(2));
        String nivel = colunas.get(3).trim();
        String situacao = colunas.get(4).trim() + " " + colunas.get(5).trim();
        String[] alternativas = new String[] {
                colunas.get(6).trim(),
                colunas.get(7).trim(),
                colunas.get(8).trim(),
                colunas.get(9).trim()
        };

        return new PerguntaPersonalizada(
                situacao,
                categoria,
                nivel,
                alternativas,
                indiceRespostaCorreta(colunas.get(10)),
                colunas.get(11).trim(),
                parseIntSeguro(colunas.get(15), 40),
                parseIntSeguro(colunas.get(14), 10));
    }

    private static List<String> separarCsv(String linha) {
        List<String> colunas = new ArrayList<String>();
        StringBuilder atual = new StringBuilder();
        boolean entreAspas = false;

        for (int i = 0; i < linha.length(); i++) {
            char caractere = linha.charAt(i);
            if (caractere == '"') {
                if (entreAspas && i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                    atual.append('"');
                    i++;
                } else {
                    entreAspas = !entreAspas;
                }
            } else if (caractere == ';' && !entreAspas) {
                colunas.add(atual.toString());
                atual.setLength(0);
            } else {
                atual.append(caractere);
            }
        }

        colunas.add(atual.toString());
        return colunas;
    }

    private static String juntarCategoria(String categoria, String tema) {
        if (tema == null || tema.trim().isEmpty()) {
            return categoria.trim();
        }
        return categoria.trim() + " - " + tema.trim();
    }

    private static int indiceRespostaCorreta(String resposta) {
        if (resposta == null || resposta.trim().isEmpty()) {
            return 0;
        }
        char letra = Character.toUpperCase(resposta.trim().charAt(0));
        if (letra < 'A' || letra > 'D') {
            return 0;
        }
        return letra - 'A';
    }

    private static int parseIntSeguro(String valor, int padrao) {
        try {
            return Integer.parseInt(valor.trim());
        } catch (Exception erro) {
            return padrao;
        }
    }

    public Pergunta getPerguntaAtual() {
        if (estaFinalizada()) {
            return null;
        }
        return perguntas.get(indiceAtual);
    }

    public ResultadoResposta responderAtual(int indiceSelecionado, boolean tempoEsgotado) {
        Pergunta pergunta = getPerguntaAtual();
        if (pergunta == null) {
            return null;
        }

        boolean acertou = !tempoEsgotado && pergunta.verificarResposta(indiceSelecionado);
        if (acertou) {
            placar.registrarAcerto(pergunta);
        }

        return new ResultadoResposta(
                pergunta,
                acertou,
                tempoEsgotado,
                indiceSelecionado,
                indiceAtual + 1,
                perguntas.size(),
                placar.getPontuacao(),
                placar.getAcertos());
    }

    public void avancarPergunta() {
        if (!estaFinalizada()) {
            indiceAtual++;
        }
    }

    public void finalizar() {
        indiceAtual = perguntas.size();
    }

    public boolean estaFinalizada() {
        return indiceAtual >= perguntas.size();
    }

    public int getIndiceAtual() {
        return indiceAtual;
    }

    public int getTotalPerguntas() {
        return perguntas.size();
    }

    public int getPontuacaoMaxima() {
        int total = 0;
        for (Pergunta pergunta : perguntas) {
            total += pergunta.getPontuacao();
        }
        return total;
    }

    public Placar getPlacar() {
        return placar;
    }

    public static class ResultadoResposta {
        private final Pergunta pergunta;
        private final boolean acertou;
        private final boolean tempoEsgotado;
        private final int indiceSelecionado;
        private final int numeroPergunta;
        private final int totalPerguntas;
        private final int pontuacaoAtual;
        private final int acertosAtuais;

        public ResultadoResposta(Pergunta pergunta, boolean acertou, boolean tempoEsgotado, int indiceSelecionado,
                int numeroPergunta, int totalPerguntas, int pontuacaoAtual, int acertosAtuais) {
            this.pergunta = pergunta;
            this.acertou = acertou;
            this.tempoEsgotado = tempoEsgotado;
            this.indiceSelecionado = indiceSelecionado;
            this.numeroPergunta = numeroPergunta;
            this.totalPerguntas = totalPerguntas;
            this.pontuacaoAtual = pontuacaoAtual;
            this.acertosAtuais = acertosAtuais;
        }

        public Pergunta getPergunta() { return pergunta; }
        public boolean isAcertou() { return acertou; }
        public boolean isTempoEsgotado() { return tempoEsgotado; }
        public int getIndiceSelecionado() { return indiceSelecionado; }
        public int getNumeroPergunta() { return numeroPergunta; }
        public int getTotalPerguntas() { return totalPerguntas; }
        public int getPontuacaoAtual() { return pontuacaoAtual; }
        public int getAcertosAtuais() { return acertosAtuais; }
    }
}
