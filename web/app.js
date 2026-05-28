const app = document.getElementById("app");

const state = {
  screen: "welcome",
  previousScreen: "",
  animateScreen: true,
  selectedAnswer: null,
  error: "",
  formError: "",
  payload: null,
  timerId: null,
  explanationDelayId: null,
  explanationCountdown: 0,
  selectedTenure: "1 - 3 anos"
};

const DEFAULT_TOTAL_QUESTIONS = 20;

function questionTimeLimit(question) {
  return Math.max(1, question.timeLimit || question.timeLeft || 1);
}

const journey = [
  { title: "Identificacao", desc: "Quem e voce na empresa" },
  { title: "Quiz interativo", desc: "20 cenarios por perfil" },
  { title: "Resultado", desc: "Perfil de risco pessoal" },
  { title: "Plano de acao", desc: "Recomendacoes praticas" }
];

function initials(name) {
  return (name || "")
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0].toUpperCase())
    .join("") || "ES";
}

function escapeHtml(value) {
  return String(value || "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function fixEncoding(value) {
  if (typeof value !== "string" || !value) {
    return value;
  }

  if (!/[ÃÂâ€™â€œâ€”]/.test(value)) {
    return value;
  }

  try {
    const bytes = new Uint8Array(Array.prototype.map.call(value, (char) => char.charCodeAt(0)));
    return new TextDecoder("utf-8").decode(bytes);
  } catch (error) {
    return value;
  }
}

function normalizePayload(value) {
  if (Array.isArray(value)) {
    return value.map(normalizePayload);
  }

  if (value && typeof value === "object") {
    const normalized = {};
    Object.keys(value).forEach((key) => {
      normalized[key] = normalizePayload(value[key]);
    });
    return normalized;
  }

  return fixEncoding(value);
}

function clamp(value, min, max) {
  return Math.max(min, Math.min(max, value));
}

function percent(value, total) {
  if (!total) {
    return 0;
  }
  return Math.round((value / total) * 100);
}

function shellClass() {
  return state.animateScreen ? "shell screen-enter" : "shell";
}

function showScreen(screen, animate) {
  state.previousScreen = state.screen;
  state.screen = screen;
  state.animateScreen = animate !== false && state.previousScreen !== state.screen;
}

async function request(url, options = {}) {
  const response = await fetch(url, {
    credentials: "same-origin",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
    },
    ...options
  });
  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.erro || "Falha inesperada.");
  }
  return data;
}

function stopTimer() {
  if (state.timerId) {
    window.clearInterval(state.timerId);
    state.timerId = null;
  }
}

function stopExplanationDelay() {
  if (state.explanationDelayId) {
    window.clearInterval(state.explanationDelayId);
    state.explanationDelayId = null;
  }
  state.explanationCountdown = 0;
}

function updateQuizTimerUI() {
  if (state.screen !== "quiz" || !state.payload || !state.payload.question) {
    return;
  }

  const question = state.payload.question;
  const timerValue = document.getElementById("timer-value");
  const timerShell = document.getElementById("timer-shell");
  const timerRing = document.getElementById("timer-ring");
  const warningBar = document.getElementById("time-warning");

  const timerPercent = clamp((question.timeLeft / questionTimeLimit(question)) * 100, 0, 100);
  const tone = question.timeLeft <= 5 ? "warning" : question.timeLeft <= 10 ? "caution" : "";

  if (timerValue) {
    timerValue.textContent = `${String(question.timeLeft).padStart(2, "0")}s`;
  }

  if (timerShell) {
    timerShell.classList.remove("warning", "caution");
    if (tone) {
      timerShell.classList.add(tone);
    }
  }

  if (timerRing) {
    timerRing.style.setProperty("--time", String(timerPercent));
  }

  if (warningBar) {
    if (question.timeLeft <= 10) {
      warningBar.classList.toggle("danger", question.timeLeft <= 5);
      warningBar.textContent = question.timeLeft <= 5
        ? "Ultimos segundos: confirme sua resposta agora."
        : "Tempo em contagem regressiva: revise e confirme com calma.";
      warningBar.hidden = false;
    } else {
      warningBar.hidden = true;
    }
  }
}

function updateExplanationCountdownUI() {
  if (state.screen !== "feedback" || !state.payload || !state.payload.feedback) {
    return;
  }

  const button = document.getElementById("next-question");
  if (!button) {
    return;
  }

  const isLastQuestion = state.payload.feedback.questionNumber >= state.payload.totalQuestions;
  const locked = state.explanationCountdown > 0;

  button.disabled = locked;
  button.classList.toggle("locked", locked);
  button.classList.toggle("ready", !locked);
  button.textContent = locked
    ? `Próxima pergunta (${state.explanationCountdown})`
    : (isLastQuestion ? "Ver resultado" : "Próxima pergunta →");
}

function startTimer() {
  stopTimer();
  if (state.screen !== "quiz" || !state.payload || !state.payload.question) {
    return;
  }
  state.timerId = window.setInterval(async () => {
    if (!state.payload || !state.payload.question) {
      stopTimer();
      return;
    }
    state.payload.question.timeLeft -= 1;
    if (state.payload.question.timeLeft <= 0) {
      stopTimer();
      await submitAnswer(true);
      return;
    }
    updateQuizTimerUI();
  }, 1000);
}

function applyPayload(data) {
  stopTimer();
  stopExplanationDelay();
  state.payload = normalizePayload(data);
  showScreen(state.payload.screen);
  state.selectedAnswer = null;
  state.error = "";
  state.formError = "";

  if (state.screen === "feedback" && state.payload.feedback) {
    const needsDelay = state.payload.feedback.tone === "warning" || state.payload.feedback.tone === "danger";
    if (needsDelay) {
      state.explanationCountdown = 5;
      state.explanationDelayId = window.setInterval(() => {
        state.explanationCountdown -= 1;
        if (state.explanationCountdown <= 0) {
          stopExplanationDelay();
        }
        updateExplanationCountdownUI();
      }, 1000);
    }
  }

  render();
  startTimer();
}

async function loadState() {
  try {
    applyPayload(await request("/api/state", { method: "GET", headers: {} }));
  } catch (error) {
    state.error = error.message;
    render();
  }
}

async function handleStart(event) {
  event.preventDefault();
  const formData = new FormData(event.currentTarget);
  const nome = String(formData.get("nome") || "").trim();
  const setor = String(formData.get("setor") || "").trim();
  const cargo = String(formData.get("cargo") || "").trim();

  if (!nome) {
    state.formError = "Informe seu nome para iniciar o treinamento.";
    render();
    return;
  }

  if (!setor) {
    state.formError = "Selecione um departamento para continuar.";
    render();
    return;
  }

  if (!cargo) {
    state.formError = "Selecione um cargo para personalizar as perguntas.";
    render();
    return;
  }

  const body = new URLSearchParams(formData);
  try {
    applyPayload(await request("/api/start", { method: "POST", body }));
  } catch (error) {
    state.error = error.message;
    render();
  }
}

async function submitAnswer(forceTimeout) {
  if (state.screen !== "quiz") {
    return;
  }
  const body = new URLSearchParams();
  body.set("indice", forceTimeout ? "-1" : String(state.selectedAnswer === null ? -1 : state.selectedAnswer));
  try {
    applyPayload(await request("/api/answer", { method: "POST", body }));
  } catch (error) {
    state.error = error.message;
    render();
  }
}

async function goNext() {
  try {
    applyPayload(await request("/api/next", { method: "POST", body: new URLSearchParams() }));
  } catch (error) {
    state.error = error.message;
    render();
  }
}

async function restart() {
  try {
    applyPayload(await request("/api/restart", { method: "POST", body: new URLSearchParams() }));
  } catch (error) {
    state.error = error.message;
    render();
  }
}

async function finishQuiz() {
  try {
    applyPayload(await request("/api/finish", { method: "POST", body: new URLSearchParams() }));
  } catch (error) {
    state.error = error.message;
    render();
  }
}

function topbar({ player, stepTag, showProgress, progress, progressLabel }) {
  const progressBlock = showProgress
    ? `
      <div class="tb-progress-wrap">
        <div class="tb-progress-copy">
          <strong>${escapeHtml(progressLabel || "")}</strong>
          <span>${progress}% concluído</span>
        </div>
        <div class="tb-progress">
          <div class="tb-progress-fill" style="width:${progress}%"></div>
        </div>
      </div>`
    : `<div class="tb-spacer"></div>`;

  const meta = player
    ? `
      <div class="tb-meta">
        <div class="tb-status"><span class="status-dot"></span> Sessao segura</div>
        <div class="tb-avatar">${escapeHtml(initials(player.name))}</div>
        <div class="tb-user">
          <strong>${escapeHtml(player.name)}</strong>
          <span>${escapeHtml(player.department)} · ${escapeHtml(player.role || "Perfil geral")}</span>
        </div>
      </div>`
    : `<div class="tb-meta"><div class="tb-status"><span class="status-dot"></span> Programa corporativo</div></div>`;

  return `
    <header class="topbar">
      <div class="brand">
        <div class="brand-mark"></div>
        <div class="brand-copy">
          <strong>Empresa Segura</strong>
          <span>v3.2 · Conscientizacao</span>
        </div>
      </div>
      ${stepTag ? `<div class="topbar-tag">${escapeHtml(stepTag)}</div>` : ""}
      ${progressBlock}
      ${meta}
    </header>`;
}

function welcomeScreen() {
  return `
    <div class="shell">
      ${topbar({ stepTag: "", showProgress: false })}
      <main class="page page-hero">
        <section class="hero-panel">
          <div class="hero-copy">
            <div>
              <div class="tag blue">Programa de Conscientizacao · 2026.Q2</div>
              <h1>Empresa<br>Segura.</h1>
              <div class="hero-chip">Funcionario cairia no golpe?</div>
              <p class="hero-lead">
                Um treinamento curto de simulacao de fraudes digitais. Em poucos minutos
                voce descobre como reagiria a phishing, engenharia social e golpes de pagamento.
              </p>
              <div class="hero-actions">
                <button class="btn btn-blue btn-lg" id="start-now">Iniciar treinamento</button>
                <button class="btn btn-ghost btn-lg" id="see-identify">Politica de acesso</button>
              </div>
              <div class="hero-stats">
                <div><strong>${DEFAULT_TOTAL_QUESTIONS}</strong><span>Cenarios reais</span></div>
                <div><strong>~35 min</strong><span>Duracao media</span></div>
                <div><strong>100%</strong><span>Sigiloso</span></div>
              </div>
            </div>
            <div class="hero-foot">Suas respostas sao confidenciais. Os resultados servem apenas para sua jornada de aprendizado.</div>
          </div>
          <div class="hero-visual">
            <div class="hero-grid"></div>
            <div class="hero-glow"></div>
            <div class="hero-center">
              <div class="hero-ring hero-ring-a"></div>
              <div class="hero-ring hero-ring-b"></div>
              <div class="hero-ring hero-ring-c"></div>
              <div class="hero-shield"></div>
            </div>
            <div class="float-card card-a">
              <span class="mini-badge red">Phishing detectado</span>
              <strong>Dominio falso</strong>
              rh-empressa.com.br
            </div>
            <div class="float-card card-b">
              <div class="mini-line">
                <span>Engenharia social</span>
                <span class="mini-badge yellow">medio</span>
              </div>
              <div class="mini-meter"><div style="width:64%"></div></div>
              <small>Score do time: 64/100</small>
            </div>
            <div class="float-card card-c">
              <span class="green-dot"></span>
              238 colaboradores concluiram este mes
            </div>
            <div class="hero-caption">Nucleo de Seguranca da Informacao</div>
          </div>
        </section>
      </main>
    </div>`;
}

function identifyScreen() {
  return `
    <div class="${shellClass()}">
      ${topbar({ stepTag: "Etapa 1 de 4", showProgress: false })}
      <main class="page">
        <section class="split-shell">
          <aside class="journey-panel">
            <div class="journey-grid"></div>
            <div class="journey-copy">
              <div class="eyebrow-dark">Sua jornada</div>
              <h2>Em 4 etapas voce descobre seu perfil de risco</h2>
              <p>Comece se identificando. Esses dados ficam restritos a sua trilha de aprendizado.</p>
              <div class="journey-steps">
                ${journey.map((item, index) => `
                  <div class="journey-step ${index === 0 ? "active" : ""}">
                    <div class="journey-bullet">${index + 1}</div>
                    <div>
                      <strong>${escapeHtml(item.title)}</strong>
                      <span>${escapeHtml(item.desc)}</span>
                    </div>
                  </div>`).join("")}
              </div>
              <div class="journey-note">
                <strong>Privacidade.</strong> Seu resultado e individual e nunca precisa de banco de dados para funcionar nesta apresentacao.
              </div>
            </div>
          </aside>
          <section class="identify-panel">
            <div class="eyebrow">Etapa 1 de 4</div>
            <h1>Antes de comecarmos, quem e voce?</h1>
            <p>Apenas para personalizar seu treinamento. Leva menos de 30 segundos.</p>
            <form class="card identify-card" id="identify-form">
              ${state.formError ? `<div class="inline-error">${escapeHtml(state.formError)}</div>` : ""}
              <div class="form-grid">
                <div class="field">
                  <label for="nome">Nome completo</label>
                  <input id="nome" name="nome" placeholder="Marina Castelo Branco" required>
                </div>
                <div class="field">
                  <label for="matricula">Matricula <span>(opcional)</span></label>
                  <input id="matricula" placeholder="000000">
                </div>
                <div class="field full">
                  <label for="email">E-mail corporativo</label>
                  <input id="email" name="email" type="email" placeholder="marina.castelo@empresa.com.br">
                </div>
                <div class="field">
                  <label for="setor">Departamento</label>
                  <select id="setor" name="setor">
                    <option>Financeiro</option>
                    <option>Comercial</option>
                    <option>Tecnologia</option>
                    <option>Recursos Humanos</option>
                    <option>Juridico</option>
                    <option>Compras</option>
                    <option>Operacoes</option>
                  </select>
                </div>
                <div class="field">
                  <label for="cargo">Cargo</label>
                  <select id="cargo" name="cargo">
                    <option>Analista Pleno</option>
                    <option>Coordenador</option>
                    <option>Especialista</option>
                    <option>Gerente</option>
                    <option>Estagiario</option>
                  </select>
                </div>
                <div class="field">
                  <label for="quantidade">Quantidade de perguntas</label>
                  <select id="quantidade" name="quantidade">
                    <option value="5">5 perguntas</option>
                    <option value="10" selected>10 perguntas</option>
                    <option value="15">15 perguntas</option>
                    <option value="20">20 perguntas</option>
                  </select>
                </div>
                <div class="field">
                  <label for="dificuldade">Dificuldade</label>
                  <select id="dificuldade" name="dificuldade">
                    <option value="Todas" selected>Todas</option>
                    <option value="Fácil">Fácil</option>
                    <option value="Médio">Médio</option>
                    <option value="Difícil">Difícil</option>
                  </select>
                </div>
                <div class="field full">
                  <label>Tempo de empresa</label>
                  <input type="hidden" id="tempo-empresa" name="tempoEmpresa" value="${escapeHtml(state.selectedTenure)}">
                  <div class="tenure-row">
                    ${["< 6 meses", "6 m - 1 ano", "1 - 3 anos", "3 - 5 anos", "5+ anos"].map((tenure) => `
                      <button
                        type="button"
                        class="tenure-chip ${state.selectedTenure === tenure ? "selected" : ""}"
                        data-tenure="${escapeHtml(tenure)}"
                      >${escapeHtml(tenure)}</button>
                    `).join("")}
                  </div>
                </div>
              </div>
              <div class="consent-box">
                <div class="consent-check">ok</div>
                <div>Estou ciente de que este e um <strong>treinamento simulado</strong> e concordo em participar da rodada educativa.</div>
              </div>
              <div class="identify-footer">
                <div class="hint">Pressione Enter para continuar.</div>
                <div class="action-row">
                  <button type="button" class="btn btn-ghost" id="back-welcome">Voltar</button>
                  <button type="submit" class="btn btn-blue btn-lg">Comecar o quiz</button>
                </div>
              </div>
            </form>
          </section>
        </section>
      </main>
    </div>`;
}

function scenarioTemplate(question) {
  const text = `${question.category} ${question.situation}`.toLowerCase();

  const makeScene = (channel, sender, meta, badge, highlight, footer) => ({
    channel,
    sender,
    meta,
    badge,
    subject: question.category,
    body: [
      question.situation,
      "Observe canal, urgencia, pedido de dados, permissao solicitada e quebra de processo antes de responder."
    ],
    highlight,
    footer
  });

  if (text.includes("whatsapp") || text.includes("aplicativo")) {
    return makeScene(
      "Mensagem instantanea",
      "Contato com foto conhecida",
      "fora do canal oficial · agora",
      "impersonacao",
      "Foto e nome conhecidos nao provam identidade.",
      "Sinal de risco: canal informal + urgencia"
    );
  }

  if (text.includes("sms")) {
    return makeScene(
      "SMS recebido",
      "Aviso automatico",
      "numero desconhecido · agora",
      "smishing",
      "Links por SMS devem ser tratados como suspeitos ate validacao.",
      "Sinal de risco: link curto + ameaca de bloqueio"
    );
  }

  if (text.includes("reuniao") || text.includes("convite")) {
    return makeScene(
      "Agenda corporativa",
      "Convite externo",
      "sem pauta clara · hoje",
      "reuniao falsa",
      "Convites tambem podem capturar credenciais ou instalar malware.",
      "Sinal de risco: link externo + urgencia"
    );
  }

  if (text.includes("ia ") || text.includes("generativa")) {
    return makeScene(
      "Ferramenta externa",
      "Assistente online",
      "fora do ambiente corporativo",
      "exposicao de dados",
      "Dados reais nao devem ser colados em ferramenta nao aprovada.",
      "Sinal de risco: dado sensivel em servico externo"
    );
  }

  if (text.includes("visitante") || text.includes("fotograf")) {
    return makeScene(
      "Atendimento presencial",
      "Visitante autorizado",
      "area interna · durante visita",
      "vazamento fisico",
      "Autorizacao de visita nao autoriza captura de telas ou documentos.",
      "Sinal de risco: foto de informacao interna"
    );
  }

  if (text.includes("portal") || text.includes("site") || text.includes("navegador")) {
    return makeScene(
      "Portal web",
      "Pagina parecida com a oficial",
      "link recebido · acesso externo",
      "site falso",
      "Endereco, certificado e origem precisam ser conferidos antes do login.",
      "Sinal de risco: pagina parecida + pedido de credencial"
    );
  }

  if (text.includes("contrato") || text.includes("assinatura")) {
    return makeScene(
      "Documento corporativo",
      "Parceiro de negocio",
      "prazo curto · revisao pendente",
      "documento alterado",
      "Pressa para assinar sem comparar versoes aumenta risco de fraude.",
      "Sinal de risco: mudanca discreta + prazo artificial"
    );
  }

  if (text.includes("terceiro") || text.includes("administrativa") || text.includes("privilegiado") || text.includes("permiss")) {
    return makeScene(
      "Solicitacao de acesso",
      "Prestador / suporte",
      "chamado incompleto · urgente",
      "acesso indevido",
      "Permissoes devem ter escopo, prazo, aprovacao e registro.",
      "Sinal de risco: acesso amplo sem controle"
    );
  }

  if (text.includes("senha") || text.includes("autentic") || text.includes("suporte")) {
    return makeScene(
      "Ligacao suspeita",
      "Suporte tecnico terceirizado",
      "ramal externo · agora",
      "engenharia social",
      "Suporte legitimo nao pede senha ou codigo MFA ao usuario final.",
      "Sinal de risco: pressao + pedido de dado sensivel"
    );
  }

  if (text.includes("pendrive") || text.includes("recepc")) {
    return makeScene(
      "Objeto encontrado",
      "Midia sem identificacao",
      "recepcao principal · hoje",
      "dispositivo fisico",
      "Conectar para descobrir o conteudo ja e um risco.",
      "Sinal de risco: engenharia por curiosidade"
    );
  }

  if (text.includes("pix") || text.includes("boleto") || text.includes("fornecedor") || text.includes("pagamento") || text.includes("conta banc")) {
    return makeScene(
      "Solicitacao financeira",
      "Fornecedor / diretoria",
      "urgente · vencimento hoje",
      "pagamento",
      "Mudanca de conta e pressao por pagamento sao combinacoes classicas de fraude.",
      "Sinal de risco: quebra de processo"
    );
  }

  return makeScene(
    "E-mail corporativo",
    "Remetente externo",
    "para voce · hoje",
    "phishing por e-mail",
    "Dominio, link, anexo e urgencia devem ser validados fora da mensagem.",
    "Sinal de risco: urgencia + pedido fora do processo"
  );
}

function quizCategories(currentCategory) {
  const categories = [
    "Phishing por e-mail",
    "Engenharia social",
    "Senhas e acesso",
    "Pagamentos e validacao"
  ];
  return categories.map((item) => {
    const active = currentCategory.toLowerCase().includes(item.split(" ")[0].toLowerCase()) || item === "Pagamentos e validacao" && currentCategory.toLowerCase().includes("pag");
    return `<span class="crumb ${active ? "active" : ""}">${escapeHtml(item)}</span>`;
  }).join("");
}

function renderScoreChips(total, hits, answered) {
  const visible = Math.min(total, 20);
  const resolved = Math.min(answered, visible);
  const successCount = Math.min(hits, resolved);

  return Array.from({ length: visible }, (_, index) => {
    if (index < successCount) {
      return `<div class="score-chip success" title="Resposta correta">✓</div>`;
    }
    if (index < resolved) {
      return `<div class="score-chip fail" title="Resposta incorreta ou tempo esgotado">✕</div>`;
    }
    return `<div class="score-chip empty"></div>`;
  }).join("");
}

function scoreSidebar(payload, options) {
  const points = payload.score || 0;
  const currentQuestion = options.currentQuestion || 1;
  const totalQuestions = payload.totalQuestions || payload.result?.total || 0;
  const showExtras = !!options.showExtras;
  const chips = renderScoreChips(totalQuestions, payload.hits || 0, options.answeredQuestions || 0);
  const visibleChips = Math.min(totalQuestions, 20);

  return `
    <aside class="quiz-side ${showExtras ? "show-extras" : "collapsed-extras"}">
      <div class="card side-card">
        <div class="eyebrow">Seu progresso</div>
        <div class="big-score">${points}<span> pts</span></div>
        ${points === 0 && currentQuestion > 1 ? `<p class="motivacional">Não desanime, você ainda pode recuperar pontos!</p>` : ""}
        <div class="score-track">${chips}</div>
        ${totalQuestions > visibleChips ? `<div class="score-caption">Resumo visual das ultimas ${visibleChips} posicoes da trilha.</div>` : ""}
        <div class="side-meta">
          <span>Acertos: <strong>${payload.hits || 0}</strong></span>
          <span>Pergunta: <strong>${currentQuestion}/${totalQuestions}</strong></span>
        </div>
      </div>
      <div class="card side-card side-extra">
        <div class="eyebrow">Você sabia?</div>
        <p>Fraudes digitais quase sempre exploram pressa, desatenção e confiança em processos aparentemente normais.</p>
      </div>
      <div class="card side-card side-extra">
        <div class="eyebrow">Ação rápida</div>
        <p>Ao suspeitar de uma abordagem, valide por canal oficial antes de clicar, pagar, informar dados ou liberar acesso.</p>
      </div>
    </aside>`;
}

function quizScreen(payload) {
  const question = payload.question;
  const progress = percent(question.number - 1, question.total);
  const timerPercent = clamp((question.timeLeft / questionTimeLimit(question)) * 100, 0, 100);
  const warning = question.timeLeft <= 5 ? "warning" : question.timeLeft <= 10 ? "caution" : "";
  const scene = scenarioTemplate(question);

  const progressBlocks = Array.from({ length: question.total }, (_, index) => {
    const current = index + 1 === question.number;
    const done = index + 1 < question.number;
    return `<div class="mini-block ${done ? "done" : current ? "current" : ""}"></div>`;
  }).join("");

  const options = question.alternatives.map((alternative, index) => {
    const key = String.fromCharCode(65 + index);
    const selected = state.selectedAnswer === index ? "selected" : "";
    return `
      <button class="answer-row ${selected}" data-answer="${index}">
        <div class="answer-key">${key}</div>
        <div class="answer-text">${escapeHtml(alternative)}</div>
        <div class="answer-mark">${selected ? "ok" : ""}</div>
      </button>`;
  }).join("");

  return `
    <div class="${shellClass()}">
      ${topbar({
        player: payload.player,
        stepTag: "Etapa 2 · Quiz",
        showProgress: true,
        progress,
        progressLabel: `Pergunta ${question.number} de ${question.total}`
      })}
      <main class="page">
        <section class="quiz-subbar card">
          <div class="subbar-left">
            <div class="eyebrow">Pergunta ${String(question.number).padStart(2, "0")} / ${question.total}</div>
            <div class="mini-progress">${progressBlocks}</div>
            <div class="crumbs">${quizCategories(question.category)}</div>
            <div class="quiz-stats-row">
              <span>Pontuacao atual: <strong>${payload.score} pts</strong></span>
              <span>Acertos: <strong>${payload.hits}</strong></span>
              <span>Nivel: <strong>${escapeHtml(question.level)}</strong></span>
            </div>
          </div>
          <div class="timer-shell ${warning}" id="timer-shell">
            <button class="btn btn-ghost small" id="finish-quiz" type="button">Ver resultado</button>
            <div class="timer-card">
              <div>
                <div class="eyebrow">Tempo</div>
                <strong id="timer-value">${String(question.timeLeft).padStart(2, "0")}s</strong>
              </div>
              <div class="timer-ring" id="timer-ring" style="--time:${timerPercent}"></div>
            </div>
          </div>
        </section>
        <section
          class="time-warning ${question.timeLeft <= 5 ? "danger" : ""}"
          id="time-warning"
          ${question.timeLeft <= 10 ? "" : "hidden"}
        >${question.timeLeft <= 5 ? "Ultimos segundos: confirme sua resposta agora." : "Tempo em contagem regressiva: revise e confirme com calma."}</section>
        <section class="quiz-main">
          <article class="scenario-pane">
            <div class="tag blue">Cenario</div>
            <p class="scenario-caption">Voce acabou de receber esta interacao no contexto corporativo.</p>
            <div class="mail-card card">
              <div class="mail-head">
                <div class="mail-id">
                  <div class="mail-avatar">${escapeHtml(scene.sender.slice(0, 2).toUpperCase())}</div>
                  <div>
                    <strong>${escapeHtml(scene.sender)}</strong>
                    <span>${escapeHtml(scene.meta)}</span>
                  </div>
                </div>
                <span class="tag yellow">${escapeHtml(scene.badge)}</span>
              </div>
              <div class="mail-subject">${escapeHtml(scene.subject)}</div>
              <div class="mail-content">
                ${scene.body.map((line) => `<p>${escapeHtml(line)}</p>`).join("")}
                <div class="callout blue">${escapeHtml(scene.highlight)}</div>
                <div class="callout red">${escapeHtml(question.situation)}</div>
                <div class="mail-footer">${escapeHtml(scene.footer)}</div>
              </div>
            </div>
          </article>
          <article class="question-pane card">
            <div class="tag gray">Categoria · ${escapeHtml(question.category)}</div>
            <h2>Qual e a atitude mais segura ao receber esta situacao?</h2>
            <p>Selecione apenas uma alternativa. Voce pode revisar antes de confirmar.</p>
            <div class="answer-list">${options}</div>
            <div class="question-footer">
              <div class="shortcut-hint">Use 1, 2, 3 ou 4 para selecionar.</div>
              <button class="btn btn-blue btn-lg" id="confirm-answer" ${state.selectedAnswer === null ? "disabled" : ""}>Confirmar resposta</button>
            </div>
          </article>
          ${scoreSidebar(payload, {
            currentQuestion: question.number,
            answeredQuestions: question.number - 1,
            showExtras: false
          })}
        </section>
      </main>
    </div>`;
}

function feedbackBullets(feedback) {
  const common = [
    "Validar a origem por canal oficial antes de agir.",
    "Nao ignorar urgencia artificial nem pedidos fora do processo.",
    "Acionar TI, seguranca ou gestor quando houver dado sensivel envolvido."
  ];

  if (feedback.tone === "success") {
    common[0] = "Sua decisao preservou o processo e reduziu o risco imediato.";
  }
  if (feedback.tone === "warning") {
    common[1] = "Tempo esgotado tambem representa risco quando a pressao induz erro.";
  }
  return common;
}

function feedbackScreen(payload) {
  const feedback = payload.feedback;
  const statusTone = feedback.tone;
  const bullets = feedbackBullets(feedback);
  const progressPercent = percent(feedback.questionNumber, payload.totalQuestions);
  const nextLabel = state.explanationCountdown > 0
    ? `Próxima pergunta (${state.explanationCountdown})`
    : `${feedback.questionNumber >= payload.totalQuestions ? "Ver resultado" : "Próxima pergunta →"}`;
  const nextDisabled = state.explanationCountdown > 0 ? "disabled" : "";
  const heroBlock = statusTone === "warning"
    ? `<section class="timeout-bar"><span class="timeout-icon">⏱</span><span>Tempo esgotado — O tempo terminou antes da confirmação.</span></section>`
    : `<section class="feedback-hero ${statusTone}">
          <div class="feedback-hero-copy">
            <div class="eyebrow light">Pergunta ${feedback.questionNumber} · ${escapeHtml(feedback.status)}</div>
            <h1>${escapeHtml(feedback.status)}.</h1>
            <p>${escapeHtml(feedback.summary)}</p>
          </div>
          <div class="feedback-metrics">
            <div><span>Pontos</span><strong>${payload.score}</strong></div>
            <div><span>Acertos</span><strong>${payload.hits}</strong></div>
          </div>
        </section>`;

  return `
    <div class="${shellClass()}">
      ${topbar({
        player: payload.player,
        stepTag: "Etapa 2 · Quiz",
        showProgress: true,
        progress: progressPercent,
        progressLabel: `Pergunta ${feedback.questionNumber} de ${payload.totalQuestions}`
      })}
      <main class="page feedback-page">
        ${heroBlock}
        <section class="feedback-grid">
          <article class="card feedback-main">
            <div class="tag ${statusTone === "success" ? "green" : statusTone === "warning" ? "yellow" : "red"}">
              Sua resposta segura depende de processo, nao de impulso
            </div>
            <h2>${escapeHtml(feedback.explanation)}</h2>
            <div class="why-card">
              <h3>Por que essa leitura importa?</h3>
              <div class="why-list">
                ${bullets.map((item, index) => `
                  <div class="why-item">
                    <div class="why-index">${index + 1}</div>
                    <div>${escapeHtml(item)}</div>
                  </div>`).join("")}
              </div>
            </div>
            <div class="principles-row">
              <div class="principle-card never">
                <div class="principle-icon">🚫</div>
                <small>Nunca</small>
                <strong>pular validacao</strong>
              </div>
              <div class="principle-card caution">
                <div class="principle-icon">⚠️</div>
                <small>Desconfie</small>
                <strong>de urgencia e segredo</strong>
              </div>
              <div class="principle-card always">
                <div class="principle-icon">✅</div>
                <small>Faca sempre</small>
                <strong>confirmacao em canal oficial</strong>
              </div>
            </div>
            <div class="feedback-actions">
              <button class="btn btn-ghost btn-lg" id="finish-quiz" type="button">Ver resultado agora</button>
              <button class="btn btn-blue btn-lg next-button ${state.explanationCountdown > 0 ? "locked" : "ready"}" id="next-question" ${nextDisabled}>${nextLabel}</button>
            </div>
          </article>
          ${scoreSidebar(payload, {
            currentQuestion: feedback.questionNumber,
            answeredQuestions: feedback.questionNumber,
            showExtras: true
          }).replace("</aside>", `<div class="card side-card side-extra fade-in-panel"><div class="eyebrow">Resposta correta</div><p>A alternativa segura foi <strong>${escapeHtml(feedback.correctOption)}</strong>. Use esta leitura como referência para a próxima rodada.</p></div></aside>`)}
        </section>
      </main>
    </div>`;
}

function classificationColor(classification) {
  if (classification === "Guardião Digital" || classification === "Guardiao Digital") {
    return "#7ce2b0";
  }
  if (classification === "Colaborador Atento") {
    return "#15a86c";
  }
  if (classification === "Em Treinamento") {
    return "#f2b544";
  }
  return "#e04949";
}

function resultMetrics(scorePercent) {
  return [
    { name: "Phishing por e-mail", value: clamp(scorePercent + 8, 22, 100) },
    { name: "Engenharia social", value: clamp(scorePercent - 10, 18, 100) },
    { name: "Senhas e MFA", value: clamp(scorePercent + 5, 20, 100) },
    { name: "Pagamentos e PIX", value: clamp(scorePercent - 3, 20, 100) }
  ];
}

function resultScreen(payload) {
  const result = payload.result;
  const scorePercent = clamp(result.scorePercent || Math.round((result.score / Math.max(result.maxScore || 1, 1)) * 100), 0, 100);
  const barColor = classificationColor(result.classification);
  const metrics = resultMetrics(scorePercent);
  const riskPosition = clamp(scorePercent, 10, 100);

  return `
    <div class="${shellClass()}">
      ${topbar({
        player: payload.player,
        stepTag: "Etapa 3 · Resultado",
        showProgress: true,
        progress: 100,
        progressLabel: `Pergunta ${result.total} de ${result.total}`
      })}
      <main class="page result-page">
        <section class="result-hero card">
          <div class="result-hero-grid">
            <div class="result-copy">
              <div class="eyebrow light">Treinamento concluido</div>
              <h1>Voce e um colaborador <span style="color:${barColor}">${escapeHtml(result.classification)}</span>.</h1>
              <p>${escapeHtml(result.message)}</p>
              <div class="summary-pills">
                <span class="summary-pill">${result.hits}/${result.total} acertos</span>
                <span class="summary-pill">${result.score} pontos</span>
                <span class="summary-pill">Perfil ${escapeHtml(result.classification)}</span>
              </div>
              <div class="hero-actions">
                <button class="btn btn-light" id="restart-quiz">Refazer quiz</button>
                <button class="btn btn-clear" id="go-identify">Nova identificacao</button>
              </div>
            </div>
            <div class="score-ring-shell">
              <div class="score-ring" style="--score:${scorePercent}">
                <div class="score-ring-inner">
                  <span>Score</span>
                  <strong>${scorePercent}</strong>
                  <small>de 100</small>
                </div>
              </div>
            </div>
          </div>
        </section>
        <section class="result-grid">
          <article class="card result-main">
            <div class="section-head">
              <h2>Desempenho por categoria</h2>
              <span>Compare-se ao seu proprio baseline</span>
            </div>
            ${metrics.map((item) => `
              <div class="metric-head">
                <strong>${escapeHtml(item.name)}</strong>
                <span>${item.value}%</span>
              </div>
              <div class="metric-bar">
                <div class="metric-fill" style="width:${item.value}%;background:${barColor}"></div>
                <div class="metric-marker" style="left:62%"></div>
              </div>`).join("")}
            <div class="risk-box">
              <div class="section-head">
                <h3>Seu perfil de risco</h3>
                <span>${escapeHtml(result.classification)}</span>
              </div>
              <div class="risk-gauge">
                <div class="risk-arc"></div>
                <div class="risk-needle" style="left:${riskPosition}%"></div>
              </div>
              <div class="risk-labels">
                <span>Vulneravel</span>
                <span>Em alerta</span>
                <span>Atento</span>
              </div>
              <div class="risk-callout">
                ${scorePercent >= 70
                  ? "Seu comportamento indica boa leitura de sinais suspeitos e respeito ao processo."
                  : scorePercent >= 40
                    ? "Voce ja identifica parte dos riscos, mas ainda precisa reforcar validacao e duplo controle."
                    : "Seu perfil pede reforco imediato em validacao de mensagens, acessos e solicitacoes urgentes."}
              </div>
            </div>
          </article>
          <aside class="result-side">
            <div class="card side-card">
              <div class="eyebrow">Resumo final</div>
              <div class="big-score">${result.score}<span> pts</span></div>
              <p><strong>${result.hits}/${result.total}</strong> respostas corretas na rodada.</p>
            </div>
            <div class="card side-card">
              <div class="eyebrow">Trilha recomendada</div>
              <div class="learning-card">
                <small>Modulo rapido · 4 min</small>
                <strong>Identificando engenharia social no telefone</strong>
              </div>
              <div class="learning-card">
                <small>Video · 6 min</small>
                <strong>Validacao dupla em pagamentos via PIX</strong>
              </div>
              <div class="learning-card">
                <small>Guia PDF</small>
                <strong>Cartilha de boas praticas no ambiente corporativo</strong>
              </div>
            </div>
            <div class="result-actions">
              <button class="btn btn-blue btn-lg" id="restart-quiz-second">Reiniciar treinamento</button>
            </div>
          </aside>
        </section>
      </main>
    </div>`;
}

function render() {
  let html = "";
  if (state.screen === "welcome") {
    html = welcomeScreen();
  } else if (state.screen === "identify") {
    html = identifyScreen();
  } else if (state.screen === "quiz") {
    html = quizScreen(state.payload);
  } else if (state.screen === "feedback") {
    html = feedbackScreen(state.payload);
  } else if (state.screen === "result") {
    html = resultScreen(state.payload);
  }

  app.innerHTML = `${state.error ? `<div class="error-banner">${escapeHtml(state.error)}</div>` : ""}${html}`;
  state.animateScreen = false;
  bindEvents();
}

function bindEvents() {
  const startNow = document.getElementById("start-now");
  if (startNow) {
    startNow.addEventListener("click", () => {
      showScreen("identify");
      render();
    });
  }

  const seeIdentify = document.getElementById("see-identify");
  if (seeIdentify) {
    seeIdentify.addEventListener("click", () => {
      showScreen("identify");
      render();
    });
  }

  const backWelcome = document.getElementById("back-welcome");
  if (backWelcome) {
    backWelcome.addEventListener("click", () => {
      showScreen("welcome");
      render();
    });
  }

  const identifyForm = document.getElementById("identify-form");
  if (identifyForm) {
    identifyForm.addEventListener("submit", handleStart);
  }

  document.querySelectorAll("[data-tenure]").forEach((button) => {
    button.addEventListener("click", () => {
      state.selectedTenure = button.getAttribute("data-tenure") || state.selectedTenure;
      document.querySelectorAll("[data-tenure]").forEach((chip) => {
        chip.classList.toggle("selected", chip === button);
      });
      const tenureInput = document.getElementById("tempo-empresa");
      if (tenureInput) {
        tenureInput.value = state.selectedTenure;
      }
    });
  });

  document.querySelectorAll("[data-answer]").forEach((button) => {
    button.addEventListener("click", () => {
      state.selectedAnswer = Number(button.getAttribute("data-answer"));
      render();
    });
  });

  const confirmAnswer = document.getElementById("confirm-answer");
  if (confirmAnswer) {
    confirmAnswer.addEventListener("click", () => submitAnswer(false));
  }

  const nextQuestion = document.getElementById("next-question");
  if (nextQuestion) {
    nextQuestion.addEventListener("click", goNext);
  }

  const finishQuizButton = document.getElementById("finish-quiz");
  if (finishQuizButton) {
    finishQuizButton.addEventListener("click", finishQuiz);
  }

  const restartQuiz = document.getElementById("restart-quiz");
  if (restartQuiz) {
    restartQuiz.addEventListener("click", restart);
  }

  const restartQuizSecond = document.getElementById("restart-quiz-second");
  if (restartQuizSecond) {
    restartQuizSecond.addEventListener("click", restart);
  }

  const goIdentify = document.getElementById("go-identify");
  if (goIdentify) {
    goIdentify.addEventListener("click", restart);
  }
}

window.addEventListener("keydown", (event) => {
  if (state.screen !== "quiz") {
    return;
  }
  if (["1", "2", "3", "4"].includes(event.key)) {
    state.selectedAnswer = Number(event.key) - 1;
    render();
  }
  if (event.key === "Enter" && state.selectedAnswer !== null) {
    submitAnswer(false);
  }
});

loadState().then(() => {
  if (state.screen === "welcome" && !state.payload) {
    render();
  }
});
