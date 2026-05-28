package model;

/*
 * CONCEITO: CLASSE CONCRETA SIMPLES
 * Jogador representa um participante do quiz.
 * É a demonstração mais direta de "classe e objeto":
 *   - A CLASSE Jogador é o molde (define o que todo jogador tem)
 *   - Um OBJETO é criado com  new Jogador("Ana", "Financeiro")
 *
 * Esta classe só tem encapsulamento — sem herança, sem abstract.
 * Ideal para entender os fundamentos antes de avançar para herança.
 */
public class Jogador {

    // ENCAPSULAMENTO: atributos private e final
    // private → só Jogador pode ler/alterar diretamente
    // final   → o nome e o setor não mudam depois que o objeto é criado
    private final String nome;
    private final String setor;

    /*
     * CONCEITO: CONSTRUTOR
     * O construtor é chamado com 'new' e inicializa os atributos do objeto.
     * 'this.nome' refere-se ao atributo da instância; 'nome' (sem this) é o parâmetro.
     */
    public Jogador(String nome, String setor) {
        this.nome = nome;
        this.setor = setor;
    }

    // GETTERS: a única forma de ler os dados de fora da classe.
    // Não há setters — Jogador é imutável depois de criado.

    public String getNome() {
        return nome;
    }

    public String getSetor() {
        return setor;
    }
}
