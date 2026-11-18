"""Smoke isolado em Chromium com APIs e Storage simulados (sem servidor real)."""
import json
from pathlib import Path
from playwright.sync_api import sync_playwright
root=Path(__file__).resolve().parents[1] / 'src/main/resources/static'
products=[
 {'id':1,'nome':'Hambúrguer da Casa','descricao':'Artesanal na brasa','preco':30,'categoria':'Lanches','tempoPreparo':15,'ativo':True,'destaque':True,'variacoes':[{'id':11,'nome':'Normal','preco':30},{'id':12,'nome':'Grande','preco':40}],'adicionais':[{'id':21,'nome':'Bacon extra','preco':6}],'disponivelAgora':True},
 {'id':2,'nome':'Café espresso','descricao':'Quentinho','preco':6.9,'categoria':'Bebidas','tempoPreparo':5,'ativo':True,'disponivelAgora':True}]
with sync_playwright() as p:
 browser=p.chromium.launch(headless=True,executable_path='/usr/bin/chromium',args=['--no-sandbox'])
 page=browser.new_page(viewport={'width':390,'height':844})
 errors=[]
 page.on('pageerror',lambda error:errors.append(str(error)))
 page.set_content('''<!doctype html><html lang="pt-BR"><head><meta charset="utf-8"/></head><body>
 <div id="root"></div><dialog id="modal"><form id="form"><h2 id="formTitle"></h2><div id="fields"></div><p id="formError"></p><menu><button type="button" id="close">Cancelar</button><button class="action">Salvar</button></menu></form></dialog></body></html>''')
 for css in ['css/app.css','css/admin.css','client/client.css']:
  page.add_style_tag(content=(root/css).read_text())
 page.evaluate('''(products)=>{
 const make=()=>{const m=new Map();return {getItem(k){return m.get(k)||null},setItem(k,v){m.set(k,String(v))},removeItem(k){m.delete(k)}}};
 Object.defineProperty(window,'sessionStorage',{value:make(),configurable:true});
 Object.defineProperty(window,'localStorage',{value:make(),configurable:true});
 sessionStorage.setItem('astrachefe:sessao:v1',JSON.stringify({token:'fixture',nome:'Teste',perfil:'CLIENTE',page:'Início'}));
 Object.defineProperty(window.crypto,'randomUUID',{value:()=> '12345678-1234-4234-8234-'+String(Math.floor(Math.random()*1e12)).padStart(12,'0')});
 window.__writes=[];window.history.pushState=()=>{};
 window.fetch=async(path,options={})=>{
  const key=String(path).split('/api/v1/')[1]?.split('?')[0];let data=[];
  if(key==='auth/me')data={nome:'Teste',email:'teste@example.com',perfil:'CLIENTE'};
  if(key==='cliente/perfil')data={nome:'Teste',email:'teste@example.com',telefone:''};
  if(key==='publico/cardapio')data=products;
  if(key==='publico/em-alta')data=[{produtoId:1,unidadesVendidas:20,popular:true}];
  if(key==='publico/avaliacoes')data={itens:[],resumo:{total:0,media:0,positivas:0,neutras:0,negativas:0},distribuicao:[],totalFiltrado:0,destaque:[]};
  if(key==='cliente/pedidos'&&options.method==='POST'){const body=JSON.parse(options.body);window.__writes.push(body);data={id:5,cliente:'Teste',status:'RECEBIDO',total:86.9,taxaServico:0,criadoEm:'2026-11-14T15:00:00',atualizadoEm:'2026-11-14T15:00:00',itens:[],etapas:{RECEBIDO:'2026-11-14T15:00:00'},tempoEstimado:15};}
  return {ok:true,status:200,json:async()=>data};
 };
}''',products)
 # The bundled React runtime must be evaluated before app.js attempts to mount it.
 for js in ['client/client.js','js/core.js','js/api.js','js/auth.js','js/components.js','js/legacy.js','js/admin.js','js/app.js']:
  page.add_script_tag(content=(root/js).read_text())
 print('browser diagnostics:',errors,'root:',page.locator('#root').inner_text()[:750])
 page.locator('.c-product').first.wait_for(timeout=2500)
 assert page.locator('.c-product').count()==2
 # One plain size, then one with extra bacon and a separate note.
 page.locator('.c-product .c-small').first.click()
 page.locator('dialog select').first.select_option('11')
 page.locator('dialog button:has-text("Adicionar ao pedido")').click()
 page.locator('.c-product .c-small').first.click()
 page.locator('dialog select').first.select_option('12')
 page.locator('dialog input[type=checkbox]').first.check()
 page.locator('dialog textarea').first.fill('Sem tomate')
 page.locator('dialog button:has-text("Adicionar ao pedido")').click()
 assert page.locator('.c-cart-item').count()==2
 print('Two independent cart rows: PASS')
 page.locator('.c-mobile-cart').click()
 assert page.locator('.c-order-rail.open').count()==1
 print('drawer styles', page.locator('.c-order-rail.open').evaluate('e => {const c=getComputedStyle(e),r=e.getBoundingClientRect();return {top:r.top,left:r.left,width:r.width,height:r.height,position:c.position,z:c.zIndex,bg:c.backgroundColor,color:c.color,scrollY:scrollY}}'))
 print('heading color',page.locator('.c-cart .c-panel-heading h2').evaluate('e=>getComputedStyle(e).color'))
 page.screenshot(path='/mnt/data/astrachefe_carrinho_mobile.png',full_page=False)
 # Remount simulates component initialization, with localStorage preserved.
 page.evaluate('''() => {window.AstraClient.unmount();window.AstraClient.mount({token:'fixture',nome:'Teste',onLogout(){}})}''')
 page.locator('.c-cart-item').first.wait_for(timeout=10000,state='attached')
 assert page.locator('.c-cart-item').count()==2
 print('Local persistence after React remount: PASS')
 page.locator('.c-mobile-cart').click()
 page.locator('.c-primary:has-text("Confirmar Pedido")').first.click()
 page.locator('dialog button:has-text("Confirmar e enviar")').click()
 page.wait_for_timeout(250)
 writes=page.evaluate('window.__writes')
 assert len(writes)==1,writes
 items=writes[0]['itens']
 assert len(items)==2,items
 assert items[0]['variacaoId']==11 and items[1]['variacaoId']==12,items
 assert items[1]['adicionaisIds']==[21],items
 assert items[1]['observacao']=='Sem tomate',items
 print('Checkout payload:',json.dumps(items,ensure_ascii=False))
 print('Browser errors:',errors)
 assert not errors,errors
 browser.close()
print('BROWSER SMOKE PASS (backend mocked, actual F5 unverified)')
