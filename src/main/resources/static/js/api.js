async function api(path, method = "GET", body) {
  const headers = { Accept: "application/json" };
  const publicAuth = path === "auth/login" || path === "auth/registrar";
  if (token && !publicAuth) headers.Authorization = "Bearer " + token;
  if (body !== undefined) headers["Content-Type"] = "application/json";
  const response = await fetch("/api/v1/" + path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    if (response.status === 401 && !publicAuth) {
      token = "";
      perfil = "";
      login();
      $("error").textContent = "Sua sessão expirou. Entre novamente.";
    }
    const message =
      response.status === 401
        ? publicAuth
          ? "E-mail ou senha inválidos. Confira seus dados ou crie uma conta."
          : "Entre novamente para continuar."
        : "Não foi possível concluir a solicitação (" + response.status + ").";
    throw Error(data?.message || message);
  }
  return data;
}
