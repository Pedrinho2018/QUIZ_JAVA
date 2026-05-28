package model;

public class Placar {
    private int pontuacao;
    private int acertos;

    public void registrarAcerto(Pergunta pergunta) {
        pontuacao += pergunta.getPontuacao();
        acertos++;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public int getAcertos() {
        return acertos;
    }

    public String getClassificacao() {
        return getClassificacao(100);
    }

    public String getClassificacao(int pontuacaoMaxima) {
        int percentual = getPercentual(pontuacaoMaxima);
        // As decisões abaixo implementam exatamente a regra de classificação pedida no enunciado.
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

    public String getMensagemFinal() {
        return getMensagemFinal(100);
    }

    public String getMensagemFinal(int pontuacaoMaxima) {
        String classificacao = getClassificacao(pontuacaoMaxima);
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

    public int getPercentual(int pontuacaoMaxima) {
        if (pontuacaoMaxima <= 0) {
            return 0;
        }
        return (int) Math.round((pontuacao * 100.0) / pontuacaoMaxima);
    }
}
