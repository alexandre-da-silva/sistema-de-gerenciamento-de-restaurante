export type Product={id:number;nome:string;descricao:string;preco:number;precoPromocional?:number|null;categoria?:string;imagemUrl?:string;tempoPreparo?:number;tipo?:string;destaque?:boolean;ativo:boolean};
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
