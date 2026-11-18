function salvarSessao() {
  try { sessionStorage.setItem('astrachefe:sessao:v1',JSON.stringify({token,nome:nomeUsuario,perfil,page})); } catch {}
}
function limparSessao() {
  token=''; perfil=''; nomeUsuario=''; page='';
  try { sessionStorage.removeItem('astrachefe:sessao:v1'); } catch {}
  if(typeof pararAtualizacaoAdmin==='function') pararAtualizacaoAdmin();
  if(typeof pararAtualizacaoCozinha==='function') pararAtualizacaoCozinha();
}
async function restaurarSessao() {
  let dados;
  try { dados=JSON.parse(sessionStorage.getItem('astrachefe:sessao:v1')||'null'); } catch {}
  if(!dados?.token || !['ADMIN','CLIENTE','COZINHA'].includes(dados.perfil)) { login(); return; }
  token=dados.token; perfil=dados.perfil; nomeUsuario=dados.nome||'';
  page=dados.page|| (perfil==='ADMIN'?'Dashboard':perfil==='COZINHA'?'Cozinha':'Início');
  try {
    const atual=await api('auth/me');
    if(atual.perfil!==perfil){limparSessao();login();return;}
    nomeUsuario=atual.nome;
    salvarSessao();
    render();
  }catch(error) {
    if(error.status===401){limparSessao();login();return;}
    $('root').innerHTML='<div class="auth-page"><section class="login"><h1>Conexão interrompida</h1><p role="alert">Não foi possível recuperar sua sessão agora. Seu acesso foi preservado.</p><button id="retry-session" class="action">Tentar novamente</button><button id="leave-session" class="ghost">Sair desta conta</button></section></div>';
    $('retry-session').onclick=restaurarSessao;
    $('leave-session').onclick=()=>{revogarSessaoNoServidor();limparSessao();login();};
  }
}
function iniciarSessao(response) {
  token = response.accessToken;
  nomeUsuario = response.nome || "Administrador";
  perfil = response.perfil;
  page = perfil === "ADMIN" ? "Dashboard" : perfil === "COZINHA" ? "Cozinha" : "Início";
  salvarSessao();
  render();
}

function login() {
  if ($("modal")?.open) $("modal").close();
  $("root").innerHTML = `
        <div class="auth-page"><section class="login">
            
            <h1>AstraChefe</h1>
            
            <form id="login">
                <label><span class="sr-only">E-mail</span><input placeholder="E-mail" name="email" type="email" required maxlength="180" autocomplete="username"></label>
                <label><span class="sr-only">Senha</span><input placeholder="Senha" name="senha" type="password" required autocomplete="current-password"></label>
                <p id="error" role="alert"></p>
                <button class="action">Entrar</button>
            </form>
        </section>
        <div class="auth-switch">Não tem uma conta? <button id="register" type="button">Cadastre-se</button></div></div>`;
  $("register").onclick = cadastro;
  $("login").onsubmit = async (event) => {
    event.preventDefault();
    const button = event.submitter;
    button.disabled = true;
    $("error").textContent = "";
    try {
      const data = Object.fromEntries(new FormData(event.target));
      iniciarSessao(await api("auth/login", "POST", data));
    } catch (error) {
      $("error").textContent = error.message;
    } finally {
      button.disabled = false;
    }
  };
}

function cadastro() {
  $("root").innerHTML = `
        <div class="auth-page"><section class="login">
            
            <h1>Crie sua conta AstraChefe</h1>
            
            <form id="cadastro">
                <label><span class="sr-only">Nome</span><input placeholder="Nome" name="nome" required maxlength="120" autocomplete="name"></label>
                <label><span class="sr-only">E-mail</span><input placeholder="E-mail" name="email" type="email" required maxlength="180" autocomplete="email"></label>
                <label><span class="sr-only">Senha</span><input placeholder="Senha" name="senha" type="password" required minlength="10" maxlength="72" autocomplete="new-password" aria-describedby="senha-ajuda"></label>
                <small id="senha-ajuda">Use pelo menos 10 caracteres.</small>
                <label><span class="sr-only">Confirmar senha</span><input placeholder="Confirmar senha" name="confirmacao" type="password" required minlength="10" maxlength="72" autocomplete="new-password"></label>
                <p id="error" role="alert"></p>
                <button class="action">Criar minha conta</button>
            </form>
        </section>
        <div class="auth-switch">Já tem uma conta? <button id="back-login" type="button">Entrar</button></div></div>`;
  $("back-login").onclick = login;
  $("cadastro").onsubmit = async (event) => {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(event.target));
    const errorElement = $("error");
    errorElement.textContent = "";
    if (data.senha !== data.confirmacao) {
      errorElement.textContent = "As senhas precisam ser iguais.";
      return;
    }
    if (!data.nome.trim()) {
      errorElement.textContent = "Informe seu nome.";
      return;
    }
    if (new TextEncoder().encode(data.senha).length > 72) {
      errorElement.textContent =
        "A senha ultrapassa o limite permitido. Use menos caracteres.";
      return;
    }
    const button = event.submitter;
    button.disabled = true;
    try {
      const response = await api("auth/registrar", "POST", {
        nome: data.nome.trim(),
        email: data.email.trim(),
        senha: data.senha,
      });
      iniciarSessao(response);
    } catch (error) {
      errorElement.textContent = error.message;
    } finally {
      button.disabled = false;
    }
  };
}
