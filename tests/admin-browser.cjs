const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const path=require('node:path');
const date='2026-11-13';
const products=[['Picanha na Brasa',89.90],['Salmão Grelhado',72.50],['Massa ao Pesto',64.90],['Burger Artesanal',58],['Risoto de Cogumelos',76.90]].map(([nome,preco],i)=>({id:i+1,nome,preco,descricao:'Preparado na hora com ingredientes selecionados.',ativo:true,categoria:'Pratos principais',categoriaId:1}));
const clients=['Ana Oliveira','Marcos Silva','Juliana Costa','Ricardo Alves','Fernanda Lima'].map((nome,i)=>({id:i+1,nome,email:`cliente${i}@example.com`,ativo:true,pedidos:4,total:250,criado_em:date,ultimo_pedido:date}));
const tables=Array.from({length:16},(_,i)=>({id:i+1,numero:i+1,capacidade_pessoas:4,status:[3,7,10,12,15,16].includes(i+1)?'OCUPADA':[6,14].includes(i+1)?'RESERVADA':'LIVRE'}));
const orders=clients.map((c,i)=>({id:i+1,cliente:c.nome,cliente_id:c.id,mesa_id:[3,7,10,12,15][i],mesa:[3,7,10,12,15][i],status:['EM_PREPARO','PRONTO','EM_PREPARO','RECEBIDO','RECEBIDO'][i],total:products[i].preco,pago:false,criado_em:new Date(Date.now()-(i+1)*60000).toISOString(),itens:[{produto_id:i+1,produto:products[i].nome,quantidade:1,subtotal:products[i].preco}]}));
orders.push({...orders[0],id:6,status:'ENTREGUE',pago:true},{...orders[1],id:7,status:'ENTREGUE',pago:false});
const features=['CARDAPIO_ONLINE','PEDIDOS_ONLINE','PAINEL_COZINHA','MESAS_E_COMANDAS','CONTROLE_ESTOQUE','CAIXA_E_PAGAMENTOS','RELATORIOS'].map(c=>({codigo:c,descricao:c.replaceAll('_',' '),habilitado:true}));
const summary={pedidosHoje:87,pedidosOntem:78,mesasTotal:16,mesasOcupadas:6,estoqueHabilitado:true,caixaHabilitado:true,estoqueBaixo:5,faturamento:4320.50,faturamentoOntem:3661.44,vendasPorHora:[{hora:11,total:300},{hora:12,total:500},{hora:13,total:800},{hora:14,total:1200},{hora:15,total:1100},{hora:16,total:1500},{hora:17,total:2300},{hora:18,total:2100},{hora:19,total:3000},{hora:20,total:3500},{hora:21,total:4320},{hora:22,total:3800}],maisVendidos:products.slice(0,3).map((p,i)=>({...p,quantidade:24-i*6,total:2157.60-i*550}))};
const stock=[{id:1,nome:'Arroz',unidade_medida:'kg',quantidade_atual:1,quantidade_minima:2}];
(async()=>{
 const browser=await chromium.launch({...(process.env.CHROMIUM_PATH?{executablePath:process.env.CHROMIUM_PATH}:{}),headless:true,args:['--no-sandbox']});
 const page=await browser.newPage({viewport:{width:1672,height:941},deviceScaleFactor:1});
 const errors=[],writes=[];page.on('pageerror',e=>errors.push(e.message));
 let empty=false,unauthorized=false;
 await page.route('**/api/v1/**',async route=>{
  const req=route.request(), url=new URL(req.url()), endpoint=url.pathname.replace('/api/v1/','');
  if(unauthorized&&endpoint!=='auth/login')return route.fulfill({status:401,contentType:'application/json',body:'{}'});
  let result;
  if(endpoint==='auth/login')result={accessToken:'test-admin-token',perfil:'ADMIN',nome:'Carlos Mendes'};
  else if(req.method()!=='GET'){writes.push({endpoint,method:req.method(),body:req.postDataJSON()});result={};}
  else if(endpoint==='admin/dashboard')result=empty?{...summary,pedidosHoje:0,pedidosOntem:0,faturamento:0,faturamentoOntem:0,mesasTotal:0,mesasOcupadas:0,estoqueBaixo:0,vendasPorHora:[],maisVendidos:[]}:summary;
  else result=({'admin/pedidos/detalhes':orders,'admin/mesas':tables,'admin/produtos':products,'admin/clientes':clients,'admin/categorias':[{id:1,nome:'Pratos principais'}],'admin/estoque':stock,'admin/relatorios/vendas':[{dia:date,total:4320.5,pagamentos:20}],'admin/recursos':features})[endpoint]||[];
  if(empty&&Array.isArray(result))result=[];
  await route.fulfill({status:200,contentType:'application/json',body:JSON.stringify(result)});
 });
 await page.goto(process.env.TEST_BASE_URL||'http://localhost:8085');
 await page.locator('[name=email]').fill('admin@example.com');await page.locator('[name=senha]').fill('test-password');await page.getByRole('button',{name:'Entrar',exact:true}).click();
 await page.getByRole('heading',{name:'Pedidos em Andamento'}).waitFor();
 await page.screenshot({path:path.resolve('tests/dashboard-desktop.png'),fullPage:true});
 assert.equal(await page.locator('.stats .stat').count(),4);
 for(const name of ['Pedidos','Mesas','Cardápio','Cozinha','Estoque','Clientes','Relatórios','Financeiro','Configurações']){
  await page.locator(`nav [data-nav="${name}"]`).click();await page.locator('.loading').waitFor({state:'detached'});assert.ok(await page.locator('#admin-content').innerText());
 }
 await page.locator('nav [data-nav="Cardápio"]').click();await page.locator('[data-product="1"]').click();
 await page.locator('#modal [name=nome]').fill('Picanha especial');await page.locator('#modal button.action').click();await page.locator('#modal').waitFor({state:'hidden'});
 assert.ok(writes.some(w=>w.method==='PUT'&&w.body.nome==='Picanha especial'));
 await page.locator('nav [data-nav="Pedidos"]').click();assert.equal(await page.getByRole('button',{name:'Novo pedido',exact:true}).count(),0);
 await page.locator('nav [data-nav="Cozinha"]').click();await page.locator('[data-step="4"]').click();assert.ok(writes.some(w=>w.endpoint==='cozinha/pedidos/4/status'&&w.body.status==='EM_PREPARO'));
 await page.locator('nav [data-nav="Financeiro"]').click();await page.locator('[data-pay="7"]').waitFor();assert.equal(await page.locator('[data-pay="6"]').count(),0);await page.locator('[data-pay="7"]').click();await page.locator('#modal button.action').click();await page.locator('#modal').waitFor({state:'hidden'});assert.ok(writes.some(w=>w.endpoint==='admin/pedidos/7/pagamentos'&&w.body.referencia));
 await page.locator('nav [data-nav="Clientes"]').click();await page.locator('#admin-search').fill('Juliana');assert.equal(await page.locator('tbody tr').count(),1);
 await page.locator('nav [data-nav="Dashboard"]').click();await page.getByRole('heading',{name:'Pedidos em Andamento'}).waitFor();await page.setViewportSize({width:390,height:844});await page.screenshot({path:path.resolve('tests/dashboard-mobile.png'),fullPage:true});
 assert.ok(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),'mobile page must not overflow horizontally');
 await page.getByRole('button',{name:'Alternar menu',exact:true}).click();await page.locator('nav [data-nav="Mesas"]').click();assert.equal(await page.locator('.admin-app.menu-open').count(),0);
 empty=true;await page.evaluate(()=>goAdmin('Dashboard'));await page.getByText('Tudo em dia por aqui').waitFor();await page.screenshot({path:path.resolve('tests/dashboard-empty.png'),fullPage:true});
 unauthorized=true;await page.getByRole('button',{name:'Atualizar dados'}).click();await page.getByRole('heading',{name:'AstraChefe',exact:true}).waitFor();assert.ok((await page.locator('#error').innerText()).includes('sessão expirou'));
 assert.deepEqual(errors,[]);console.log('PASS: 10 telas, edição, bloqueio de criação pelo administrador, cozinha, pagamento, busca, mobile, vazio e sessão expirada.');await browser.close();
})().catch(e=>{console.error(e);process.exit(1)});
