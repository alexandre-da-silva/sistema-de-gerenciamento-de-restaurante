function render() {
  return perfil === "ADMIN" ? renderAdmin() : renderLegacy();
}
login();
