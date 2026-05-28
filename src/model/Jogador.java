package model;

public class Jogador {
    private final String nome;
    private final String setor;

    public Jogador(String nome, String setor) {
        this.nome = nome;
        this.setor = setor;
    }

    public String getNome() {
        return nome;
    }

    public String getSetor() {
        return setor;
    }
}
