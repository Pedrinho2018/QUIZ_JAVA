package model;

public class PerguntaFacil extends Pergunta {
    public PerguntaFacil(String situacao, String categoria, String[] alternativas, int indiceCorreto,
            String explicacao, int tempoLimiteSegundos) {
        super(situacao, categoria, "Fácil", alternativas, indiceCorreto, explicacao, tempoLimiteSegundos);
    }

    @Override
    public int getPontuacao() {
        return 10;
    }
}
