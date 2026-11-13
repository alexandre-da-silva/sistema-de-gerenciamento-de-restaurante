const routes = {
  Pedidos: "cliente/pedidos",
  Cozinha: "cozinha/pedidos",
  Cardápio: "publico/cardapio",
  Mesas: "admin/mesas",
  Estoque: "admin/estoque",
  Caixa: "admin/pedidos",
  Recursos: "admin/recursos",
  Relatórios: "admin/relatorios/vendas",
  Usuários: "admin/usuarios",
};
async function render() {
  const renderToken = token;
  const renderPage = page;
  const admin = perfil === "ADMIN";
  const menus = admin
    ? Object.keys(routes)
    : perfil === "COZINHA"
      ? ["Cozinha"]
      : ["Cardápio", "Pedidos"];
  $("root").innerHTML =
    '<aside><div class="logo">Restaurante</div><nav>' +
    menus
      .map(
        (m) =>
          '<button data-page="' +
          m +
          '" class="' +
          (m === page ? "active" : "") +
          '">' +
          m +
          "</button>",
      )
      .join("") +
    "</nav><footer>" +
    esc(perfil) +
    ' <button id="exit" class="ghost">Sair</button></footer></aside><main><header>Operação / ' +
    page +
    '<button id="refresh" class="ghost">Atualizar</button></header><div class="hero"><h1>' +
    page +
    '</h1><button id="create" class="action">Adicionar</button></div><p id="error" role="status"></p><section class="panel" id="content"></section></main>';
  $("exit").onclick = () => {
    token = "";
    perfil = "";
    login();
  };
  $("refresh").onclick = render;
  document.querySelectorAll("[data-page]").forEach(
    (b) =>
      (b.onclick = () => {
        page = b.dataset.page;
        render();
      }),
  );
  const path = admin && page === "Pedidos" ? "admin/pedidos" : routes[page];
  const configs = {
    Mesas: [
      ["numero", "Número", "number"],
      ["capacidade", "Lugares", "number"],
    ],
    Estoque: [
      ["nome", "Nome"],
      ["unidade", "Unidade"],
    ],
    Usuários: [
      ["nome", "Nome"],
      ["email", "E-mail", "email"],
      ["senha", "Senha (mínimo 12)", "password"],
      [
        "perfil",
        "Perfil",
        "select",
        ["ADMIN", "CLIENTE", "COZINHA"].map((x) => [x, x]),
      ],
    ],
  };
  $("create").hidden = !(
    configs[page] ||
    page === "Cardápio" ||
    page === "Pedidos"
  );
  $("create").onclick = async () => {
    try {
      if (configs[page])
        form("Cadastrar", configs[page], (d) =>
          api(
            path,
            "POST",
            page === "Mesas"
              ? { numero: +d.numero, capacidade: +d.capacidade }
              : d,
          ),
        );
      else if (page === "Cardápio" && admin)
        form(
          "Cadastrar produto",
          [
            ["nome", "Nome"],
            ["descricao", "Descrição"],
            ["preco", "Preço", "number"],
          ],
          (d) =>
            api("admin/produtos", "POST", {
              ...d,
              preco: +d.preco,
              ativo: true,
            }),
        );
      else {
        const produtos = await api("publico/cardapio");
        if (!produtos.length) throw Error("Cardápio vazio.");
        form(
          "Novo pedido",
          [
            [
              "produto",
              "Produto",
              "select",
              produtos.map((p) => [p.id, p.nome]),
            ],
            ["quantidade", "Quantidade", "number"],
          ],
          (d) =>
            api("cliente/pedidos", "POST", {
              itens: [{ produtoId: +d.produto, quantidade: +d.quantidade }],
            }),
        );
      }
    } catch (err) {
      $("error").textContent = err.message;
    }
  };
  try {
    const data = await api(path);
    if (token !== renderToken || page !== renderPage || !$("content")) return;
    $("content").innerHTML =
      data
        .map((item) => {
          let title = item.nome || item.codigo || "Pedido #" + item.id,
            detail = "",
            button = "";
          if (page === "Cardápio")
            detail = "R$ " + Number(item.preco).toFixed(2);
          else if (page === "Mesas") {
            title = "Mesa " + item.numero;
            detail = item.status;
          } else if (page === "Estoque") {
            detail = item.quantidade_atual + " " + item.unidade_medida;
            button = '<button data-stock="' + item.id + '">Movimentar</button>';
          } else if (page === "Usuários")
            detail = item.email + " · " + item.perfil;
          else if (page === "Relatórios") {
            title = item.dia;
            detail = "R$ " + item.total;
          } else if (page === "Recursos") {
            detail = item.descricao;
            button =
              '<button data-feature="' +
              item.codigo +
              '" data-enabled="' +
              item.habilitado +
              '">' +
              (item.habilitado ? "Desabilitar" : "Habilitar") +
              "</button>";
          } else {
            detail =
              item.status +
              " · R$ " +
              item.total +
              (item.itens
                ? " · " +
                  item.itens
                    .map((i) => i.quantidade + "× " + i.produto)
                    .join(", ")
                : "");
            const next = {
              RECEBIDO: "EM_PREPARO",
              EM_PREPARO: "PRONTO",
              PRONTO: "ENTREGUE",
            }[item.status];
            if (page === "Cozinha" && next)
              button =
                '<button data-step="' +
                item.id +
                '" data-status="' +
                next +
                '">' +
                next +
                "</button>";
            const pago = item.pago === true || Number(item.pago) === 1;
            if (page === "Caixa" && pago) detail += " · Pago";
            if (page === "Caixa" && item.status === "ENTREGUE" && !pago)
              button = '<button data-pay="' + item.id + '">Pagamento</button>';
          }
          return (
            '<div class="row"><div><b>' +
            esc(title) +
            "</b><small>" +
            esc(detail) +
            "</small></div>" +
            button +
            "</div>"
          );
        })
        .join("") || "<p>Nenhum registro.</p>";
  } catch (err) {
    if (token !== renderToken || page !== renderPage || !$("content")) return;
    $("error").textContent = err.message;
  }
  $("content").onclick = async (e) => {
    const b = e.target.closest("button");
    if (!b) return;
    b.disabled = true;
    try {
      if (b.dataset.step)
        await api("cozinha/pedidos/" + b.dataset.step + "/status", "PATCH", {
          status: b.dataset.status,
        });
      if (b.dataset.feature)
        await api("admin/recursos/" + b.dataset.feature, "PUT", {
          habilitado: b.dataset.enabled !== "true",
        });
      if (b.dataset.stock) {
        form(
          "Movimentar",
          [
            ["quantidade", "Quantidade positiva ou negativa", "number"],
            ["motivo", "Motivo"],
          ],
          (d) =>
            api("admin/estoque/" + b.dataset.stock + "/movimentos", "POST", {
              ...d,
              quantidade: +d.quantidade,
            }),
        );
        return;
      }
      if (b.dataset.pay) {
        const referencia = crypto.randomUUID();
        form(
          "Confirmar recebimento",
          [
            [
              "forma",
              "Forma",
              "select",
              ["PIX", "DINHEIRO", "CARTAO"].map((x) => [x, x]),
            ],
          ],
          (d) =>
            api("admin/pedidos/" + b.dataset.pay + "/pagamentos", "POST", {
              ...d,
              referencia,
            }),
        );
        return;
      }
      await render();
    } catch (err) {
      $("error").textContent = err.message;
    } finally {
      b.disabled = false;
    }
  };
}
login();
