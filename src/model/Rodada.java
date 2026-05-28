package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import repository.PerguntaRepository;

public class Rodada {

    private static final PerguntaRepository PERGUNTA_REPOSITORY = new PerguntaRepository();

    private final List<Pergunta> perguntas;
    private final Placar placar;
    private int indiceAtual;

    public Rodada(List<Pergunta> perguntas) {
        this.perguntas = new ArrayList<Pergunta>(perguntas);
        this.placar = new Placar();
        this.indiceAtual = 0;
    }

    public static Rodada criarRodadaPadrao() {
        List<Pergunta> perguntas = carregarPerguntas();
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
        List<Pergunta> perguntas = carregarPerguntas();
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

    private static List<Pergunta> carregarPerguntas() {
        return PERGUNTA_REPOSITORY.listarAtivas();
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
