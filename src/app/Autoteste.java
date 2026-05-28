package app;

import model.Pergunta;
import model.PerguntaPersonalizada;
import model.Placar;
import model.Rodada;
import model.Rodada.ResultadoResposta;

/*
 * CONCEITO: CLASSE UTILITÁRIA / CLASSE DE TESTE
 * Autoteste valida que as regras do jogo funcionam corretamente sem precisar
 * abrir o navegador ou jogar manualmente.
 *
 * 'final' na declaração da classe impede que ela seja herdada — ela não foi
 * projetada para ser estendida.
 *
 * O construtor privado impede que alguém crie um objeto Autoteste com 'new'.
 * Todos os métodos são 'static', então a classe é usada apenas como conjunto
 * de funções agrupadas — não como objetos.
 */
public final class Autoteste {

    // Construtor privado: ninguém pode fazer  new Autoteste()
    private Autoteste() {
    }

    /*
     * Ponto de entrada do autoteste — chamado por Main quando recebe "--self-test".
     * Executa três validações em sequência; se qualquer uma falhar, lança exceção.
     */
    public static void executar() {
        validarFluxoComPontuacaoMaxima();      // garante que acertar tudo soma a pontuação total
        validarFluxoSemPontuacaoPorTimeout();  // garante que timeout não pontua
        validarRodadaPorPerfil();              // garante que o quiz web fica menor e personalizado
        validarFaixasDeClassificacao();        // garante que as faixas de % estão corretas
    }

    /*
     * Simula uma rodada onde o jogador acerta TODAS as perguntas.
     * Verifica se a pontuação final é igual à pontuação máxima possível
     * e se a rodada carrega as 300 perguntas ativas do CSV.
     *
     * Demonstra uso de: polimorfismo (getPerguntaAtual retorna Pergunta),
     * classe interna (ResultadoResposta) e encapsulamento (getPlacar, getPontuacao).
     */
    private static void validarFluxoComPontuacaoMaxima() {
        Rodada rodada = Rodada.criarRodadaPadrao();
        int pontuacaoMaxima = rodada.getPontuacaoMaxima();

        while (!rodada.estaFinalizada()) {
            Pergunta pergunta = rodada.getPerguntaAtual();
            // Responde com o índice correto e sem timeout → deve pontuar sempre.
            ResultadoResposta resultado = rodada.responderAtual(pergunta.getIndiceCorreto(), false);
            if (resultado == null || !resultado.isAcertou()) {
                throw new IllegalStateException("Resposta correta deveria pontuar.");
            }
            rodada.avancarPergunta();
        }

        if (rodada.getPlacar().getPontuacao() != pontuacaoMaxima) {
            throw new IllegalStateException("Pontuacao maxima esperada era " + pontuacaoMaxima + " pontos.");
        }

        if (rodada.getTotalPerguntas() != 300) {
            throw new IllegalStateException("A rodada padrao deveria conter 300 perguntas do CSV.");
        }
    }

    /*
     * Simula uma rodada onde TODAS as perguntas expiram (timeout).
     * Verifica que nenhum ponto é somado — timeout não deve pontuar.
     */
    private static void validarFluxoSemPontuacaoPorTimeout() {
        Rodada rodada = Rodada.criarRodadaPadrao();

        while (!rodada.estaFinalizada()) {
            // -1 como índice e true como timeout → simula tempo esgotado.
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

    private static void validarRodadaPorPerfil() {
        Rodada rodada = Rodada.criarRodadaParaPerfil("Financeiro", "Gerente");

        if (rodada.getTotalPerguntas() != 20) {
            throw new IllegalStateException("A rodada por perfil deveria conter 20 perguntas.");
        }

        Pergunta pergunta = rodada.getPerguntaAtual();
        if (pergunta == null) {
            throw new IllegalStateException("A rodada por perfil deveria iniciar com uma pergunta.");
        }

        Rodada rodadaCurta = Rodada.criarRodadaParaPerfil("Financeiro", "Gerente", 5);
        if (rodadaCurta.getTotalPerguntas() != 5) {
            throw new IllegalStateException("A rodada curta deveria respeitar a quantidade escolhida.");
        }

        Rodada rodadaDificil = Rodada.criarRodadaParaPerfil("Financeiro", "Gerente", 5, "Difícil");
        while (!rodadaDificil.estaFinalizada()) {
            if (!"Difícil".equals(rodadaDificil.getPerguntaAtual().getNivel())) {
                throw new IllegalStateException("Filtro de dificuldade deveria retornar apenas perguntas dificeis.");
            }
            rodadaDificil.avancarPergunta();
        }
    }

    /*
     * Verifica as quatro faixas de classificação do Placar.
     * Cria um Placar com pontuação controlada e confere se a classificação é a esperada.
     */
    private static void validarFaixasDeClassificacao() {
        // Cada chamada testa uma faixa: (pontuacao, maximo, classificacaoEsperada)
        validarClassificacao(0,   1000, "Alerta Vermelho");    // 0%
        validarClassificacao(400, 1000, "Em Treinamento");     // 40%
        validarClassificacao(700, 1000, "Colaborador Atento"); // 70%
        validarClassificacao(950, 1000, "Guardião Digital");   // 95%
    }

    /*
     * Cria um Placar com exatamente 'pontuacaoEsperada' pontos usando perguntas dummy,
     * depois confere se a classificação calculada bate com 'classificacaoEsperada'.
     *
     * Usa perguntas dummy com pontuação configurável apenas para exercitar o Placar.
     *
     * CONCEITO: polimorfismo — registrarAcerto(Pergunta) usa a pontuação da pergunta
     * sem depender de como ela foi criada.
     */
    private static void validarClassificacao(int pontuacaoEsperada, int pontuacaoMaxima, String classificacaoEsperada) {
        Placar placar = new Placar();
        int restante = pontuacaoEsperada;

        while (restante >= 20) {
            placar.registrarAcerto(perguntaDummy(20));
            restante -= 20;
        }

        while (restante >= 10) {
            placar.registrarAcerto(perguntaDummy(10));
            restante -= 10;
        }

        if (!classificacaoEsperada.equals(placar.getClassificacao(pontuacaoMaxima))) {
            throw new IllegalStateException("Classificacao incorreta para " + pontuacaoEsperada + " pontos.");
        }
    }

    /*
     * Métodos auxiliares que criam perguntas com dados fictícios ("dummy")
     * só para que o Placar possa registrar acertos durante o teste.
     * O conteúdo da pergunta não importa aqui — apenas a pontuação importa.
     */
    private static Pergunta perguntaDummy(int pontos) {
        return new PerguntaPersonalizada("Dummy", "Dummy", "Teste", new String[] { "A", "B", "C", "D" },
                0, "Dummy", 10, pontos);
    }
}
