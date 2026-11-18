const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const path=require('node:path');
const vm=require('node:vm');
const {JSDOM}=require('../frontend/node_modules/jsdom');
const root=path.resolve(__dirname,'..');
const code=name=>fs.readFileSync(path.join(root,'src/main/resources/static/js',name),'utf8');
function sessionContext(){
 const store=new Map([['astrachefe:sessao:v1',JSON.stringify({token:'valid',perfil:'COZINHA',nome:'Equipe',page:'Cozinha'})]]);
 const elements=Object.fromEntries(['root','exit','refresh','create','content','error','modal','retry-session','leave-session'].map(k=>[k,{}]));
 const timers=[];const c={console,token:'valid',perfil:'COZINHA',nomeUsuario:'Equipe',page:'Cozinha',esc:String,$:k=>elements[k],sessionStorage:{getItem:k=>store.get(k),setItem:(k,v)=>store.set(k,v),removeItem:k=>store.delete(k)},api:async()=>[],document:{querySelectorAll:()=>[],visibilityState:'visible'},render(){},setInterval:fn=>{timers.push(fn);return timers.length},clearInterval(){},revogarSessaoNoServidor(){c.revoked=true}};
 vm.createContext(c);vm.runInContext(code('auth.js')+code('legacy.js'),c);c.login=()=>{c.loggedOut=true};
 return {c,elements,store,timers,run:s=>vm.runInContext(s,c)};
}
test('kitchen logout removes stored access and revokes server session',async()=>{
 const s=sessionContext();await s.run('renderLegacy()');s.elements.exit.onclick();
 assert.equal(s.store.has('astrachefe:sessao:v1'),false);assert.equal(s.c.token,'');assert.equal(s.c.revoked,true);assert.equal(s.c.loggedOut,true);
});
test('network error preserves session and retry restores it',async()=>{
 const s=sessionContext();s.c.api=async()=>{throw Error('network')};await s.run('restaurarSessao()');
 assert.equal(s.store.has('astrachefe:sessao:v1'),true);assert.equal(s.c.loggedOut,undefined);assert.match(s.elements.root.innerHTML,/Tentar novamente/);
 s.c.api=async()=>({perfil:'COZINHA',nome:'Equipe'});s.c.render=()=>{s.c.rendered=true};await s.elements['retry-session'].onclick();assert.equal(s.c.rendered,true);
});
test('401 restoration discards invalid saved session',async()=>{
 const s=sessionContext();s.c.api=async()=>{throw Object.assign(Error('expired'),{status:401})};await s.run('restaurarSessao()');assert.equal(s.store.size,0);assert.equal(s.c.loggedOut,true);
});
test('kitchen polling refreshes requests and keeps content on transient failure',async()=>{
 const s=sessionContext();let calls=0;s.c.api=async()=>{calls++;return []};await s.run('renderLegacy()');await s.timers[0]();assert.equal(calls,2);
 const html=s.elements.root.innerHTML;s.c.api=async()=>{throw Error('offline')};await s.timers[0]();assert.equal(s.elements.root.innerHTML,html);assert.match(s.elements.error.textContent,/offline/);
});
function adminContext(){
 const dom=new JSDOM('<div id="root"></div><div id="fields"></div><div id="error"></div>',{url:'http://localhost/',runScripts:'outside-only'});const w=dom.window;
 w.$=id=>w.document.getElementById(id);w.esc=v=>String(v??'').replaceAll('&','&amp;').replaceAll('"','&quot;').replaceAll('<','&lt;');w.token='token';w.perfil='ADMIN';w.nomeUsuario='Admin';w.page='Cardápio';w.api=async()=>[];
 w.eval(code('admin.js')+'\nwindow.adminState=adminState;');return {w,dom,run:s=>w.eval(s)};
}
test('admin follows pagination to include orders older than 500',async()=>{
 const s=adminContext();const calls=[];
 s.w.api=async p=>{calls.push(p);return calls.length===1?Array.from({length:500},(_,i)=>({id:1000-i})): [{id:4,status:'RECEBIDO'},{id:3,status:'ENTREGUE',pago:false}]};
 const list=await s.run('carregarPedidosAdmin()');assert.equal(list.length,502);assert.equal(list.at(-2).id,4);assert.equal(calls[1],'admin/pedidos/detalhes?antesDe=501');s.dom.window.close();
});
test('product form preserves and submits option recipes separately from base recipe',async()=>{
 const s=adminContext();let submit,body;
 s.w.adminDialog=(_title,html,save)=>{s.w.$('fields').innerHTML=html;submit=save};
 s.w.api=async(_path,_method,payload)=>{body=payload};
 s.run(`adminState.data={ingredients:[{id:1,nome:'Arroz',unidade_medida:'kg',custo_unitario:10},{id:2,nome:'Queijo',unidade_medida:'kg',custo_unitario:20}],categories:[]};productForm({id:3,nome:'Prato',preco:20,fichaTecnica:[{ingredienteId:1,quantidade:0.1}],variacoes:[{id:10,nome:'Grande',preco:30,ingredientes:[{ingredienteId:1,quantidade:0.2}]}],adicionais:[{id:20,nome:'Queijo extra',preco:5,ingredientes:[{ingredienteId:2,quantidade:0.05}]}]})`);
 const inputs=[...s.w.document.querySelectorAll('#fields > .product-form > fieldset input, #fields > .product-form > fieldset select, #fields > .product-form > fieldset textarea')];
 const data=new Map();for(const input of inputs)if(!input.closest('.repeat-area'))data.set(input.name,input.value);
 await submit({get:k=>data.get(k)||''});
 assert.equal(body.variacoes[0].id,10);assert.equal(body.variacoes[0].ingredientes[0].quantidade,0.2);assert.equal(body.adicionais[0].ingredientes[0].ingredienteId,2);assert.equal(body.fichaTecnica.length,1);assert.equal(body.fichaTecnica[0].quantidade,0.1);
 // Clearing a recipe sends [] instead of silently retaining the old one.
 s.w.document.querySelector('[data-repeater="variacoes"] [data-repeater="insumosOpcao"] .repeat-row').remove();await submit({get:k=>data.get(k)||''});assert.equal(body.variacoes[0].ingredientes.length,0);s.dom.window.close();
});
test('new ingredient form sends minimum stock and stock view offers editing',async()=>{
 const s=adminContext();let save,payload;s.w.page='Estoque';s.w.adminDialog=(_t,html,fn)=>{save=fn;assert.match(html,/name="quantidadeMinima"/)};s.w.api=async(_p,_m,b)=>{payload=b};s.run('createAdminRecord()');await save({get:k=>({nome:'Arroz',unidade:'kg',custoUnitario:'10',quantidadeMinima:'2.5'})[k]});assert.equal(payload.quantidadeMinima,2.5);s.run("adminState.data.stock=[{id:1,nome:'Arroz',quantidade_atual:1,quantidade_minima:2}]");assert.match(s.run('stockView()'),/data-minimum="1"/);s.dom.window.close();
});
