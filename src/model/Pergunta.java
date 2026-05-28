package model;

public abstract class Pergunta {
    private final String situacao;
    private final String categoria;
    private final String nivel;
    private final String[] alternativas;
    private final int indiceCorreto;
    private final String explicacao;
    private final int tempoLimiteSegundos;

    protected Pergunta(String situacao, String categoria, String nivel, String[] alternativas,
            int indiceCorreto, String explicacao, int tempoLimiteSegundos) {
        this.situacao = situacao;
        this.categoria = categoria;
        this.nivel = nivel;
        this.alternativas = alternativas.clone();
        this.indiceCorreto = indiceCorreto;
        this.explicacao = explicacao;
        this.tempoLimiteSegundos = tempoLimiteSegundos;
    }

    public String getSituacao() {
        return situacao;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getNivel() {
        return nivel;
    }

    public String[] getAlternativas() {
        return alternativas.clone();
    }

    public int getIndiceCorreto() {
        return indiceCorreto;
    }

    public String getExplicacao() {
        return explicacao;
    }

    public int getTempoLimiteSegundos() {
        return tempoLimiteSegundos;
    }

    public boolean verificarResposta(int indiceResposta) {
        return indiceResposta == indiceCorreto;
    }

    // Cada subclasse define sua pontuação para deixar a herança visível na apresentação.
    public abstract int getPontuacao();
}
