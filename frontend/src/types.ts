export type Recipe={ingredienteId:number;quantidade:number};
export type ProductDetails={sku?:string;setorProducao?:string;porcao?:string;diasSemana?:number[];inicio?:string;fim?:string;permiteObservacoes?:boolean;ordemExibicao?:number;variacoes?:{id:string;nome:string;preco:number;fatorReceita:number}[];adicionais?:{id:string;nome:string;preco:number}[];alergenos?:string[];tags?:string[];calorias?:number;nutricional?:string};
export type Selection={variacaoId?:string;adicionais:string[];observacao?:string};
export type CartLine=Selection&{produtoId:number;quantidade:number};
export type Product={detalhes?:ProductDetails;disponivelAgora?:boolean;vendas30Dias?:number;calor?:number;popular?:boolean;id:number;nome:string;descricao:string;preco:number;precoPromocional?:number|null;categoria?:string;imagemUrl?:string;tempoPreparo?:number;tipo?:string;destaque?:boolean;ativo:boolean};
export type Order={id:number;cliente:string;mesa?:number;status:string;total:number;taxaServico:number;observacao?:string;criadoEm:string;atualizadoEm:string;etapas:Record<string,string>;tempoEstimado:number;itens:{produtoId:number;produto:string;quantidade:number;precoUnitario:number;subtotal:number;imagemUrl?:string}[]};
export type Review={id:number;produto_id:number;pedido_id:number;nota:number;comentario:string;cliente:string;produto:string;imagem_url?:string;criado_em:string};
export type ReviewPage={itens:Review[];resumo:{total:number;media:number;positivas:number;neutras:number;negativas:number};distribuicao:{nota:number;quantidade:number}[];totalFiltrado:number;destaque:{id:number;nome:string;imagem_url?:string;media:number;quantidade:number}[]};
export const money=(v:number)=>new Intl.NumberFormat('pt-BR',{style:'currency',currency:'BRL'}).format(Number(v)||0);
export const normalize=(s:string)=>s.normalize('NFD').replace(/[\u0300-\u036f]/g,'').toLowerCase();
export const price=(p:Product)=>Number(p.precoPromocional??p.preco);
export const image=(url?:string)=>url&&/^\/(media|images)\/[a-zA-Z0-9._-]+$/.test(url)?url:'/images/placeholder.svg';
export const labels:Record<string,string>={RECEBIDO:'Pedido recebido',EM_PREPARO:'Em preparo',PRONTO:'Pronto',ENTREGUE:'Finalizado',CANCELADO:'Cancelado'};
export const dateTime=(v?:string)=>v?new Date(v).toLocaleString('pt-BR',{dateStyle:'short',timeStyle:'short'}):'—';
export function totals(cart:Record<number,number>,products:Product[],service:boolean){const cents=products.reduce((s,p)=>s+Math.round(price(p)*100)*(cart[p.id]||0),0);const fee=service?Math.round(cents*.1):0;return {subtotal:cents/100,fee:fee/100,total:(cents+fee)/100};}

export function unitPrice(p:Product,line:Selection){return Number(p.detalhes?.variacoes?.find(v=>v.id===line.variacaoId)?.preco??price(p))+(p.detalhes?.adicionais||[]).filter(a=>line.adicionais.includes(a.id)).reduce((s,a)=>s+Number(a.preco),0);}
export function validLine(p:Product,line:CartLine){const vs=p.detalhes?.variacoes||[],ads=p.detalhes?.adicionais||[];return p.disponivelAgora!==false&&p.ativo!==false&&(!vs.length?!line.variacaoId:vs.some(v=>v.id===line.variacaoId))&&line.adicionais.every(id=>ads.some(a=>a.id===id))&&(!line.observacao||p.detalhes?.permiteObservacoes!==false);}
export function cartTotals(cart:Record<string,CartLine>,products:Product[],service:boolean){const cents=Object.values(cart).reduce((s,l)=>{const p=products.find(p=>p.id===l.produtoId);return s+(p?Math.round(unitPrice(p,l)*100)*l.quantidade:0)},0);const fee=service?Math.round(cents*.1):0;return {subtotal:cents/100,fee:fee/100,total:(cents+fee)/100};}
export function lineKey(id:number,s:Selection){return JSON.stringify([id,s.variacaoId||'',s.adicionais.slice().sort(),s.observacao?.trim()||'']);}
