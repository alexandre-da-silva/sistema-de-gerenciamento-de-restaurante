const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const vm=require('node:vm');
function setup(response, page='Pedidos') {
 const elements={};
 for(const id of ['root','exit','refresh','create','error','content','modal']) elements[id]={};
 const ctx={token:'test-token',perfil:'ADMIN',page,esc:String,$:id=>elements[id]||null,document:{querySelectorAll:()=>[]},login:()=>{delete elements.content;elements.error={}},fetch:async()=>response};
 vm.createContext(ctx);
 for(const file of ['api.js','app.js']) vm.runInContext(fs.readFileSync('src/main/resources/static/js/'+file,'utf8').replace(/login\(\);\s*$/,''),ctx);
 return {ctx,elements,render:()=>vm.runInContext('render()',ctx)};
}
test('401 returns to login without accessing removed content',async()=>{
 const s=setup({ok:false,status:401,json:async()=>null});
 await s.render();
 assert.equal(s.ctx.token,'');
 assert.match(s.elements.error.textContent,/sessão expirou/);
});
test('cashier hides payment action for paid orders only',async()=>{
 const s=setup({ok:true,json:async()=>[
 {id:1,status:'ENTREGUE',total:10,pago:1},
 {id:2,status:'ENTREGUE',total:20,pago:0},
 {id:3,status:'PRONTO',total:30,pago:0}
 ]},'Caixa');
 await s.render();
 assert.match(s.elements.content.innerHTML,/Pago/);
 assert.doesNotMatch(s.elements.content.innerHTML,/data-pay="1"/);
 assert.match(s.elements.content.innerHTML,/data-pay="2"/);
 assert.doesNotMatch(s.elements.content.innerHTML,/data-pay="3"/);
});
test('login does not send an old bearer token',async()=>{
 const s=setup({}); let headers;
 s.ctx.fetch=async(_,options)=>{headers=options.headers;return {ok:true,json:async()=>({})}};
 await vm.runInContext('api("auth/login","POST",{})',s.ctx);
 assert.equal(headers.Authorization,undefined);
});
