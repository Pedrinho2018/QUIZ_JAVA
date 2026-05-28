package app;

import model.Pergunta;
import model.PerguntaDificil;
import model.PerguntaFacil;
import model.Placar;
import model.Rodada;
import model.Rodada.ResultadoResposta;

public final class Autoteste {
    private Autoteste() {
    }

    public static void executar() {
        validarFluxoComPontuacaoMaxima();
        validarFluxoSemPontuacaoPorTimeout();
        validarFaixasDeClassificacao();
    }

    private static void validarFluxoComPontuacaoMaxima() {
        Rodada rodada = Rodada.criarRodadaPadrao();
        int pontuacaoMaxima = rodada.getPontuacaoMaxima();

        while (!rodada.estaFinalizada()) {
            Pergunta pergunta = rodada.getPerguntaAtual();
            ResultadoResposta resultado = rodada.responderAtual(pergunta.getIndiceCorreto(), false);
            if (resultado == null || !resultado.isAcertou()) {
                throw new IllegalStateException("Resposta correta deveria pontuar.");
            }
            rodada.avancarPergunta();
        }

        if (rodada.getPlacar().getPontuacao() != pontuacaoMaxima) {
            throw new IllegalStateException("Pontuacao maxima esperada era " + pontuacaoMaxima + " pontos.");
        }

        if (rodada.getTotalPerguntas() != 100) {
            throw new IllegalStateException("A rodada padrao deveria conter 100 perguntas.");
        }
    }

    private static void validarFluxoSemPontuacaoPorTimeout() {
        Rodada rodada = Rodada.criarRodadaPadrao();

        while (!rodada.estaFinalizada()) {
            ResultadoResposta resultado = rodada.responderAtual(-1, true);
            if (resultado == null || !resultado.isTempoEsgotado()) {
                throw new IllegalStateException("Timeout deveria gerar feedback de tempo esgotado.");
            }
            rodada.avancarPergunta();
        }

        if (rodada.getPlacar().getPontuacao() != 0) {
            throw new IllegalStateException("Timeout nao deveria somar pontos.");
        }
    }

    private static void validarFaixasDeClassificacao() {
        validarClassificacao(0, 1000, "Alerta Vermelho");
        validarClassificacao(400, 1000, "Em Treinamento");
        validarClassificacao(700, 1000, "Colaborador Atento");
        validarClassificacao(950, 1000, "Guardião Digital");
    }

    private static void validarClassificacao(int pontuacaoEsperada, int pontuacaoMaxima, String classificacaoEsperada) {
        Placar placar = new Placar();
        int restante = pontuacaoEsperada;

        while (restante >= 20) {
            placar.registrarAcerto(perguntaDificilDummy());
            restante -= 20;
        }

        while (restante >= 10) {
            placar.registrarAcerto(perguntaFacilDummy());
            restante -= 10;
        }

        if (!classificacaoEsperada.equals(placar.getClassificacao(pontuacaoMaxima))) {
            throw new IllegalStateException("Classificacao incorreta para " + pontuacaoEsperada + " pontos.");
        }
    }

    private static Pergunta perguntaFacilDummy() {
        return new PerguntaFacil("Dummy", "Dummy", new String[] { "A", "B", "C", "D" }, 0, "Dummy", 10);
    }

    private static Pergunta perguntaDificilDummy() {
        return new PerguntaDificil("Dummy", "Dummy", new String[] { "A", "B", "C", "D" }, 0, "Dummy", 10);
    }
}
