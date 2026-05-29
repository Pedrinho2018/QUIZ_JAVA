package repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Pergunta;
import model.PerguntaPersonalizada;

/*
 * CONCEITO: REPOSITÓRIO
 * Esta classe isola a camada de dados do restante da aplicação.
 * Ela sabe onde as perguntas estão armazenadas, como inicializar o SQLite
 * e como transformar registros em objetos do domínio (Pergunta).
 *
 * Assim, Rodada e Placar trabalham com objetos Java e não com SQL direto.
 */
public final class PerguntaRepository {
    // Arquivos físicos usados pela aplicação.
    private static final Path ARQUIVO_CSV = Paths.get("data", "perguntas_quiz_ciberseguranca_300.csv");
    private static final Path ARQUIVO_SQLITE = Paths.get("data", "quiz.db");
    private static final String URL_PADRAO = "jdbc:sqlite:" + ARQUIVO_SQLITE.toString();

    // URL efetiva do banco, podendo vir de variável de ambiente.
    private final String urlBanco;

    public PerguntaRepository() {
        this(resolverUrlBanco());
    }

    // Ao criar o repositório, o sistema garante que o banco já esteja pronto para uso.
    public PerguntaRepository(String urlBanco) {
        this.urlBanco = urlBanco;
        carregarDriver();
        inicializarBanco();
    }

    /*
     * Lê todas as perguntas ativas do banco e converte cada linha
     * em um objeto PerguntaPersonalizada.
     */
    public List<Pergunta> listarAtivas() {
        List<Pergunta> perguntas = new ArrayList<Pergunta>();
        String sql = "SELECT categoria, tema, nivel, situacao, pergunta, "
                + "alternativa_a, alternativa_b, alternativa_c, alternativa_d, "
                + "resposta_correta, explicacao, pontuacao, tempo_limite_segundos "
                + "FROM perguntas WHERE ativa = 1";

        try (Connection conexao = DriverManager.getConnection(urlBanco);
                PreparedStatement comando = conexao.prepareStatement(sql);
                ResultSet resultado = comando.executeQuery()) {
            while (resultado.next()) {
                perguntas.add(new PerguntaPersonalizada(
                        montarSituacao(resultado.getString("situacao"), resultado.getString("pergunta")),
                        juntarCategoria(resultado.getString("categoria"), resultado.getString("tema")),
                        valorTexto(resultado.getString("nivel")),
                        new String[] {
                                valorTexto(resultado.getString("alternativa_a")),
                                valorTexto(resultado.getString("alternativa_b")),
                                valorTexto(resultado.getString("alternativa_c")),
                                valorTexto(resultado.getString("alternativa_d"))
                        },
                        indiceRespostaCorreta(resultado.getString("resposta_correta")),
                        valorTexto(resultado.getString("explicacao")),
                        resultado.getInt("tempo_limite_segundos"),
                        resultado.getInt("pontuacao")));
            }
        } catch (SQLException erro) {
            throw new IllegalStateException("Nao foi possivel carregar perguntas do SQLite.", erro);
        }

        if (perguntas.isEmpty()) {
            throw new IllegalStateException("O banco SQLite nao possui perguntas ativas.");
        }
        return perguntas;
    }

    // Permite trocar o banco via variável de ambiente sem recompilar o projeto.
    private static String resolverUrlBanco() {
        String urlAmbiente = System.getenv("QUIZ_DATABASE_URL");
        if (urlAmbiente != null && !urlAmbiente.trim().isEmpty()) {
            return urlAmbiente.trim();
        }
        return URL_PADRAO;
    }

    // Carrega o driver JDBC necessário para abrir conexões SQLite.
    private static void carregarDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException erro) {
            throw new IllegalStateException("Driver JDBC do SQLite nao encontrado no classpath.", erro);
        }
    }

    /*
     * Prepara o banco local.
     * Se a tabela ainda estiver vazia, importa os dados do CSV inicial.
     */
    private void inicializarBanco() {
        try {
            Path diretorio = ARQUIVO_SQLITE.getParent();
            if (diretorio != null) {
                Files.createDirectories(diretorio);
            }
        } catch (IOException erro) {
            throw new IllegalStateException("Nao foi possivel preparar o diretorio do banco SQLite.", erro);
        }

        try (Connection conexao = DriverManager.getConnection(urlBanco)) {
            criarTabela(conexao);
            if (contarPerguntas(conexao) == 0) {
                importarCsv(conexao);
            }
        } catch (SQLException erro) {
            throw new IllegalStateException("Nao foi possivel inicializar o banco SQLite.", erro);
        }
    }

    // Cria a tabela principal caso ela ainda não exista.
    private static void criarTabela(Connection conexao) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS perguntas ("
                + "id INTEGER PRIMARY KEY,"
                + "categoria TEXT NOT NULL,"
                + "tema TEXT,"
                + "nivel TEXT NOT NULL,"
                + "situacao TEXT NOT NULL,"
                + "pergunta TEXT NOT NULL,"
                + "alternativa_a TEXT NOT NULL,"
                + "alternativa_b TEXT NOT NULL,"
                + "alternativa_c TEXT NOT NULL,"
                + "alternativa_d TEXT NOT NULL,"
                + "resposta_correta TEXT NOT NULL,"
                + "explicacao TEXT NOT NULL,"
                + "sinal_de_risco TEXT,"
                + "nivel_alerta TEXT,"
                + "pontuacao INTEGER NOT NULL,"
                + "tempo_limite_segundos INTEGER NOT NULL,"
                + "ativa INTEGER NOT NULL DEFAULT 1"
                + ")";

        try (Statement comando = conexao.createStatement()) {
            comando.execute(sql);
        }
    }

    // Verifica se já existem perguntas no banco antes de importar o CSV.
    private static int contarPerguntas(Connection conexao) throws SQLException {
        try (Statement comando = conexao.createStatement();
                ResultSet resultado = comando.executeQuery("SELECT COUNT(*) FROM perguntas")) {
            return resultado.next() ? resultado.getInt(1) : 0;
        }
    }

    /*
     * Faz a carga inicial do CSV para o SQLite.
     * Isso permite que o sistema trabalhe depois com consultas SQL mais simples.
     */
    private static void importarCsv(Connection conexao) {
        if (!Files.exists(ARQUIVO_CSV)) {
            throw new IllegalStateException("Arquivo de perguntas nao encontrado: " + ARQUIVO_CSV);
        }

        String sql = "INSERT INTO perguntas ("
                + "id, categoria, tema, nivel, situacao, pergunta, "
                + "alternativa_a, alternativa_b, alternativa_c, alternativa_d, "
                + "resposta_correta, explicacao, sinal_de_risco, nivel_alerta, "
                + "pontuacao, tempo_limite_segundos, ativa"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            List<String> linhas = Files.readAllLines(ARQUIVO_CSV, StandardCharsets.UTF_8);
            conexao.setAutoCommit(false);

            try (PreparedStatement comando = conexao.prepareStatement(sql)) {
                for (int i = 1; i < linhas.size(); i++) {
                    String linha = linhas.get(i);
                    if (linha == null || linha.trim().isEmpty()) {
                        continue;
                    }

                    List<String> colunas = separarCsv(linha);
                    if (colunas.size() < 17) {
                        continue;
                    }

                    comando.setInt(1, parseIntSeguro(colunas.get(0), i));
                    comando.setString(2, valorTexto(colunas.get(1)));
                    comando.setString(3, valorTexto(colunas.get(2)));
                    comando.setString(4, valorTexto(colunas.get(3)));
                    comando.setString(5, valorTexto(colunas.get(4)));
                    comando.setString(6, valorTexto(colunas.get(5)));
                    comando.setString(7, valorTexto(colunas.get(6)));
                    comando.setString(8, valorTexto(colunas.get(7)));
                    comando.setString(9, valorTexto(colunas.get(8)));
                    comando.setString(10, valorTexto(colunas.get(9)));
                    comando.setString(11, valorTexto(colunas.get(10)));
                    comando.setString(12, valorTexto(colunas.get(11)));
                    comando.setString(13, valorTexto(colunas.get(12)));
                    comando.setString(14, valorTexto(colunas.get(13)));
                    comando.setInt(15, parseIntSeguro(colunas.get(14), 10));
                    comando.setInt(16, parseIntSeguro(colunas.get(15), 40));
                    comando.setInt(17, "sim".equalsIgnoreCase(valorTexto(colunas.get(16))) ? 1 : 0);
                    comando.addBatch();
                }
                comando.executeBatch();
            }

            conexao.commit();
        } catch (IOException erro) {
            rollbackSilencioso(conexao);
            throw new IllegalStateException("Nao foi possivel ler o CSV para popular o SQLite.", erro);
        } catch (SQLException erro) {
            rollbackSilencioso(conexao);
            throw new IllegalStateException("Nao foi possivel importar perguntas para o SQLite.", erro);
        } finally {
            restaurarAutoCommit(conexao);
        }
    }

    // Em caso de erro durante a importação, desfaz a transação.
    private static void rollbackSilencioso(Connection conexao) {
        try {
            conexao.rollback();
        } catch (SQLException ignorado) {
        }
    }

    // Restaura o comportamento padrão da conexão ao final da importação.
    private static void restaurarAutoCommit(Connection conexao) {
        try {
            conexao.setAutoCommit(true);
        } catch (SQLException ignorado) {
        }
    }

    /*
     * Parser simples de CSV com separador ';' e suporte a campos entre aspas.
     * Foi implementado manualmente para ler o formato do arquivo do projeto.
     */
    private static List<String> separarCsv(String linha) {
        List<String> colunas = new ArrayList<String>();
        StringBuilder atual = new StringBuilder();
        boolean entreAspas = false;

        for (int i = 0; i < linha.length(); i++) {
            char caractere = linha.charAt(i);
            if (caractere == '"') {
                if (entreAspas && i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                    atual.append('"');
                    i++;
                } else {
                    entreAspas = !entreAspas;
                }
            } else if (caractere == ';' && !entreAspas) {
                colunas.add(removerBom(atual.toString()));
                atual.setLength(0);
            } else {
                atual.append(caractere);
            }
        }

        colunas.add(removerBom(atual.toString()));
        return colunas;
    }

    // Remove BOM do UTF-8 e espaços extras.
    private static String removerBom(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\uFEFF", "").trim();
    }

    // Normaliza nulos para string vazia.
    private static String valorTexto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    // Junta categoria e tema em um único rótulo apresentado para o jogador.
    private static String juntarCategoria(String categoria, String tema) {
        if (tema == null || tema.trim().isEmpty()) {
            return valorTexto(categoria);
        }
        return valorTexto(categoria) + " - " + valorTexto(tema);
    }

    private static String montarSituacao(String situacao, String pergunta) {
        String situacaoLimpa = valorTexto(situacao);
        String perguntaLimpa = valorTexto(pergunta);
        if (situacaoLimpa.isEmpty()) {
            return perguntaLimpa;
        }
        if (perguntaLimpa.isEmpty()) {
            return situacaoLimpa;
        }
        return situacaoLimpa + " " + perguntaLimpa;
    }

    private static int indiceRespostaCorreta(String resposta) {
        if (resposta == null || resposta.trim().isEmpty()) {
            return 0;
        }
        char letra = Character.toUpperCase(resposta.trim().charAt(0));
        if (letra < 'A' || letra > 'D') {
            return 0;
        }
        return letra - 'A';
    }

    private static int parseIntSeguro(String valor, int padrao) {
        try {
            return Integer.parseInt(valorTexto(valor));
        } catch (Exception erro) {
            return padrao;
        }
    }
}
