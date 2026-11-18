function render() {
 if(window.AstraClient)window.AstraClient.unmount();
 return perfil === "ADMIN" ? renderAdmin() : perfil === "CLIENTE" ? window.AstraClient.mount({token,nome:nomeUsuario,onLogout:()=>{revogarSessaoNoServidor();limparSessao();window.AstraClient.unmount();login();}}) : renderLegacy();
}
restaurarSessao();
