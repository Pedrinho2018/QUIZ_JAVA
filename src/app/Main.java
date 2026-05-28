package app;

import web.ServidorWeb;

public class Main {
    private static final int PORTA_PADRAO = 8080;

    public static void main(String[] args) {
        if (args.length > 0 && "--self-test".equals(args[0])) {
            Autoteste.executar();
            System.out.println("Autoteste concluido com sucesso.");
            return;
        }

        ServidorWeb.iniciar(resolverPorta());
    }

    private static int resolverPorta() {
        String portaAmbiente = System.getenv("PORT");
        if (portaAmbiente == null || portaAmbiente.trim().isEmpty()) {
            return PORTA_PADRAO;
        }

        try {
            return Integer.parseInt(portaAmbiente.trim());
        } catch (NumberFormatException erro) {
            System.out.println("PORT invalida. Usando porta padrao " + PORTA_PADRAO + ".");
            return PORTA_PADRAO;
        }
    }
}
