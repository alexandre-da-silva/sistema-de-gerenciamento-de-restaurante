"use strict";
const $ = (id) => document.getElementById(id);
let nomeUsuario = "";
let token = "",
  perfil = "",
  page = "";
const esc = (v) =>
  String(v ?? "").replace(
    /[&<>"']/g,
    (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[
        c
      ],
  );

const sessionStore={get(key){try{return sessionStorage.getItem('astra:'+key)}catch{return null}},set(key,value){try{sessionStorage.setItem('astra:'+key,value)}catch{}},remove(key){try{sessionStorage.removeItem('astra:'+key)}catch{}}};
function encerrarSessao(){sessionStore.remove('sessao');sessionStore.remove('pagina');token='';perfil='';nomeUsuario='';window.AstraClient?.unmount();login();}
function salvarSessao(){sessionStore.set('sessao',JSON.stringify({token,perfil,nome:nomeUsuario}));}
function salvarPagina(){sessionStore.set('pagina',JSON.stringify({perfil,page}));}
