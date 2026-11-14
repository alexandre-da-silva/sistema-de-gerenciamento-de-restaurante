function render() {
 if(window.AstraClient)window.AstraClient.unmount();
 if(typeof salvarPagina==='function')salvarPagina();
 return perfil === "ADMIN" ? renderAdmin() : perfil === "CLIENTE" ? window.AstraClient.mount({token,nome:nomeUsuario,initialPage:page,onNavigate:p=>{page=p;salvarPagina();},onLogout:encerrarSessao}) : renderLegacy();
}
window.addEventListener('DOMContentLoaded',restaurarSessao);
setInterval(()=>{if(!token||document.visibilityState!=='visible'||$('modal')?.open||document.querySelector('.admin-app.menu-open'))return;
 if(perfil==='ADMIN'&&!['INPUT','TEXTAREA','SELECT'].includes(document.activeElement?.tagName))renderAdmin();
 else if(perfil==='COZINHA')renderLegacy();
},10000);
