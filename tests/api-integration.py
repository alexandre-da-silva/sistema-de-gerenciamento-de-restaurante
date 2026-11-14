"""Runs only against an isolated development database, never production.
The CI workflow provides an empty MySQL service and synthetic credentials.
"""
import json, uuid, urllib.request, urllib.error, concurrent.futures
BASE='http://localhost:8080/api/v1/'
def call(path,method='GET',data=None,token=None,status=200):
    headers={'Content-Type':'application/json'}
    if token: headers['Authorization']='Bearer '+token
    request=urllib.request.Request(BASE+path,data=None if data is None else json.dumps(data).encode(),headers=headers,method=method)
    try:
        with urllib.request.urlopen(request) as response: code=response.status; raw=response.read()
    except urllib.error.HTTPError as error: code=error.code; raw=error.read()
    assert code==status,(path,code,raw.decode()[:1000])
    return json.loads(raw) if raw else None
admin=call('auth/login','POST',{'email':'admin@example.com','senha':'integration-admin-password'})['accessToken']
client=call('auth/registrar','POST',{'nome':'Integration Client','email':'client@example.com','senha':'integration-client-password'})['accessToken']
assert call('auth/sessao',token=client)['perfil']=='CLIENTE'
for module in ['CONTROLE_ESTOQUE','CAIXA_E_PAGAMENTOS']:
    call('admin/recursos/'+module,'PUT',{'habilitado':True},admin)
call('admin/insumos','POST',{'nome':'Insumo de teste','unidade':'kg','custoUnitario':30},admin)
ing=call('admin/insumos',token=admin)[0]['id']
call(f'admin/estoque/{ing}/movimentos','POST',{'quantidade':10,'motivo':'Saldo de integração'},admin)
recipe=[{'ingredienteId':ing,'quantidade':.25}]
body={'nome':'Prato de teste','descricao':'Produto de teste automatizado','preco':40,'ativo':True,'tempoPreparo':15,'detalhes':{'sku':'TEST-001','setorProducao':'Bar','porcao':'Individual','diasSemana':[],'controlaEstoque':True,'permiteObservacoes':True,'ordemExibicao':1,'fichaTecnica':recipe,'variacoes':[{'id':'large','nome':'Grande','preco':50,'fatorReceita':2}],'adicionais':[{'id':'extra','nome':'Extra','preco':5,'insumos':[{'ingredienteId':ing,'quantidade':.1}]}],'alergenos':['lactose'],'tags':[]}}
product=call('admin/produtos','POST',body,admin,status=201)
assert float(product['custo'])==7.5
pid=product['id']
def order(q=1):return {'itens':[{'produtoId':pid,'quantidade':q,'variacaoId':'large','adicionais':['extra'],'observacao':'Ao ponto'}],'taxaServico':False,'referencia':str(uuid.uuid4())}
call('cliente/pedidos','POST',order(),admin,status=403)
first=order(2)
created=call('cliente/pedidos','POST',first,client,status=201)
assert float(created['total'])==110
assert float(call('admin/insumos',token=admin)[0]['quantidade_atual'])==8.8
repeated=call('cliente/pedidos','POST',first,client,status=201)
assert repeated['id']==created['id']
assert float(call('admin/insumos',token=admin)[0]['quantidade_atual'])==8.8
call(f"cliente/pedidos/{created['id']}/cancelar",'PATCH',token=client)
assert float(call('admin/insumos',token=admin)[0]['quantidade_atual'])==10
call(f"cliente/pedidos/{created['id']}/cancelar",'PATCH',token=client,status=409)
second=call('cliente/pedidos','POST',order(6),client,status=201)
for state in ['EM_PREPARO','PRONTO','ENTREGUE']:
    call(f"cozinha/pedidos/{second['id']}/status",'PATCH',{'status':state},admin)
call(f"admin/pedidos/{second['id']}/pagamentos",'POST',{'forma':'PIX','referencia':str(uuid.uuid4())},admin)
rec=next(p for p in call('publico/recomendacoes') if p['id']==pid)
assert rec['popular'] and rec['vendas30Dias']==6 and rec['calor']==1
assert rec['custo'] is None and rec['detalhes']['fichaTecnica']==[] and rec['detalhes']['fiscal'] is None
# Two simultaneous orders compete for one serving; the loser must not overdraw.
call(f'admin/estoque/{ing}/movimentos','POST',{'quantidade':-5.4,'motivo':'Preparar concorrência'},admin)
def race(_):
    try: return call('cliente/pedidos','POST',order(),client,status=201)
    except AssertionError as e:
        assert e.args[0][1]==409,e
        return None
with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool: results=list(pool.map(race,range(2)))
assert len([r for r in results if r])==1
assert float(call('admin/insumos',token=admin)[0]['quantidade_atual'])==.4
print('PASS: migrations, admin restriction, options/prices, recipe costs, idempotency, cancellation, popularity, concurrent stock.')
