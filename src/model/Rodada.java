package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import repository.PerguntaRepository;

/*
 * CONCEITO: CLASSE DE CONTROLE DO DOMÍNIO
 * Rodada representa uma partida do quiz em andamento.
 * Ela concentra o fluxo do jogo: carrega perguntas, controla qual é a atual,
 * registra respostas, avança etapas e expõe o placar acumulado.
 *
 * Em termos de POO, Rodada faz composição com:
 *   - List<Pergunta> perguntas
 *   - Placar placar
 */
public class Rodada {

    // Repositório único compartilhado pela classe para carregar as perguntas ativas.
    private static final PerguntaRepository PERGUNTA_REPOSITORY = new PerguntaRepository();

    // Estado da rodada: conjunto de perguntas, placar e posição atual.
    private final List<Pergunta> perguntas;
    private final Placar placar;
    private int indiceAtual;

    // Cria uma rodada copiando a lista recebida para proteger a coleção original.
    public Rodada(List<Pergunta> perguntas) {
        this.perguntas = new ArrayList<Pergunta>(perguntas);
        this.placar = new Placar();
        this.indiceAtual = 0;
    }

    // Monta a rodada completa com todas as perguntas ativas embaralhadas.
    public static Rodada criarRodadaPadrao() {
        List<Pergunta> perguntas = carregarPerguntas();
        Collections.shuffle(perguntas);
        return new Rodada(perguntas);
    }

    // Sobrecargas para facilitar a criação da rodada com valores padrão.
    public static Rodada criarRodadaParaPerfil(String setor, String cargo) {
        return criarRodadaParaPerfil(setor, cargo, 20, "");
    }

    public static Rodada criarRodadaParaPerfil(String setor, String cargo, int quantidadePerguntas) {
        return criarRodadaParaPerfil(setor, cargo, quantidadePerguntas, "");
    }

    /*
     * Carrega as perguntas, aplica o filtro de dificuldade, embaralha
     * e limita a quantidade final usada na rodada.
     */
    public static Rodada criarRodadaParaPerfil(String setor, String cargo, int quantidadePerguntas, String dificuldade) {
        List<Pergunta> perguntas = carregarPerguntas();
        perguntas = filtrarPorDificuldade(perguntas, dificuldade);
        Collections.shuffle(perguntas);
        int limite = limitarQuantidade(quantidadePerguntas, perguntas.size());
        return new Rodada(perguntas.subList(0, limite));
    }

    // Se vier "Fácil" ou "Difícil", mantém apenas as perguntas daquele nível.
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

    // Garante uma quantidade válida de perguntas sem ultrapassar o total disponível.
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

    // Retorna a pergunta que está em jogo no momento.
    public Pergunta getPerguntaAtual() {
        if (estaFinalizada()) {
            return null;
        }
        return perguntas.get(indiceAtual);
    }

    /*
     * Processa a resposta da pergunta atual.
     * Se acertar sem timeout, soma pontos no placar.
     * O retorno encapsula tudo que a interface precisa mostrar no feedback.
     */
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

    // Move o cursor para a próxima pergunta, se ainda houver rodada ativa.
    public void avancarPergunta() {
        if (!estaFinalizada()) {
            indiceAtual++;
        }
    }

    // Encerra a rodada imediatamente.
    public void finalizar() {
        indiceAtual = perguntas.size();
    }

    // A rodada termina quando o índice atual passa do fim da lista.
    public boolean estaFinalizada() {
        return indiceAtual >= perguntas.size();
    }

    public int getIndiceAtual() {
        return indiceAtual;
    }

    public int getTotalPerguntas() {
        return perguntas.size();
    }

    // Soma a pontuação máxima possível da rodada inteira.
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

    /*
     * CLASSE INTERNA DE TRANSFERÊNCIA DE DADOS
     * ResultadoResposta agrupa o resultado de uma resposta para evitar
     * vários retornos soltos e simplificar a comunicação com a camada web.
     */
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

        // Getters usados pela camada web para montar o feedback ao jogador.
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
