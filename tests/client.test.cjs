const test=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const path=require('node:path');
const {JSDOM,VirtualConsole}=require('../frontend/node_modules/jsdom');
const esbuild=require('../frontend/node_modules/esbuild');
const base=path.resolve(__dirname,'..');
const products=[{id:1,nome:'Picanha Grelhada',descricao:'Com arroz e farofa',preco:59.90,precoPromocional:49.90,categoria:'Pratos Principais',tempoPreparo:15,ativo:true,destaque:true},{id:2,nome:'Café espresso',descricao:'Café fresco',preco:6.90,categoria:'Bebidas',tempoPreparo:5,ativo:true}];
const wait=()=>new Promise(r=>setTimeout(r,40));
async function setup(){
 const errors=[],writes=[];
 const vc=new VirtualConsole();vc.on('jsdomError',e=>errors.push(e.message));
 const dom=new JSDOM('<div id="root"></div>',{url:'http://localhost/',runScripts:'dangerously',pretendToBeVisual:true,virtualConsole:vc});const w=dom.window;
 w.matchMedia=()=>({matches:false,addEventListener(){},removeEventListener(){},addListener(){},removeListener(){}});
 w.ResizeObserver=class{observe(){}disconnect(){}unobserve(){}};
 w.scrollTo=()=>{};w.HTMLElement.prototype.scrollIntoView=()=>{};
 w.HTMLDialogElement.prototype.showModal=function(){this.open=true};
 w.HTMLDialogElement.prototype.close=function(){this.open=false};
 w.fetch=async(url,opt={})=>{
 const key=url.replace('/api/v1/','');let data;
 if(opt.method==='POST'){
  const body=JSON.parse(opt.body);writes.push({key,body});
  data={id:12,cliente:'Alexandre',status:'RECEBIDO',total:54.89,taxaServico:4.99,criadoEm:'2026-11-13T14:00:00',atualizadoEm:'2026-11-13T14:00:00',etapas:{RECEBIDO:'2026-11-13T14:00:00'},itens:[{produtoId:1,produto:'Picanha Grelhada',quantidade:1,precoUnitario:49.90,subtotal:49.90}],tempoEstimado:15};
 }else data=key==='publico/cardapio'?products:key==='cliente/perfil'?{nome:'Alexandre',email:'cliente@example.com',telefone:''}:key==='cliente/mesas'?[{id:1,numero:12}]:key.startsWith('publico/avaliacoes')?{itens:[],resumo:{total:0,media:0,positivas:0,neutras:0,negativas:0},distribuicao:[],totalFiltrado:0,destaque:[]}:[];
 return {ok:true,status:200,json:async()=>data};
 };
 w.eval(fs.readFileSync(path.join(base,'src/main/resources/static/client/client.js'),'utf8'));
 w.AstraClient.mount({token:'fixture',nome:'Alexandre',onLogout(){}});await wait();await wait();
 return {w,dom,writes,errors,close(){w.AstraClient.unmount();dom.window.close()}};
}
function click(w,selector){const el=w.document.querySelector(selector);assert.ok(el,selector);el.click();return wait()}
function button(w,text,scope=''){return [...w.document.querySelectorAll(scope+' button')].find(b=>b.textContent.trim()===text)}
function input(w,el,value){Object.getOwnPropertyDescriptor(w.HTMLInputElement.prototype,'value').set.call(el,value);el.dispatchEvent(new w.Event('input',{bubbles:true}));}
test('cart uses promotional cents and explicit optional service',()=>{
 const output=esbuild.transformSync(fs.readFileSync(path.join(base,'frontend/src/types.ts'),'utf8'),{loader:'ts',format:'cjs'}).code;
 const m={exports:{}};new Function('module','exports',output)(m,m.exports);
 assert.deepEqual(m.exports.totals({1:2,2:1},products,true),{subtotal:106.7,fee:10.67,total:117.37});
 assert.equal(m.exports.totals({1:1},products,false).total,49.9);
 assert.equal(m.exports.image('https://untrusted.example/a.svg'),'/images/placeholder.svg');
});
test('client adds, adjusts quantities and sends only IDs with a reference',async()=>{
 const ctx=await setup();const {w,writes,errors}=ctx;
 try{
 assert.equal(w.document.querySelectorAll('.c-product').length,2);
 await click(w,'.c-product .c-small');
 await click(w,'[aria-label="Aumentar Picanha Grelhada"]');
 assert.equal(w.document.querySelector('.c-quantity span').textContent,'2');
 await click(w,'[aria-label="Diminuir Picanha Grelhada"]');
 await click(w,'.c-service input');
 button(w,'Confirmar Pedido').click();await wait();
 assert.ok(w.document.querySelector('dialog').open);
 button(w,'Confirmar e enviar para a cozinha').click();await wait();
 assert.equal(writes.length,1);assert.equal(writes[0].key,'cliente/pedidos');
 assert.deepEqual(writes[0].body.itens,[{produtoId:1,quantidade:1}]);
 assert.equal(writes[0].body.taxaServico,true);assert.match(writes[0].body.referencia,/^[a-f0-9-]{36}$/);
 assert.equal('total' in writes[0].body,false);assert.equal(w.document.querySelector('.c-cart'),null);
 assert.deepEqual(errors,[]);
 }finally{ctx.close()}
});
test('coffee search ignores accents and opens supplied pouring menu; main dishes open conveyor',async()=>{
 const ctx=await setup();const {w,errors}=ctx;
 try{
 button(w,'Bebidas','.c-categories').click();await wait();
 input(w,w.document.querySelector('[aria-label="Buscar no cardápio"]'),'cafe');await wait();
 assert.equal(w.document.querySelectorAll('.c-product').length,1);
 assert.ok(w.document.querySelector('.c-coffee'));
 button(w,'Pratos Principais','.c-categories').click();await wait();
 assert.ok(w.document.querySelector('.c-conveyor'));
 assert.equal(w.document.querySelectorAll('.c-product').length,1);
 assert.deepEqual(errors,[]);
 }finally{ctx.close()}
});
test('navigation opens orders, offers, reviews and profile with real empty states',async()=>{
 const ctx=await setup();const {w,errors}=ctx;
 try{
 for(const name of ['Meus Pedidos','Acompanhar Pedido','Promoções','Avaliações','Perfil','Cardápio']){
 button(w,name,'.c-sidebar').click();await wait();assert.ok(w.document.querySelector('.c-main').textContent.length>30);
 }
 assert.deepEqual(errors,[]);
 }finally{ctx.close()}
});
