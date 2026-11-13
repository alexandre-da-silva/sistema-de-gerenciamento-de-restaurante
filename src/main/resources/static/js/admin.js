/* RestoERP: painel administrativo conectado à API. */
const adminState = { data: {}, query: "", status: "", date: new Intl.DateTimeFormat("en-CA", {timeZone:"America/Sao_Paulo"}).format(new Date()), version: 0 };
const adminMenus = [["Dashboard","home"],["Pedidos","bag"],["Mesas","table"],["Cardápio","book"],["Cozinha","chef"],["Estoque","box"],["Clientes","users"],["Relatórios","chart"],["Financeiro","coin"],["Configurações","settings"]];
const iconPaths = {
 home:'<path d="m3 10 9-7 9 7M5 9v12h5v-7h4v7h5V9"/>',
 bag:'<path d="M5 7h14l2 14H3L5 7Z"/><path d="M8 8V6a4 4 0 0 1 8 0v2"/>',
 table:'<path d="M3 8h18v4H3zM5 12v9m14-9v9M3 3v3m18-3v3M8 15h8m-8 0v5m8-5v5"/>',
 book:'<path d="M12 5v17M3 3c4-1 7 0 9 2 2-2 5-3 9-2v17c-4-1-7 0-9 2-2-2-5-3-9-2V3Z"/><path d="M6 7h2m-2 4h2m8-4h2m-2 4h2"/>',
 chef:'<path d="M6 13a5 5 0 1 1 2-9 5 5 0 0 1 8 0 5 5 0 1 1 2 9l-1 8H7l-1-8ZM7 17h10m-8-6 1 3m5-3-1 3"/>',
 box:'<path d="m12 2 10 5-10 5L2 7l10-5ZM2 7v11l10 5 10-5V7M12 12v11M7 5l10 5"/>',
 users:'<circle cx="9" cy="7" r="4"/><path d="M2 21v-3a7 7 0 0 1 14 0v3M17 3a4 4 0 0 1 0 8m2 3a6 6 0 0 1 3 5v2"/>',
 user:'<circle cx="12" cy="7" r="4"/><path d="M4 21v-3a8 8 0 0 1 16 0v3H4Z"/>',
 chart:'<path d="M3 21V12h4v9m3 0V7h4v14m3 0V3h4v18"/>',
 coin:'<ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M3 5v5c0 4 18 4 18 0V5M3 10v5c0 4 18 4 18 0v-5M3 15v4c0 4 18 4 18 0v-4"/>',
 settings:'<path d="m9 3 1-2h4l1 2 3 2 3 1v4l-2 2 2 2v4l-3 1-3 2-1 2h-4l-1-2-3-2-3-1v-4l2-2-2-2V6l3-1 3-2Z"/><circle cx="12" cy="12" r="4"/>',
 menu:'<path d="M3 5h18M3 12h18M3 19h18"/>', search:'<circle cx="10" cy="10" r="7"/><path d="m15 15 6 6"/>',
 bell:'<path d="M18 8a6 6 0 0 0-12 0c0 8-3 8-3 10h18c0-2-3-2-3-10M9 21h6"/>',
 down:'<path d="m6 9 6 6 6-6"/>', arrow:'<path d="M3 12h18m-6-6 6 6-6 6"/>',
 calendar:'<rect x="3" y="5" width="18" height="17" rx="2"/><path d="M7 2v6m10-6v6M3 11h18"/>',
 cloche:'<path d="M3 17a9 9 0 0 1 18 0H3ZM1 21h22M12 5V3m-1 0h2"/>',
 alert:'<path d="m10 3-9 17h22L14 3a2 2 0 0 0-4 0Z"/><path d="M12 8v5m0 3v1"/>',
 clock:'<circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 3"/>',
 check:'<circle cx="12" cy="12" r="10"/><path d="m7 12 3 3 7-7"/>',
 trophy:'<path d="M7 2h10v9a5 5 0 0 1-10 0V2ZM7 4H2v4a5 5 0 0 0 5 5m10-9h5v4a5 5 0 0 1-5 5M12 16v5m-5 1h10"/>',
 plus:'<path d="M12 4v16M4 12h16"/>', more:'<circle cx="4" cy="12" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="20" cy="12" r="1"/>',
 refresh:'<path d="M20 7a9 9 0 1 0 1 8M20 2v6h-6"/>', logout:'<path d="M9 3H3v18h6m5-14 5 5-5 5M7 12h14"/>', edit:'<path d="m15 3 6 6-12 12H3v-6L15 3ZM12 6l6 6"/>'
};
function ico(name) { return '<svg class="icon" viewBox="0 0 24 24" aria-hidden="true">'+(iconPaths[name]||iconPaths.cloche)+'</svg>'; }
const brl = value => new Intl.NumberFormat("pt-BR", {style:"currency",currency:"BRL"}).format(Number(value||0));
const quantity = value => new Intl.NumberFormat("pt-BR",{maximumFractionDigits:3}).format(Number(value||0));
const statusNames = { RECEBIDO:"Aguardando", EM_PREPARO:"Em preparo", PRONTO:"Pronto", ENTREGUE:"Entregue", CANCELADO:"Cancelado" };
function statusBadge(status) { return `<span class="badge ${esc(status)}">${ico(status==="RECEBIDO"?"clock":status==="PRONTO"||status==="ENTREGUE"?"check":"chef")}${esc(statusNames[status]||status)}</span>`; }
function emptyState(title, detail="", icon="cloche") { return `<div class="empty-state">${ico(icon)}<strong>${esc(title)}</strong>${detail?`<p>${esc(detail)}</p>`:""}</div>`; }
function paid(p) { return p.pago===true || Number(p.pago)===1; }
function activeOrders() { return (adminState.data.orders||[]).filter(p=>!['ENTREGUE','CANCELADO'].includes(p.status)); }
function matches(item) { const query=adminState.query.trim().toLocaleLowerCase("pt-BR"); return !query || JSON.stringify(item).toLocaleLowerCase("pt-BR").includes(query); }
function filteredOrders() { return (adminState.data.orders||[]).filter(matches).filter(p=>!adminState.status || p.status===adminState.status); }
function currentFirstName() { return (nomeUsuario||"Administrador").trim().split(/\s+/)[0]; }
function initials() { return (nomeUsuario||"Administrador").split(/\s+/).slice(0,2).map(x=>x[0]).join("").toUpperCase(); }
function shortDate(value) { if(!value)return "—"; const date=new Date(value); return Number.isNaN(date.getTime())?"—":date.toLocaleDateString("pt-BR"); }
function elapsed(value) { if(!value)return "—"; const minutes=Math.max(0,Math.floor((Date.now()-new Date(value).getTime())/60000)); return !Number.isFinite(minutes)?"—": minutes<60?`${minutes} min`:`${Math.floor(minutes/60)} h`; }
function growth(now, before) { if(before==null||now==null)return "Sem comparação disponível"; if(Number(before)===0)return Number(now)>0?"Primeiros registros no período":"Sem movimento no período"; const change=(Number(now)-Number(before))/Number(before)*100; return `<span class="${change>=0?'positive':'negative'}">${change>=0?'▲':'▼'} ${quantity(Math.abs(change))}%</span> em relação ao dia anterior`; }
function goAdmin(next) { page=next;adminState.query="";adminState.status="";render(); }
function adminShell() {
 const subtitles={Dashboard:"Aqui está o resumo do seu restaurante hoje.",Pedidos:"Acompanhe cada pedido, do recebimento à entrega.",Mesas:"Uma visão do seu salão, mesa por mesa.",Cardápio:"Os sabores da casa, organizados em um só lugar.",Cozinha:"Organize o preparo e mantenha os pedidos em dia.",Estoque:"Cuide dos ingredientes e acompanhe as reposições.",Clientes:"Conheça quem faz parte da história do seu restaurante.",Relatórios:"Acompanhe os recebimentos do seu restaurante.",Financeiro:"Confirme os pagamentos e acompanhe as contas.",Configurações:"Escolha os módulos disponíveis na sua operação."};
 return `<div class="admin-app"><button class="mobile-scrim" aria-label="Fechar menu" data-action="menu"></button><aside class="sidebar"><div class="brand">${ico('chef')}<div><strong>Resto<span>ERP</span></strong><small>ERP para Restaurantes</small></div></div><nav aria-label="Menu administrativo">${adminMenus.map(([label,icon])=>`<button data-nav="${label}" class="${page===label?'active':''}" ${page===label?'aria-current="page"':''}>${ico(icon)}<span>${label}</span></button>`).join('')}</nav><div class="sidebar-art"></div><div class="sidebar-bottom"><button data-action="logout">${ico('logout')} Sair da conta</button></div></aside><header class="topbar"><button class="icon-button" data-action="menu" aria-label="Alternar menu">${ico('menu')}</button><span class="topbar-title">RestoERP</span><label class="global-search">${ico('search')}<input id="admin-search" type="search" placeholder="Buscar pedidos, clientes, pratos, mesas..." aria-label="Buscar nesta tela" value="${esc(adminState.query)}"></label><button class="icon-button notification" data-action="notifications" aria-label="Ver pedidos aguardando">${ico('bell')}<i id="notification-count" hidden></i></button><div class="profile"><span class="avatar">${esc(initials())}</span><div><strong>${esc(nomeUsuario||'Administrador')}</strong><small>Administrador</small></div></div></header><main><div class="page-heading"><div><h1>${page==='Dashboard'?`Olá, ${esc(currentFirstName())}! <span aria-hidden="true">👋</span>`:esc(page)}</h1><p>${subtitles[page]||''}</p></div><div class="page-actions">${page==='Dashboard'?`<label class="date-control">${ico('calendar')}<input type="date" id="dashboard-date" aria-label="Data dos indicadores" value="${adminState.date}"></label>`:''}<button class="icon-button" data-action="refresh" aria-label="Atualizar dados">${ico('refresh')}</button>${['Pedidos','Mesas','Cardápio','Estoque','Clientes'].includes(page)?`<button class="action" data-action="create">${ico('plus')}${({Pedidos:'Novo pedido',Mesas:'Nova mesa',Cardápio:'Novo produto',Estoque:'Novo ingrediente',Clientes:'Novo cliente'})[page]}</button>`:''}</div></div><div id="error" role="alert"></div><div id="admin-content"><div class="loading" role="status">Carregando seu restaurante…</div></div></main></div>`;
}
async function renderAdmin() {
 const version=++adminState.version, currentToken=token;
 if(!adminMenus.some(([name])=>name===page)) page='Dashboard';
 $('root').innerHTML=adminShell();
 $('root').onclick=adminClick;
 $('admin-search').oninput=e=>{adminState.query=e.target.value;paintAdmin();};
 if($('dashboard-date')) $('dashboard-date').onchange=e=>{if(e.target.value){adminState.date=e.target.value;renderAdmin();}};
 let routes=[];
 if(page==='Dashboard') routes=[['summary',`admin/dashboard?data=${adminState.date}`],['orders','admin/pedidos/detalhes'],['tables','admin/mesas']];
 else if(['Pedidos','Cozinha','Financeiro'].includes(page)) routes=[['orders','admin/pedidos/detalhes']];
 else if(page==='Mesas') routes=[['tables','admin/mesas'],['orders','admin/pedidos/detalhes']];
 else if(page==='Cardápio') routes=[['products','admin/produtos'],['categories','admin/categorias']];
 else if(page==='Estoque') routes=[['stock','admin/estoque']];
 else if(page==='Clientes') routes=[['clients','admin/clientes']];
 else if(page==='Relatórios') routes=[['sales','admin/relatorios/vendas']];
 else if(page==='Configurações') routes=[['features','admin/recursos']];
 const results=await Promise.allSettled(routes.map(async([key,path])=>[key,await api(path)]));
 if(version!==adminState.version || currentToken!==token || !$('admin-content'))return;
 adminState.data={}; adminState.errors={};
 results.forEach((r,i)=>{if(r.status==='fulfilled')adminState.data[r.value[0]]=r.value[1];else adminState.errors[routes[i][0]]=r.reason.message;});
 const fatal=Object.keys(adminState.errors).filter(key=>!(page==='Dashboard'&&key==='tables'));
 if(fatal.length) { $('error').className='error-banner';$('error').textContent=Object.values(adminState.errors).join(' '); }
 paintAdmin();
}
function paintAdmin() {
 if(!$('admin-content'))return;
 const views={Dashboard:dashboardView,Pedidos:ordersView,Mesas:tablesView,Cardápio:menuView,Cozinha:kitchenView,Estoque:stockView,Clientes:clientsView,Relatórios:reportsView,Financeiro:financeView,Configurações:settingsView};
 $('admin-content').innerHTML=views[page]();
 const count=(adminState.data.orders||[]).filter(p=>p.status==='RECEBIDO').length;
 if($('notification-count')){$('notification-count').hidden=!count;$('notification-count').textContent=count;}
 const filter=$('status-filter');if(filter)filter.onchange=e=>{adminState.status=e.target.value;paintAdmin();};
}
function panel(title,icon,body,action='',extra='') {return `<section class="panel ${extra}"><div class="panel-head">${ico(icon)}<h2>${title}</h2>${action}</div>${body}</section>`;}
function stat(label,value,icon,foot,tone='') {return `<article class="stat ${tone}"><div class="stat-main"><div class="stat-icon">${ico(icon)}</div><div><div class="stat-label">${label}</div><div class="stat-value">${value}</div></div></div><div class="stat-foot">${foot}</div><span class="stat-watermark">${ico(icon)}</span></article>`;}
function dashboardView() {
 const s=adminState.data.summary; if(!s)return emptyState('Não foi possível carregar o dashboard','Use o botão Atualizar para tentar novamente.','alert');
 const orders=activeOrders().filter(matches), tables=(adminState.data.tables||[]).filter(matches), tops=(s.maisVendidos||[]).filter(matches);
 const occupation=Number(s.mesasTotal)?Math.round(Number(s.mesasOcupadas)/Number(s.mesasTotal)*100):0;
 const stats=`<div class="stats">${stat('Pedidos no dia',quantity(s.pedidosHoje),'cloche',growth(s.pedidosHoje,s.pedidosOntem))}${stat('Mesas Ocupadas',`${s.mesasOcupadas} <small>/ ${s.mesasTotal}</small>`,'table',`<span class="positive">▲ ${occupation}%</span> de ocupação atual`,'wine')}${stat('Faturamento',s.caixaHabilitado?brl(s.faturamento):'—','coin',s.caixaHabilitado?growth(s.faturamento,s.faturamentoOntem):'Caixa desabilitado')}${stat('Estoque Baixo',s.estoqueHabilitado?`${s.estoqueBaixo} <small>itens</small>`:'—','alert',s.estoqueHabilitado?'Ingredientes no limite mínimo':'Estoque desabilitado','red')}</div>`;
 const link=(target,text)=>`<button class="text-button" data-nav="${target}">${text} ${ico('arrow')}</button>`;
 return stats+`<div class="dashboard-middle">${panel('Pedidos em Andamento','bag',`<div class="panel-content">${orders.length?orderTable(orders.slice(0,5),true):emptyState('Tudo em dia por aqui','Os pedidos em andamento aparecerão aqui.','bag')}</div>`,link('Pedidos','Ver todos'),'orders-panel')}${panel('Mapa de Mesas','table',adminState.errors.tables?emptyState('Mesas indisponíveis',adminState.errors.tables,'table'):tables.length?tableMap(tables.slice(0,16)):emptyState('Seu salão começa aqui','Cadastre as mesas para visualizar a ocupação.','table'),'<span class="subtle-label">Salão principal</span>')}${panel('Fila da Cozinha','chef',kitchenList(orders.slice(0,5)),`<span class="count-chip">${orders.length} pedidos</span>`,'kitchen-panel')}</div><div class="dashboard-bottom">${panel('Vendas do Dia','chart',s.caixaHabilitado?`<div class="chart-body"><div class="chart-total"><strong>${brl(s.faturamento)}</strong><p>${growth(s.faturamento,s.faturamentoOntem)}</p></div>${salesChart(s.vendasPorHora||[])}</div>`:emptyState('Caixa desabilitado','Ative Caixa e pagamentos em Configurações.','chart'),'<span class="subtle-label">Recebimentos por hora</span>')}${panel('Mais Vendidos <small>(no dia)</small>','trophy',tops.length?tops.map((p,i)=>`<div class="bestseller"><span class="rank">${i+1}</span><span class="dish-thumb">${ico('cloche')}</span><div class="bestseller-info"><strong>${esc(p.nome)}</strong><small>${quantity(p.quantidade)} unidades</small></div><b>${brl(p.total)}</b></div>`).join(''):emptyState('Ainda sem vendas no período','O ranking considera pedidos com pagamento confirmado.','trophy'),link('Relatórios','Ver relatório'))}</div>`;
}
function orderTable(orders,compact=false,financial=false) {
 return `<div class="table-wrap"><table><thead><tr><th>Mesa</th><th>Cliente</th><th>Pedido</th><th>Status</th><th>Valor</th><th><span class="sr-only">Ações</span></th></tr></thead><tbody>${orders.map(p=>`<tr><td><span class="table-number">${p.mesa?String(p.mesa).padStart(2,'0'):'—'}</span></td><td><span class="client-cell">${ico('user')}${esc(p.cliente)}</span></td><td>${compact?esc((p.itens||[]).map(i=>i.produto).join(' + ').slice(0,33)):`<b>#${p.id}</b><small>${esc((p.itens||[]).map(i=>`${i.quantidade}× ${i.produto}`).join(', '))}</small>`}</td><td>${statusBadge(p.status)}${financial&&paid(p)?'<small class="positive">Pago</small>':''}</td><td>${brl(p.total)}</td><td>${financial&&p.status==='ENTREGUE'&&!paid(p)?`<button class="ghost" data-pay="${p.id}">Receber</button>`:`<button class="icon-button" data-order="${p.id}" aria-label="Ver pedido ${p.id}">${ico('more')}</button>`}</td></tr>`).join('')}</tbody></table></div>`;
}
function tableMap(tables) { const all=adminState.data.tables||[];return `<div class="tables-grid">${tables.map((m,i)=>`<button class="table-seat ${esc(m.status)} ${i%4===0||i%4===3?'round':''}" data-table="${m.id}" aria-label="Mesa ${m.numero}, ${esc(m.status)}"><span>${String(m.numero).padStart(2,'0')}</span></button>`).join('')}</div><div class="legend"><span><i class="dot"></i>Livre (${all.filter(m=>m.status==='LIVRE').length})</span><span><i class="dot busy"></i>Ocupada (${all.filter(m=>m.status==='OCUPADA').length})</span><span><i class="dot reserved"></i>Reservada (${all.filter(m=>m.status==='RESERVADA').length})</span></div>`;}
function kitchenList(orders) {return orders.length?`<div class="kitchen-list">${orders.map(p=>`<div class="kitchen-item"><span class="dish-thumb">${ico('cloche')}</span><div class="kitchen-info"><strong>${esc((p.itens||[]).map(i=>i.produto).join(' + ')||`Pedido #${p.id}`)}</strong><small>${p.mesa?'Mesa '+String(p.mesa).padStart(2,'0'):'Sem mesa'} · #${p.id}</small></div>${statusBadge(p.status)}<time>${elapsed(p.criado_em)}</time></div>`).join('')}</div>`:emptyState('Cozinha sem pendências','Novos pedidos aparecerão nesta fila.','chef');}
function salesChart(rows) {
 const values=Array.from({length:24},(_,hour)=>Number(rows.find(r=>Number(r.hora)===hour)?.total||0));
 const top=Math.max(100,...values), max=Math.ceil(top/100)*100;
 const x=h=>45+h*14.4,y=v=>140-v/max*120;
 const points=values.map((v,h)=>`${x(h)},${y(v)}`).join(' ');
 return `<svg class="chart" viewBox="0 0 390 170" role="img" aria-label="Recebimentos por hora em reais"><defs><linearGradient id="sales-fill" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#ff8b26" stop-opacity=".45"/><stop offset="100%" stop-color="#ff8b26" stop-opacity=".03"/></linearGradient></defs>${[0,1,2,3,4].map(i=>`<line x1="45" y1="${140-i*30}" x2="377" y2="${140-i*30}" stroke="#eae0da"/><text x="38" y="${144-i*30}" text-anchor="end">${quantity(max*i/4)}</text>`).join('')}<polygon points="45,140 ${points} 376.2,140" fill="url(#sales-fill)"/><polyline points="${points}" fill="none" stroke="#f67a0a" stroke-width="2.5"/>${values.map((v,h)=>v?`<circle cx="${x(h)}" cy="${y(v)}" r="3" fill="white" stroke="#f67a0a" stroke-width="1.5"><title>${h}h: ${brl(v)}</title></circle>`:'').join('')}${[0,4,8,12,16,20,23].map(h=>`<text x="${x(h)}" y="160" text-anchor="middle">${h}h</text>`).join('')}</svg>`;
}
function statusFilter() {return `<select id="status-filter" aria-label="Filtrar status"><option value="">Todos os status</option>${Object.entries(statusNames).map(([value,label])=>`<option value="${value}" ${adminState.status===value?'selected':''}>${label}</option>`).join('')}</select>`;}
function ordersView(){ const list=filteredOrders();return `<section class="panel list-panel"><div class="toolbar"><p>${list.length} pedidos · até 500 mais recentes</p>${statusFilter()}</div><div class="panel-content">${list.length?orderTable(list):emptyState('Nenhum pedido encontrado','Crie um pedido ou ajuste sua busca.','bag')}</div></section>`; }
function tablesView(){const list=(adminState.data.tables||[]).filter(matches);return list.length?`<div class="table-page-grid">${list.map(m=>`<button class="table-card" data-table="${m.id}"><span class="table-seat ${esc(m.status)}"><span>${String(m.numero).padStart(2,'0')}</span></span><h2>Mesa ${String(m.numero).padStart(2,'0')}</h2><p>${esc(m.status==='LIVRE'?'Livre':m.status==='OCUPADA'?'Ocupada':'Reservada')}</p><p>${m.capacidade_pessoas||'—'} lugares</p></button>`).join('')}</div>`:emptyState('Nenhuma mesa encontrada','Cadastre uma mesa ou ajuste sua busca.','table');}
function menuView(){const list=(adminState.data.products||[]).filter(matches);return list.length?`<div class="menu-grid">${list.map(p=>`<article class="product-card ${p.ativo?'':'inactive'}"><span class="dish-thumb">${ico('cloche')}</span><h2>${esc(p.nome)}</h2><span class="badge">${esc(p.categoria||'Sem categoria')} · ${p.ativo?'Ativo':'Inativo'}</span><p>${esc(p.descricao||'Sem descrição cadastrada.')}</p><footer><b>${brl(p.preco)}</b><button class="ghost" data-product="${p.id}">${ico('edit')} Editar</button></footer></article>`).join('')}</div>`:emptyState('Seu cardápio está esperando','Adicione o primeiro produto ou ajuste sua busca.','book');}
function kitchenView(){return `<div class="kanban">${['RECEBIDO','EM_PREPARO','PRONTO'].map(status=>{const list=activeOrders().filter(matches).filter(p=>p.status===status);return `<section class="kanban-column"><h2>${statusBadge(status)} <span class="count-chip">${list.length}</span></h2>${list.length?list.map(p=>`<article class="order-card"><header><b>Pedido #${p.id}</b><span>${p.mesa?'Mesa '+p.mesa:'Sem mesa'}</span></header><p>${esc(p.cliente)} · ${elapsed(p.criado_em)}</p><ul>${(p.itens||[]).map(i=>`<li><b>${i.quantidade}×</b> ${esc(i.produto)}</li>`).join('')}</ul>${p.observacao?`<p><b>Observação:</b> ${esc(p.observacao)}</p>`:''}<button class="action" data-step="${p.id}" data-next="${({RECEBIDO:'EM_PREPARO',EM_PREPARO:'PRONTO',PRONTO:'ENTREGUE'})[status]}">${({RECEBIDO:'Iniciar preparo',EM_PREPARO:'Marcar como pronto',PRONTO:'Confirmar entrega'})[status]} ${ico('arrow')}</button></article>`).join(''):emptyState('Nenhum pedido nesta etapa','','chef')}</section>`;}).join('')}</div>`;}
function stockView(){const list=(adminState.data.stock||[]).filter(matches);return `<section class="panel list-panel"><div class="toolbar"><p>${list.length} ingredientes · movimentação manual</p></div><div class="panel-content">${list.length?`<div class="table-wrap"><table><thead><tr><th>Ingrediente</th><th>Saldo atual</th><th>Mínimo</th><th>Situação</th><th>Ações</th></tr></thead><tbody>${list.map(i=>`<tr><td><b>${esc(i.nome)}</b></td><td>${quantity(i.quantidade_atual)} ${esc(i.unidade_medida)}</td><td>${quantity(i.quantidade_minima)} ${esc(i.unidade_medida)}</td><td>${Number(i.quantidade_atual)<=Number(i.quantidade_minima)?'<span class="stock-low">Reposição necessária</span>':'<span class="positive">Em dia</span>'}</td><td><button class="ghost" data-stock="${i.id}">Movimentar</button></td></tr>`).join('')}</tbody></table></div>`:emptyState('Nenhum ingrediente disponível',adminState.errors.stock||'Cadastre ingredientes para começar.','box')}</div></section>`;}
function clientsView(){const list=(adminState.data.clients||[]).filter(matches);return `<section class="panel list-panel"><div class="toolbar"><p>${list.length} clientes · até 500 registros</p></div><div class="panel-content">${list.length?`<div class="table-wrap"><table><thead><tr><th>Cliente</th><th>Contato</th><th>Pedidos</th><th>Total em pedidos</th><th>Último pedido</th></tr></thead><tbody>${list.map(c=>`<tr><td><span class="client-cell">${ico('user')}<b>${esc(c.nome)}</b></span><small>Desde ${shortDate(c.criado_em)}</small></td><td>${esc(c.email)}<small>${esc(c.telefone||'Telefone não informado')}</small></td><td>${c.pedidos}</td><td>${brl(c.total)}</td><td>${shortDate(c.ultimo_pedido)}</td></tr>`).join('')}</tbody></table></div>`:emptyState('Nenhum cliente encontrado','Cadastre um cliente ou ajuste a busca.','users')}</div></section>`;}
function reportsView(){const list=(adminState.data.sales||[]).filter(matches);return `<section class="panel list-panel"><div class="toolbar"><p>Recebimentos confirmados · até 365 dias com movimento</p><button class="ghost" data-action="export">Exportar CSV</button></div><div class="panel-content">${list.length?`<div class="table-wrap"><table><thead><tr><th>Data</th><th>Pagamentos</th><th>Total recebido</th></tr></thead><tbody>${list.map(s=>`<tr><td>${esc(String(s.dia).slice(0,10).split('-').reverse().join('/'))}</td><td>${s.pagamentos}</td><td><b>${brl(s.total)}</b></td></tr>`).join('')}</tbody></table></div>`:emptyState('Sem recebimentos para exibir',adminState.errors.sales||'Os pagamentos confirmados aparecerão aqui.','chart')}</div></section>`;}
function financeView(){const list=filteredOrders().filter(p=>p.status==='ENTREGUE');return `<section class="panel list-panel"><div class="toolbar"><p>Pedidos entregues · confirmação manual do recebimento integral</p></div><div class="panel-content">${list.length?orderTable(list,false,true):emptyState('Nenhuma conta para exibir','Os pedidos entregues aparecerão aqui.','coin')}</div></section>`;}
function settingsView(){return `<div class="settings-grid">${(adminState.data.features||[]).map(f=>`<article class="setting"><div><h2>${esc(f.descricao)}</h2><p>${f.codigo.startsWith('EMISSAO_')?'Integração fiscal ainda não disponível.':f.habilitado?'Disponível para sua operação.':'Desabilitado no momento.'}</p></div><button class="switch ${f.habilitado?'on':''}" role="switch" aria-checked="${!!f.habilitado}" aria-label="${esc(f.descricao)}" data-feature="${esc(f.codigo)}" ${f.codigo.startsWith('EMISSAO_')?'disabled':''}></button></article>`).join('')}</div>`;}
function adminDialog(title,html,onSave,saveText='Salvar') {
 $('formTitle').textContent=title;$('fields').innerHTML=html;$('formError').textContent='';
 const submit=$('form').querySelector('button[type="submit"], button.action');submit.hidden=!onSave;submit.textContent=saveText;submit.disabled=false;
 $('close').textContent=onSave?'Cancelar':'Fechar';
 $('form').onsubmit=async e=>{e.preventDefault();if(!onSave)return;submit.disabled=true;try{await onSave(new FormData(e.target));$('modal').close();await renderAdmin();}catch(err){$('formError').textContent=err.message;}finally{submit.disabled=false;}};
 $('modal').showModal();
}
function inputField(name,label,type='text',value='',attrs='') {return `<label>${label}<input name="${name}" type="${type}" value="${esc(value)}" ${attrs}></label>`;}
function selectField(name,label,items,value='') {return `<label>${label}<select name="${name}">${items.map(([v,t])=>`<option value="${esc(v)}" ${String(value)===String(v)?'selected':''}>${esc(t)}</option>`).join('')}</select></label>`;}
function productForm(product) {
 const p=product||{};
 const html=inputField('nome','Nome do produto','text',p.nome||'','required maxlength="140"')+inputField('descricao','Descrição','text',p.descricao||'','maxlength="1000"')+inputField('preco','Preço (R$)','number',p.preco||'','required min="0.01" step="0.01"')+selectField('categoriaId','Categoria',[['','Sem categoria'],...(adminState.data.categories||[]).map(c=>[c.id,c.nome])],p.categoriaId||'')+selectField('ativo','Disponibilidade',[['true','Ativo no cardápio'],['false','Inativo']],String(p.ativo!==false));
 adminDialog(p.id?'Editar produto':'Novo produto',html,d=>api('admin/produtos'+(p.id?'/'+p.id:''),p.id?'PUT':'POST',{nome:d.get('nome').trim(),descricao:d.get('descricao'),preco:Number(d.get('preco')),categoriaId:d.get('categoriaId')?Number(d.get('categoriaId')):null,ativo:d.get('ativo')==='true'}));
}
async function newOrderForm() {
 const [products,clients,tableResult]=await Promise.all([api('admin/produtos'),api('admin/clientes'),api('admin/mesas').then(data=>({data})).catch(error=>({error}))]);
 const available=products.filter(p=>p.ativo), customers=clients.filter(c=>c.ativo===true||Number(c.ativo)===1);
 if(!available.length)throw Error('Cadastre um produto ativo no Cardápio antes de criar um pedido.');
 if(!customers.length)throw Error('Cadastre um cliente antes de criar um pedido.');
 const row=()=>`<div class="order-line"><select name="produtoId" aria-label="Produto">${available.map(p=>`<option value="${p.id}">${esc(p.nome)} · ${brl(p.preco)}</option>`).join('')}</select><input name="quantidade" type="number" aria-label="Quantidade" value="1" min="1" max="99" step="1" required><button class="remove-line" type="button" aria-label="Remover item">×</button></div>`;
 adminDialog('Novo pedido',selectField('clienteId','Cliente',customers.map(c=>[c.id,c.nome]))+selectField('mesaId','Mesa',[['','Sem mesa'],...(tableResult.data||[]).filter(m=>m.status==='LIVRE').map(m=>[m.id,'Mesa '+m.numero])])+(tableResult.error?`<p class="detail-meta">Mesas indisponíveis: ${esc(tableResult.error.message)}</p>`:'')+`<label>Itens do pedido</label><div id="order-lines">${row()}</div><button type="button" class="add-line" id="add-order-line">+ Adicionar item</button>`+inputField('observacao','Observação','text','','maxlength="1000"'),d=>{
 const ids=d.getAll('produtoId'),counts=d.getAll('quantidade');if(!ids.length)throw Error('Adicione pelo menos um item.');
 return api('admin/pedidos','POST',{clienteId:Number(d.get('clienteId')),mesaId:d.get('mesaId')?Number(d.get('mesaId')):null,observacao:d.get('observacao'),itens:ids.map((id,i)=>({produtoId:Number(id),quantidade:Number(counts[i])}))});},'Criar pedido');
 $('add-order-line').onclick=()=>$('order-lines').insertAdjacentHTML('beforeend',row());
 $('order-lines').onclick=e=>{if(e.target.closest('.remove-line')&&$('order-lines').children.length>1)e.target.closest('.order-line').remove();};
}
function orderDetails(id) {
 const p=(adminState.data.orders||[]).find(p=>Number(p.id)===Number(id));if(!p)return;
 adminDialog('Pedido #'+p.id,`<p class="detail-meta"><b>${esc(p.cliente)}</b><br>${p.mesa?'Mesa '+p.mesa:'Sem mesa'} · ${esc(statusNames[p.status])}<br>${paid(p)?'Pagamento confirmado':'Pagamento ainda não confirmado'}</p><ul class="detail-list">${(p.itens||[]).map(i=>`<li><span>${i.quantidade}× ${esc(i.produto)}</span><b>${brl(i.subtotal)}</b></li>`).join('')}<li><b>Total</b><b>${brl(p.total)}</b></li></ul>${p.observacao?`<p class="detail-meta">${esc(p.observacao)}</p>`:''}${['RECEBIDO','EM_PREPARO'].includes(p.status)?'<p class="detail-meta">Use o botão abaixo somente se deseja cancelar este pedido.</p>':''}`,['RECEBIDO','EM_PREPARO'].includes(p.status)?()=>api(`cozinha/pedidos/${p.id}/status`,'PATCH',{status:'CANCELADO'}):null,'Cancelar pedido');
}
function tableDetails(id) {
 const m=(adminState.data.tables||[]).find(m=>Number(m.id)===Number(id));if(!m)return;
 const orders=(adminState.data.orders||[]).filter(p=>Number(p.mesa_id)===Number(id)&&p.status!=='CANCELADO'&&!paid(p));
 adminDialog('Mesa '+m.numero,`<p class="detail-meta">${esc(m.status)} · ${m.capacidade_pessoas||'—'} lugares</p>${orders.length?`<ul class="detail-list">${orders.map(p=>`<li><span>Pedido #${p.id} · ${esc(p.cliente)}</span><b>${brl(p.total)}</b></li>`).join('')}</ul><p class="detail-meta">A mesa é liberada após cancelar o pedido ou confirmar seu pagamento.</p>`:'<p class="detail-meta">Nenhum pedido em aberto nesta mesa.</p>'}`,null);
}
function paymentForm(id) {
 const p=(adminState.data.orders||[]).find(p=>Number(p.id)===Number(id));if(!p)return;
 const reference=crypto.randomUUID();
 adminDialog('Receber pedido #'+id,`<p class="detail-meta">Confirme apenas após receber <b>${brl(p.total)}</b>. Este registro não faz uma cobrança automática.</p>`+selectField('forma','Forma de pagamento',[['PIX','PIX'],['DINHEIRO','Dinheiro'],['CARTAO','Cartão']]),d=>api(`admin/pedidos/${id}/pagamentos`,'POST',{forma:d.get('forma'),referencia:reference}),'Confirmar recebimento');
}
function createAdminRecord() {
 if(page==='Cardápio')return productForm();
 if(page==='Pedidos')return newOrderForm();
 if(page==='Mesas')return adminDialog('Nova mesa',inputField('numero','Número da mesa','number','','required min="1" step="1"')+inputField('capacidade','Quantidade de lugares','number','4','required min="1" step="1"'),d=>api('admin/mesas','POST',{numero:Number(d.get('numero')),capacidade:Number(d.get('capacidade'))}));
 if(page==='Estoque')return adminDialog('Novo ingrediente',inputField('nome','Nome','text','','required maxlength="120"')+selectField('unidade','Unidade de medida',[['kg','Quilogramas (kg)'],['g','Gramas (g)'],['L','Litros (L)'],['ml','Mililitros (ml)'],['un','Unidades (un)']]),d=>api('admin/estoque','POST',{nome:d.get('nome').trim(),unidade:d.get('unidade')}));
 if(page==='Clientes')return adminDialog('Novo cliente',inputField('nome','Nome completo','text','','required maxlength="120"')+inputField('email','E-mail','email','','required maxlength="180"')+inputField('senha','Senha inicial (12 a 60 caracteres)','password','','required minlength="12" maxlength="60" autocomplete="new-password"'),d=>{const password=d.get('senha');if(new TextEncoder().encode(password).length>72)throw Error('A senha deve ter no máximo 72 bytes. Use menos caracteres especiais.');return api('admin/usuarios','POST',{nome:d.get('nome').trim(),email:d.get('email').trim(),senha:password,perfil:'CLIENTE'});});
}
async function adminClick(event) {
 const button=event.target.closest('button');if(!button)return;
 if(button.dataset.nav){goAdmin(button.dataset.nav);return;}
 const action=button.dataset.action;
 if(action==='menu'){const app=document.querySelector('.admin-app');app.classList.toggle(innerWidth<=960?'menu-open':'compact');return;}
 if(action==='logout'){adminState.version++;token='';perfil='';nomeUsuario='';$('root').onclick=null;login();return;}
 if(action==='notifications'){goAdmin('Pedidos');adminState.status='RECEBIDO';return;}
 if(action==='refresh'){await renderAdmin();return;}
 if(action==='export'){const rows=[['Data','Pagamentos','Total recebido'],...(adminState.data.sales||[]).filter(matches).map(r=>[r.dia,r.pagamentos,Number(r.total).toFixed(2).replace('.',',')])];const blob=new Blob(['\ufeff'+rows.map(row=>row.map(v=>'"'+String(v).replace(/"/g,'""')+'"').join(';')).join('\r\n')],{type:'text/csv;charset=utf-8'});const url=URL.createObjectURL(blob),a=document.createElement('a');a.href=url;a.download='restoerp-recebimentos.csv';a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);return;}
 button.disabled=true;
 try {
  if(action==='create')await createAdminRecord();
  else if(button.dataset.order)orderDetails(button.dataset.order);
  else if(button.dataset.table)tableDetails(button.dataset.table);
  else if(button.dataset.product)productForm((adminState.data.products||[]).find(p=>Number(p.id)===Number(button.dataset.product)));
  else if(button.dataset.pay)paymentForm(button.dataset.pay);
  else if(button.dataset.stock){const id=button.dataset.stock;adminDialog('Movimentar estoque',inputField('quantidade','Quantidade (positiva para entrada, negativa para saída)','number','','required step="0.001"')+inputField('motivo','Motivo da movimentação','text','','required maxlength="255"'),d=>api(`admin/estoque/${id}/movimentos`,'POST',{quantidade:Number(d.get('quantidade')),motivo:d.get('motivo')}));}
  else if(button.dataset.step){await api(`cozinha/pedidos/${button.dataset.step}/status`,'PATCH',{status:button.dataset.next});await renderAdmin();}
  else if(button.dataset.feature){const f=(adminState.data.features||[]).find(f=>f.codigo===button.dataset.feature);await api('admin/recursos/'+f.codigo,'PUT',{habilitado:!f.habilitado});await renderAdmin();}
 }catch(err){if($('error')){$('error').className='error-banner';$('error').textContent=err.message;}}
 finally{button.disabled=false;}
}
