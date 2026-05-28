package model;

public class PerguntaDificil extends Pergunta {
    public PerguntaDificil(String situacao, String categoria, String[] alternativas, int indiceCorreto,
            String explicacao, int tempoLimiteSegundos) {
        super(situacao, categoria, "Difícil", alternativas, indiceCorreto, explicacao, tempoLimiteSegundos);
    }

    @Override
    public int getPontuacao() {
        return 20;
    }
}
