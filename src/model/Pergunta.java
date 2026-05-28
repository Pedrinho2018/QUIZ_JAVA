package model;

/*
 * CONCEITO: CLASSE ABSTRATA
 * Uma classe abstrata é um "molde incompleto" — ela define atributos e comportamentos
 * comuns a um grupo de objetos, mas não pode ser instanciada diretamente.
 * Ex: você não pode escrever  new Pergunta(...)  — só pode criar PerguntaFacil ou PerguntaDificil.
 *
 * Aqui Pergunta define tudo que qualquer pergunta do quiz precisa ter,
 * mas deixa getPontuacao() sem implementação para que cada subclasse decida o valor.
 */
public abstract class Pergunta {

    // CONCEITO: ENCAPSULAMENTO
    // Todos os atributos são 'private' — nenhuma outra classe acessa eles diretamente.
    // O acesso é feito apenas pelos métodos getters abaixo (getXxx).
    // 'final' significa que o valor não pode ser alterado depois de atribuído no construtor.
    private final String situacao;          // texto da pergunta exibida ao jogador
    private final String categoria;         // ex: "Boleto falso", "Senha compartilhada"
    private final String nivel;             // "Fácil" ou "Difícil" — definido pelas subclasses
    private final String[] alternativas;    // as 4 opções de resposta
    private final int indiceCorreto;        // posição (0-3) da alternativa correta
    private final String explicacao;        // feedback mostrado após responder
    private final int tempoLimiteSegundos;  // tempo que o jogador tem para responder

    /*
     * CONCEITO: CONSTRUTOR PROTEGIDO (protected)
     * 'protected' permite que as subclasses (PerguntaFacil, PerguntaDificil) chamem
     * este construtor usando super(...), mas impede que código externo o use diretamente.
     * Isso reforça que Pergunta só existe através de suas subclasses.
     */
    protected Pergunta(String situacao, String categoria, String nivel, String[] alternativas,
            int indiceCorreto, String explicacao, int tempoLimiteSegundos) {
        this.situacao = situacao;
        this.categoria = categoria;
        this.nivel = nivel;
        // .clone() cria uma cópia do array — quem criou o objeto não pode alterar
        // as alternativas depois passando a referência original.
        this.alternativas = alternativas.clone();
        this.indiceCorreto = indiceCorreto;
        this.explicacao = explicacao;
        this.tempoLimiteSegundos = tempoLimiteSegundos;
    }

    // CONCEITO: GETTERS (métodos de acesso)
    // Como os atributos são private, as outras classes usam esses métodos para ler os valores.
    // Retornar alternativas.clone() impede que quem chama altere o array interno da pergunta.

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
        // Retorna cópia para proteger o array original (encapsulamento defensivo).
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

    /*
     * CONCEITO: MÉTODO CONCRETO NA CLASSE ABSTRATA
     * Mesmo sendo abstrata, a classe pode ter métodos com implementação completa.
     * verificarResposta funciona igual para qualquer tipo de pergunta, então fica aqui.
     */
    public boolean verificarResposta(int indiceResposta) {
        return indiceResposta == indiceCorreto;
    }

    /*
     * CONCEITO: MÉTODO ABSTRATO
     * 'abstract' significa "toda subclasse DEVE implementar este método".
     * Aqui decidimos que cada tipo de pergunta define sua própria pontuação.
     * Isso é a base do POLIMORFISMO: o mesmo método (getPontuacao) se comporta
     * diferente dependendo de qual objeto está sendo usado (Fácil=10, Difícil=20).
     */
    public abstract int getPontuacao();
}
