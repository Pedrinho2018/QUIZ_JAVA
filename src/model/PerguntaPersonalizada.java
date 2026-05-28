package model;

public class PerguntaPersonalizada extends Pergunta {

    private final int pontuacao;

    public PerguntaPersonalizada(String situacao, String categoria, String nivel, String[] alternativas,
            int indiceCorreto, String explicacao, int tempoLimiteSegundos, int pontuacao) {
        super(situacao, categoria, nivel, alternativas, indiceCorreto, explicacao, tempoLimiteSegundos);
        this.pontuacao = pontuacao;
    }

    @Override
    public int getPontuacao() {
        return pontuacao;
    }
}
