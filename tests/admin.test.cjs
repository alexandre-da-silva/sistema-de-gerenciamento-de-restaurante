const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const vm=require('node:vm');
const {webcrypto}=require('node:crypto');
function setup(){
 const elements={root:{},'admin-content':{},error:{},'notification-count':{},'admin-search':{}};
 const ctx={console,Intl,Date,Number,String,Math,JSON,Map,Array,Promise,TextEncoder,crypto:webcrypto,setTimeout,URL,Blob,
  token:'test-token',perfil:'ADMIN',page:'Dashboard',nomeUsuario:'Alexandre Santana',
  esc:v=>String(v??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'})[c]),
  $:id=>elements[id]||null,api:async()=>[],render:()=>{},document:{querySelector:()=>({classList:{toggle(){}}})},innerWidth:1672};
 vm.createContext(ctx);vm.runInContext(fs.readFileSync('src/main/resources/static/js/admin.js','utf8'),ctx);
 return {ctx,elements,run:s=>vm.runInContext(s,ctx)};
}
test('dashboard renders actual totals and no example names or sales',()=>{
 const s=setup();s.run(`adminState.data={summary:{pedidosHoje:0,pedidosOntem:0,mesasOcupadas:0,mesasTotal:0,estoqueHabilitado:false,caixaHabilitado:false,maisVendidos:[]},orders:[],tables:[]};adminState.errors={}`);
 const html=s.run('dashboardView()');assert.match(html,/Caixa desabilitado/);assert.match(html,/Tudo em dia/);assert.doesNotMatch(html,/Carlos|4\.320|Ana Oliveira|NaN|undefined/);
});
test('all ten admin screens render without missing values',()=>{
 const s=setup();s.run(`adminState.errors={};adminState.data={summary:{pedidosHoje:2,pedidosOntem:1,mesasTotal:3,mesasOcupadas:1,caixaHabilitado:true,estoqueHabilitado:true,estoqueBaixo:0,faturamento:35,faturamentoOntem:30,maisVendidos:[]},orders:[],tables:[],products:[],categories:[],clients:[],stock:[],sales:[],features:[]}`);
 for(const screen of ['dashboardView','ordersView','tablesView','menuView','kitchenView','stockView','clientsView','reportsView','financeView','settingsView'])assert.doesNotMatch(s.run(screen+'()'),/undefined|NaN/);
});
test('customer text is escaped and search filters orders',()=>{
 const s=setup();s.run(`adminState.data.orders=[{id:1,cliente:'<img src=x onerror=alert(1)>',status:'RECEBIDO',total:20,itens:[{produto:'Arroz',quantidade:1}]}];adminState.query='arroz'`);
 const html=s.run('orderTable(filteredOrders())');assert.match(html,/&lt;img/);assert.doesNotMatch(html,/<img src=x/);s.run("adminState.query='inexistente'");assert.equal(s.run('filteredOrders().length'),0);
});
test('payment button is offered only for delivered unpaid orders',()=>{
 const s=setup();s.run(`adminState.data.orders=[{id:1,status:'ENTREGUE',pago:true,total:10},{id:2,status:'ENTREGUE',pago:0,total:20},{id:3,status:'PRONTO',pago:0,total:30}]`);
 const html=s.run('financeView()');assert.match(html,/data-pay="2"/);assert.doesNotMatch(html,/data-pay="1"|data-pay="3"/);
});
test('kitchen displays valid next states and hides terminal orders',()=>{
 const s=setup();s.run(`adminState.data.orders=['RECEBIDO','EM_PREPARO','PRONTO','ENTREGUE','CANCELADO'].map((status,i)=>({id:i+1,status,total:20,itens:[]}))`);
 const html=s.run('kitchenView()');assert.match(html,/data-next="EM_PREPARO"/);assert.match(html,/data-next="PRONTO"/);assert.match(html,/data-next="ENTREGUE"/);assert.doesNotMatch(html,/Pedido #4|Pedido #5/);
});
test('dashboard can load when the tables module is disabled',async()=>{
 const s=setup();s.ctx.api=async path=>{if(path==='admin/mesas')throw Error('Módulo desabilitado');if(path.startsWith('admin/dashboard'))return {pedidosHoje:0,pedidosOntem:0,mesasOcupadas:0,mesasTotal:0,maisVendidos:[]};return [];};
 await s.run('renderAdmin()');assert.match(s.elements['admin-content'].innerHTML,/Mesas indisponíveis/);assert.match(s.elements['admin-content'].innerHTML,/Vendas do Dia/);
});
test('late responses cannot overwrite the next screen',async()=>{
 const s=setup();let resolve;const pending=new Promise(r=>resolve=r);s.ctx.api=()=>pending;const render=s.run('renderAdmin()');s.run('adminState.version++');s.elements['admin-content'].innerHTML='NEW SCREEN';resolve([]);await render;assert.equal(s.elements['admin-content'].innerHTML,'NEW SCREEN');
});
test('zero-base percentage and 24 hour chart do not produce NaN or infinity',()=>{
 const s=setup();assert.doesNotMatch(s.run('growth(100,0)'),/Infinity|NaN/);assert.match(s.run('growth(0,0)'),/Sem movimento/);const html=s.run('salesChart([])');assert.doesNotMatch(html,/Infinity|NaN/);assert.match(html,/23h/);
});
