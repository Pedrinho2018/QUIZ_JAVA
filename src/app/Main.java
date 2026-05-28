package app;

import web.ServidorWeb;

/*
 * CONCEITO: PONTO DE ENTRADA DA APLICAÇÃO
 * Main é a classe inicial do programa Java — o método main(String[] args) é
 * chamado automaticamente pela JVM quando o programa é executado.
 *
 * Esta classe tem duas responsabilidades simples:
 *   1. Se o argumento "--self-test" for passado, executa os testes internos.
 *   2. Caso contrário, sobe o servidor web na porta configurada.
 */
public class Main {

    // Constante da porta padrão. 'static final' = valor fixo pertencente à classe.
    private static final int PORTA_PADRAO = 8080;

    /*
     * main é o único método que a JVM conhece para iniciar o programa.
     * args são os argumentos passados na linha de comando (ex: --self-test).
     */
    public static void main(String[] args) {
        // Se o primeiro argumento for "--self-test", executa validações automatizadas.
        if (args.length > 0 && "--self-test".equals(args[0])) {
            Autoteste.executar();
            System.out.println("Autoteste concluido com sucesso.");
            return; // encerra o programa após o teste
        }

        // Caso normal: inicia o servidor web na porta resolvida.
        ServidorWeb.iniciar(resolverPorta());
    }

    /*
     * Lê a variável de ambiente PORT para permitir configuração sem recompilar.
     * Se não existir ou for inválida, usa a porta padrão (8080).
     * Isso é comum em ambientes de produção como Docker e nuvem.
     */
    private static int resolverPorta() {
        String portaAmbiente = System.getenv("PORT");
        if (portaAmbiente == null || portaAmbiente.trim().isEmpty()) {
            return PORTA_PADRAO;
        }

        try {
            return Integer.parseInt(portaAmbiente.trim());
        } catch (NumberFormatException erro) {
            // Se PORT contiver texto inválido (ex: "abc"), avisa e usa o padrão.
            System.out.println("PORT invalida. Usando porta padrao " + PORTA_PADRAO + ".");
            return PORTA_PADRAO;
        }
    }
}
