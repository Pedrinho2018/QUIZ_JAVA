package model;

/*
 * CONCEITO: HERANÇA  (extends)
 * PerguntaFacil HERDA de Pergunta — ela recebe automaticamente todos os atributos
 * e métodos da classe pai (situacao, categoria, getAlternativas, verificarResposta, etc.).
 * Não é necessário reescrever nada disso aqui.
 *
 * Relação: PerguntaFacil É UMA Pergunta (relação "é-um" = herança correta).
 */
public class PerguntaFacil extends Pergunta {

    /*
     * CONCEITO: CONSTRUTOR COM SUPER
     * O construtor de PerguntaFacil recebe os dados da pergunta, mas repassa
     * para o construtor da superclasse usando super(...).
     * Repare que o nível "Fácil" é fixo aqui — quem cria uma PerguntaFacil
     * não precisa informar o nível, pois ele já está definido nesta classe.
     */
    public PerguntaFacil(String situacao, String categoria, String[] alternativas, int indiceCorreto,
            String explicacao, int tempoLimiteSegundos) {
        super(situacao, categoria, "Fácil", alternativas, indiceCorreto, explicacao, tempoLimiteSegundos);
    }

    /*
     * CONCEITO: SOBRESCRITA DE MÉTODO (@Override) — POLIMORFISMO
     * A classe pai (Pergunta) declarou getPontuacao() como abstract, ou seja,
     * obrigou toda subclasse a implementar esse método.
     * Aqui PerguntaFacil define que uma pergunta fácil vale 10 pontos.
     *
     * @Override avisa o compilador: "estou implementando/substituindo um método da superclasse."
     * Se o nome estiver errado, o compilador acusa erro — evita bugs silenciosos.
     */
    @Override
    public int getPontuacao() {
        return 10;
    }
}
