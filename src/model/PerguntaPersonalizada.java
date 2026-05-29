package model;

/*
 * CONCEITO: SUBCLASSE CONCRETA
 * PerguntaPersonalizada é a implementação real usada pelo sistema.
 * Ela herda toda a estrutura comum de Pergunta e só adiciona o que
 * varia aqui: a pontuação de cada questão carregada do banco/CSV.
 */
public class PerguntaPersonalizada extends Pergunta {

    // Cada pergunta pode valer uma quantidade diferente de pontos.
    private final int pontuacao;

    /*
     * O construtor repassa os dados comuns para a superclasse com super(...)
     * e guarda apenas a pontuação específica desta implementação.
     */
    public PerguntaPersonalizada(String situacao, String categoria, String nivel, String[] alternativas,
            int indiceCorreto, String explicacao, int tempoLimiteSegundos, int pontuacao) {
        super(situacao, categoria, nivel, alternativas, indiceCorreto, explicacao, tempoLimiteSegundos);
        this.pontuacao = pontuacao;
    }

    // Sobrescrita do método abstrato da superclasse.
    @Override
    public int getPontuacao() {
        return pontuacao;
    }
}
