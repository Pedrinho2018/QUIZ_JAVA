package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Rodada {
    private final List<Pergunta> perguntas;
    private final Placar placar;
    private int indiceAtual;

    public Rodada(List<Pergunta> perguntas) {
        this.perguntas = new ArrayList<>(perguntas);
        this.placar = new Placar();
        this.indiceAtual = 0;
    }

    public static Rodada criarRodadaPadrao() {
        List<Pergunta> perguntas = new ArrayList<Pergunta>();
        String[][] contextos = new String[][] {
                { "Financeiro", "pagamento de fornecedor", "nota fiscal", "diretoria financeira" },
                { "Recursos Humanos", "atualização cadastral", "cadastro funcional", "coordenação de RH" },
                { "Tecnologia", "acesso VPN", "credencial de acesso", "time de infraestrutura" },
                { "Comercial", "contrato com cliente", "proposta comercial", "gerência comercial" },
                { "Compras", "ordem de compra", "processo de cotação", "coordenação de compras" }
        };

        for (int i = 0; i < contextos.length; i++) {
            String departamento = contextos[i][0];
            String processo = contextos[i][1];
            String ativo = contextos[i][2];
            String gestor = contextos[i][3];

            perguntas.add(criarBoletoSuspeito(departamento, processo));
            perguntas.add(criarChefeImpersonado(departamento, gestor));
            perguntas.add(criarLinkEncurtado(departamento, ativo));
            perguntas.add(criarSenhaCompartilhada(departamento));
            perguntas.add(criarQrCodeEvento(departamento));
            perguntas.add(criarAnexoUrgente(departamento, ativo));
            perguntas.add(criarFalsoPortalLogin(departamento));
            perguntas.add(criarReembolsoUrgente(departamento));
            perguntas.add(criarComprovanteFalso(departamento, processo));
            perguntas.add(criarConviteReuniaoSuspeito(departamento));

            perguntas.add(criarPendriveRecepcao(departamento));
            perguntas.add(criarMfaPorTelefone(departamento));
            perguntas.add(criarZipPoliticaInterna(departamento));
            perguntas.add(criarTrocaContaBancaria(departamento, processo));
            perguntas.add(criarUrgenciaContrato(departamento));
            perguntas.add(criarSuporteAcessoRemoto(departamento));
            perguntas.add(criarPlanilhaAuditoria(departamento, ativo));
            perguntas.add(criarNuvemExterna(departamento, ativo));
            perguntas.add(criarPixFornecedor(departamento, processo));
            perguntas.add(criarBiometriaOuSelfie(departamento));
        }

        // A rodada final fica com 100 questões para ampliar a cobertura do treinamento.
        Collections.shuffle(perguntas);
        return new Rodada(perguntas);
    }

    private static Pergunta criarBoletoSuspeito(String departamento, String processo) {
        return new PerguntaFacil(
                "Você recebe um e-mail dizendo que o " + processo + " do setor de " + departamento
                        + " vence hoje e o boleto veio de um domínio parecido com o da empresa. O que fazer?",
                "Boleto falso",
                new String[] {
                        "Pagar rapidamente para evitar multa e atraso interno.",
                        "Encaminhar o boleto para colegas decidirem em grupo.",
                        "Confirmar com o responsável pelos canais internos antes de qualquer ação.",
                        "Abrir o arquivo no celular pessoal para testar sem risco."
                },
                2,
                "Golpistas usam urgência e domínios parecidos para induzir pagamento indevido. A validação fora do e-mail reduz o risco.",
                25);
    }

    private static Pergunta criarChefeImpersonado(String departamento, String gestor) {
        return new PerguntaFacil(
                "No WhatsApp, alguém com foto do " + gestor + " pede uma transferência imediata ligada ao setor de "
                        + departamento + ". Qual é a atitude mais segura?",
                "Pedido falso do chefe",
                new String[] {
                        "Transferir para demonstrar agilidade e resolver logo.",
                        "Ligar para o gestor ou confirmar com outro responsável autorizado.",
                        "Pedir a chave PIX e efetuar o envio sem registrar o caso.",
                        "Responder apenas com um ok e aguardar nova cobrança."
                },
                1,
                "Pedidos urgentes fora do processo oficial são sinais clássicos de golpe por impersonação.",
                25);
    }

    private static Pergunta criarLinkEncurtado(String departamento, String ativo) {
        return new PerguntaFacil(
                "Um parceiro do departamento de " + departamento + " envia um link encurtado para atualizar o "
                        + ativo + ". O melhor passo é:",
                "Link suspeito",
                new String[] {
                        "Clicar para verificar o destino do endereço.",
                        "Abrir o link só se o antivírus estiver ativo.",
                        "Responder pedindo outro link e acessar mesmo sem validação.",
                        "Validar a solicitação em canal conhecido antes de abrir qualquer endereço."
                },
                3,
                "Links encurtados escondem o destino real. A confirmação fora da mensagem evita páginas falsas.",
                20);
    }

    private static Pergunta criarSenhaCompartilhada(String departamento) {
        return new PerguntaFacil(
                "Um colega do setor de " + departamento + " pede sua senha para concluir uma tarefa mais rápido. Como agir?",
                "Senha compartilhada",
                new String[] {
                        "Compartilhar só desta vez por ser urgente.",
                        "Enviar a senha e trocá-la depois.",
                        "Negar o compartilhamento e orientar uso do acesso próprio ou suporte.",
                        "Anotar a senha em papel e deixar em local seguro."
                },
                2,
                "Senha é pessoal e intransferível. Compartilhar credenciais compromete rastreabilidade e segurança.",
                20);
    }

    private static Pergunta criarQrCodeEvento(String departamento) {
        return new PerguntaFacil(
                "Na recepção do setor de " + departamento
                        + ", há um cartaz com QR Code prometendo brinde se você atualizar seus dados agora. Qual é a melhor decisão?",
                "QR Code suspeito",
                new String[] {
                        "Escanear rapidamente porque a campanha parece oficial.",
                        "Pedir que um colega teste primeiro no celular dele.",
                        "Evitar o QR Code e confirmar a campanha pelos canais oficiais da empresa.",
                        "Fotografar o QR Code e acessar mais tarde em casa."
                },
                2,
                "QR Codes também podem direcionar para páginas falsas. A confirmação por canais internos continua sendo necessária.",
                20);
    }

    private static Pergunta criarAnexoUrgente(String departamento, String ativo) {
        return new PerguntaFacil(
                "Chega ao setor de " + departamento + " um arquivo chamado 'ajuste urgente de " + ativo
                        + ".pdf.exe'. Qual reação reduz mais o risco?",
                "Anexo disfarçado",
                new String[] {
                        "Abrir para conferir se o ajuste realmente é urgente.",
                        "Renomear o arquivo e tentar abrir de novo.",
                        "Enviar para colegas e perguntar se alguém já recebeu.",
                        "Não abrir e validar origem, contexto e formato do arquivo com o remetente oficial."
                },
                3,
                "Arquivos com dupla extensão são técnica comum para disfarçar executáveis maliciosos.",
                20);
    }

    private static Pergunta criarFalsoPortalLogin(String departamento) {
        return new PerguntaFacil(
                "Você recebe um aviso de que o portal do setor de " + departamento
                        + " precisa de novo login imediato por causa de manutenção. O que fazer primeiro?",
                "Portal falso",
                new String[] {
                        "Usar o link da mensagem para agilizar a regularização.",
                        "Digitar usuário e senha só para testar se a página é verdadeira.",
                        "Acessar o portal pelo endereço oficial já conhecido ou pelo favorito salvo.",
                        "Responder ao e-mail pedindo mais detalhes e acessar o mesmo link depois."
                },
                2,
                "Páginas falsas de login imitam visual e urgência. O acesso deve ocorrer apenas por endereço confiável.",
                20);
    }

    private static Pergunta criarReembolsoUrgente(String departamento) {
        return new PerguntaFacil(
                "Alguém pede ao time de " + departamento
                        + " um reembolso imediato com comprovante pouco legível e insiste que o gestor está em reunião. Qual é o passo mais seguro?",
                "Reembolso suspeito",
                new String[] {
                        "Aprovar por empatia e registrar depois.",
                        "Pedir uma foto melhor e efetuar o reembolso assim que chegar.",
                        "Confirmar identidade, documentação e fluxo de aprovação antes de qualquer pagamento.",
                        "Solicitar que a pessoa fale com outro colaborador menos ocupado."
                },
                2,
                "Golpes financeiros exploram pressa e exceções de processo. Reembolso também precisa seguir validação.",
                20);
    }

    private static Pergunta criarComprovanteFalso(String departamento, String processo) {
        return new PerguntaFacil(
                "Você recebe um comprovante de pagamento relacionado a " + processo + " no setor de " + departamento
                        + " e a outra parte pede liberação imediata. Qual a resposta correta?",
                "Comprovante falso",
                new String[] {
                        "Liberar o processo porque o comprovante foi enviado.",
                        "Pedir mais pressão para acelerar a compensação.",
                        "Aguardar a confirmação real no sistema ou banco antes de liberar qualquer etapa.",
                        "Encaminhar para o grupo do setor decidir por votação."
                },
                2,
                "Comprovantes podem ser editados. A confirmação deve ocorrer no sistema oficial antes da liberação.",
                20);
    }

    private static Pergunta criarConviteReuniaoSuspeito(String departamento) {
        return new PerguntaFacil(
                "Chega um convite de reunião para o setor de " + departamento
                        + " com link estranho e tema genérico de urgência. Qual atitude é mais adequada?",
                "Convite suspeito",
                new String[] {
                        "Entrar imediatamente para descobrir do que se trata.",
                        "Aceitar e repassar aos colegas para não perder o assunto.",
                        "Confirmar com o organizador por canal conhecido antes de entrar no link.",
                        "Abrir em modo anônimo para reduzir qualquer risco."
                },
                2,
                "Convites maliciosos podem levar a captura de credenciais ou instalação de malware.",
                18);
    }

    private static Pergunta criarPendriveRecepcao(String departamento) {
        return new PerguntaDificil(
                "Um pendrive sem identificação aparece próximo ao setor de " + departamento + ". Qual decisão reduz mais o risco?",
                "Pendrive desconhecido",
                new String[] {
                        "Conectar em uma máquina próxima para descobrir o dono.",
                        "Entregar ao TI ou segurança da informação sem conectar o dispositivo.",
                        "Levar para casa e testar em outro notebook.",
                        "Guardar para usar depois se ninguém reclamar."
                },
                1,
                "Mídias desconhecidas podem conter malware. O correto é isolar o dispositivo e acionar o time responsável.",
                20);
    }

    private static Pergunta criarMfaPorTelefone(String departamento) {
        return new PerguntaDificil(
                "Durante uma ligação, alguém se apresenta como suporte do setor de " + departamento
                        + " e pede seu código de autenticação multifator. O que fazer?",
                "Engenharia social",
                new String[] {
                        "Informar o código, porque a pessoa já sabe seu nome.",
                        "Passar o código só se a ligação vier de ramal interno.",
                        "Recusar, encerrar a ligação e procurar o suporte pelos canais oficiais.",
                        "Pedir que a pessoa envie mensagem por aplicativo para registrar o pedido."
                },
                2,
                "Códigos MFA nunca devem ser compartilhados. Suporte legítimo não solicita esse dado ao usuário.",
                20);
    }

    private static Pergunta criarZipPoliticaInterna(String departamento) {
        return new PerguntaDificil(
                "Chega um e-mail ao setor de " + departamento
                        + " com anexo 'nova_politica_interna.zip' vindo de remetente externo. Qual análise é mais adequada?",
                "Anexo suspeito",
                new String[] {
                        "Abrir o ZIP em modo leitura para verificar rapidamente.",
                        "Baixar o arquivo e pedir ajuda depois.",
                        "Verificar remetente, contexto e canal oficial antes de abrir qualquer anexo.",
                        "Mandar o anexo para todos perguntando se é seguro."
                },
                2,
                "Arquivos compactados são usados para ocultar malware. A checagem da origem deve vir antes da abertura.",
                18);
    }

    private static Pergunta criarTrocaContaBancaria(String departamento, String processo) {
        return new PerguntaDificil(
                "Um parceiro informa troca de conta bancária em um " + processo + " do setor de " + departamento
                        + " perto do vencimento. Qual procedimento segue a boa prática?",
                "Fornecedor falso",
                new String[] {
                        "Atualizar a conta porque o e-mail parece profissional.",
                        "Solicitar aprovação do primeiro colega disponível.",
                        "Confirmar a alteração por telefone oficial e seguir o fluxo interno de dupla checagem.",
                        "Pagar na conta nova só desta vez."
                },
                2,
                "Mudança de conta é um dos golpes mais comuns em empresas. A dupla validação com contato oficial é essencial.",
                18);
    }

    private static Pergunta criarUrgenciaContrato(String departamento) {
        return new PerguntaDificil(
                "Você recebe mensagem dizendo que, se um pagamento do setor de " + departamento
                        + " não sair em 10 minutos, a empresa perderá um contrato. O principal sinal é:",
                "Urgência falsa para pagamento",
                new String[] {
                        "Alta prioridade operacional totalmente normal.",
                        "Uso de pressão emocional para burlar o processo de conferência.",
                        "Sinal de que o pagamento já foi aprovado automaticamente.",
                        "Situação que permite pular validação por ser estratégica."
                },
                1,
                "Golpes usam senso artificial de urgência para impedir checagem. Processo seguro não deve ser ignorado sob pressão.",
                18);
    }

    private static Pergunta criarSuporteAcessoRemoto(String departamento) {
        return new PerguntaDificil(
                "Uma pessoa afirma ser do suporte e pede acesso remoto ao computador do setor de " + departamento
                        + " para 'corrigir falha crítica'. Qual é a melhor conduta?",
                "Acesso remoto indevido",
                new String[] {
                        "Permitir o acesso para resolver mais rápido.",
                        "Autorizar se a pessoa falar com segurança.",
                        "Negar até validar a identidade e abrir chamado em canal oficial.",
                        "Passar o acesso remoto, mas sem informar a senha."
                },
                2,
                "Ferramentas de acesso remoto podem ser exploradas por atacantes. Todo suporte precisa ser validado por fluxo oficial.",
                20);
    }

    private static Pergunta criarPlanilhaAuditoria(String departamento, String ativo) {
        return new PerguntaDificil(
                "Chega uma solicitação de 'auditoria externa' pedindo planilha com " + ativo + " do setor de "
                        + departamento + " em caráter sigiloso. O que deve prevalecer?",
                "Coleta indevida de dados",
                new String[] {
                        "Enviar porque auditoria costuma ser urgente.",
                        "Compartilhar parte do material para ganhar tempo.",
                        "Validar a demanda com gestor e segurança antes de enviar qualquer dado.",
                        "Mandar apenas se o remetente usar assinatura formal."
                },
                2,
                "Solicitações de dados sensíveis exigem validação formal. Sigilo declarado na mensagem não substitui processo.",
                18);
    }

    private static Pergunta criarNuvemExterna(String departamento, String ativo) {
        return new PerguntaDificil(
                "Um contato externo pede que o setor de " + departamento
                        + " envie o " + ativo + " por uma pasta compartilhada em nuvem fora do padrão da empresa. Como agir?",
                "Compartilhamento externo",
                new String[] {
                        "Enviar para não atrasar a negociação.",
                        "Compactar o arquivo com senha simples e compartilhar.",
                        "Usar apenas canais corporativos aprovados e validar a necessidade do compartilhamento.",
                        "Mandar via e-mail pessoal para evitar bloqueio da empresa."
                },
                2,
                "Dados corporativos devem trafegar em canais autorizados. Atalhos externos aumentam risco de vazamento.",
                18);
    }

    private static Pergunta criarPixFornecedor(String departamento, String processo) {
        return new PerguntaDificil(
                "Um suposto fornecedor envia nova chave PIX para um " + processo + " do setor de " + departamento
                        + " e pressiona por pagamento ainda hoje. Qual a resposta correta?",
                "PIX suspeito",
                new String[] {
                        "Pagar, porque a chave PIX agiliza a operação.",
                        "Pedir aprovação verbal de qualquer colega e concluir o pagamento.",
                        "Validar a mudança por contato oficial e seguir a política de dupla conferência.",
                        "Efetuar metade do valor como teste."
                },
                2,
                "Mudanças de chave PIX precisam da mesma validação rígida que mudança de conta bancária.",
                18);
    }

    private static Pergunta criarBiometriaOuSelfie(String departamento) {
        return new PerguntaDificil(
                "Você recebe pedido para gravar selfie e documento 'para liberar acesso urgente' ao setor de "
                        + departamento + ". O que deve ser considerado primeiro?",
                "Coleta indevida de identidade",
                new String[] {
                        "Atender rapidamente porque é só uma etapa de confirmação.",
                        "Mandar apenas a selfie para reduzir risco.",
                        "Validar legitimidade do pedido em canal oficial antes de compartilhar imagem ou documento.",
                        "Enviar o material com marca d'água e depois apagar."
                },
                2,
                "Documentos e biometria são dados sensíveis. O compartilhamento sem validação pode permitir fraudes de identidade.",
                20);
    }

    public Pergunta getPerguntaAtual() {
        if (estaFinalizada()) {
            return null;
        }
        return perguntas.get(indiceAtual);
    }

    public ResultadoResposta responderAtual(int indiceSelecionado, boolean tempoEsgotado) {
        Pergunta pergunta = getPerguntaAtual();
        if (pergunta == null) {
            return null;
        }

        // A pontuação só muda quando a alternativa está correta e o tempo não acabou.
        boolean acertou = !tempoEsgotado && pergunta.verificarResposta(indiceSelecionado);
        if (acertou) {
            placar.registrarAcerto(pergunta);
        }

        return new ResultadoResposta(
                pergunta,
                acertou,
                tempoEsgotado,
                indiceSelecionado,
                indiceAtual + 1,
                perguntas.size(),
                placar.getPontuacao(),
                placar.getAcertos());
    }

    public void avancarPergunta() {
        if (!estaFinalizada()) {
            indiceAtual++;
        }
    }

    public boolean estaFinalizada() {
        return indiceAtual >= perguntas.size();
    }

    public int getIndiceAtual() {
        return indiceAtual;
    }

    public int getTotalPerguntas() {
        return perguntas.size();
    }

    public int getPontuacaoMaxima() {
        int total = 0;
        for (Pergunta pergunta : perguntas) {
            total += pergunta.getPontuacao();
        }
        return total;
    }

    public Placar getPlacar() {
        return placar;
    }

    // Guarda o resultado de cada pergunta para a tela de feedback não depender da lógica da rodada.
    public static class ResultadoResposta {
        private final Pergunta pergunta;
        private final boolean acertou;
        private final boolean tempoEsgotado;
        private final int indiceSelecionado;
        private final int numeroPergunta;
        private final int totalPerguntas;
        private final int pontuacaoAtual;
        private final int acertosAtuais;

        public ResultadoResposta(Pergunta pergunta, boolean acertou, boolean tempoEsgotado, int indiceSelecionado,
                int numeroPergunta, int totalPerguntas, int pontuacaoAtual, int acertosAtuais) {
            this.pergunta = pergunta;
            this.acertou = acertou;
            this.tempoEsgotado = tempoEsgotado;
            this.indiceSelecionado = indiceSelecionado;
            this.numeroPergunta = numeroPergunta;
            this.totalPerguntas = totalPerguntas;
            this.pontuacaoAtual = pontuacaoAtual;
            this.acertosAtuais = acertosAtuais;
        }

        public Pergunta getPergunta() {
            return pergunta;
        }

        public boolean isAcertou() {
            return acertou;
        }

        public boolean isTempoEsgotado() {
            return tempoEsgotado;
        }

        public int getIndiceSelecionado() {
            return indiceSelecionado;
        }

        public int getNumeroPergunta() {
            return numeroPergunta;
        }

        public int getTotalPerguntas() {
            return totalPerguntas;
        }

        public int getPontuacaoAtual() {
            return pontuacaoAtual;
        }

        public int getAcertosAtuais() {
            return acertosAtuais;
        }
    }
}
