package model;

/*
 * CONCEITO: HERANÇA — segunda subclasse de Pergunta
 * PerguntaDificil segue exatamente a mesma estrutura de PerguntaFacil,
 * mas com nível "Difícil" e pontuação de 20 pontos.
 *
 * Isso demonstra o poder da herança: toda a lógica de verificação,
 * encapsulamento e atributos já está em Pergunta — aqui só definimos
 * o que é DIFERENTE neste tipo específico.
 */
public class PerguntaDificil extends Pergunta {

    // O nível "Difícil" é definido aqui e repassado para a superclasse via super().
    public PerguntaDificil(String situacao, String categoria, String[] alternativas, int indiceCorreto,
            String explicacao, int tempoLimiteSegundos) {
        super(situacao, categoria, "Difícil", alternativas, indiceCorreto, explicacao, tempoLimiteSegundos);
    }

    /*
     * CONCEITO: POLIMORFISMO em ação
     * Quando o código chama  pergunta.getPontuacao()  em um objeto do tipo PerguntaDificil,
     * o Java executa ESTE método automaticamente e retorna 20.
     * Se o objeto fosse PerguntaFacil, retornaria 10.
     * O chamador não precisa saber qual tipo é — o objeto sabe responder sozinho.
     */
    @Override
    public int getPontuacao() {
        return 20;
    }
}
