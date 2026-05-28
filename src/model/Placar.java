package model;

/*
 * CONCEITO: CLASSE COM RESPONSABILIDADE ÚNICA
 * Placar cuida apenas de acumular pontos e calcular a classificação do jogador.
 * Isso segue o princípio de que cada classe deve ter uma responsabilidade bem definida.
 *
 * Note que Placar recebe objetos do tipo Pergunta no método registrarAcerto —
 * isso é POLIMORFISMO: não importa se é PerguntaFacil ou PerguntaDificil,
 * o Placar chama getPontuacao() e o objeto correto responde com o valor certo.
 */
public class Placar {

    // Atributos sem 'final' porque eles mudam durante o jogo (são incrementados).
    // Inicializados em 0 automaticamente pelo Java.
    private int pontuacao;
    private int acertos;

    /*
     * CONCEITO: POLIMORFISMO NO USO
     * 'pergunta' pode ser qualquer subtipo de Pergunta (Fácil ou Difícil).
     * pergunta.getPontuacao() executa o método da subclasse correta automaticamente.
     * O Placar não precisa saber qual tipo de pergunta é — apenas pede a pontuação.
     */
    public void registrarAcerto(Pergunta pergunta) {
        pontuacao += pergunta.getPontuacao(); // polimorfismo: 10 ou 20 dependendo do tipo
        acertos++;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public int getAcertos() {
        return acertos;
    }

    /*
     * CONCEITO: SOBRECARGA DE MÉTODO (overloading)
     * getClassificacao() e getClassificacao(int) têm o mesmo nome mas parâmetros diferentes.
     * Isso é sobrecarga (overloading) — diferente de sobrescrita (@Override).
     * A versão sem parâmetro usa 100 como pontuação máxima por padrão.
     */
    public String getClassificacao() {
        return getClassificacao(100);
    }

    public String getClassificacao(int pontuacaoMaxima) {
        int percentual = getPercentual(pontuacaoMaxima);
        // Regras de classificação baseadas no percentual de acerto:
        // 0-30%   → Alerta Vermelho
        // 31-60%  → Em Treinamento
        // 61-90%  → Colaborador Atento
        // 91-100% → Guardião Digital
        if (percentual <= 30) {
            return "Alerta Vermelho";
        }
        if (percentual <= 60) {
            return "Em Treinamento";
        }
        if (percentual <= 90) {
            return "Colaborador Atento";
        }
        return "Guardião Digital";
    }

    // Sobrecarga de getMensagemFinal — mesma lógica de getClassificacao acima.
    public String getMensagemFinal() {
        return getMensagemFinal(100);
    }

    public String getMensagemFinal(int pontuacaoMaxima) {
        String classificacao = getClassificacao(pontuacaoMaxima);
        // Compara Strings com .equals() — nunca com == para texto em Java.
        if ("Alerta Vermelho".equals(classificacao)) {
            return "Você precisa reforçar urgentemente os cuidados com fraudes digitais.";
        }
        if ("Em Treinamento".equals(classificacao)) {
            return "Você está evoluindo, mas ainda precisa validar melhor situações suspeitas.";
        }
        if ("Colaborador Atento".equals(classificacao)) {
            return "Você demonstra bons hábitos de segurança e reconhece a maioria dos golpes.";
        }
        return "Excelente desempenho. Você age como um verdadeiro guardião digital.";
    }

    /*
     * Calcula o percentual de aproveitamento do jogador.
     * A guarda (pontuacaoMaxima <= 0) evita divisão por zero.
     * Math.round arredonda o resultado para o inteiro mais próximo.
     */
    public int getPercentual(int pontuacaoMaxima) {
        if (pontuacaoMaxima <= 0) {
            return 0;
        }
        return (int) Math.round((pontuacao * 100.0) / pontuacaoMaxima);
    }
}
