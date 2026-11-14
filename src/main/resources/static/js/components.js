function form(title, fields, save) {
  const submit = $("form").querySelector("button.action");
  submit.hidden = false;
  submit.textContent = "Salvar";
  $("close").textContent = "Cancelar";
  $("formTitle").textContent = title;
  $("formError").textContent = "";
  $("fields").innerHTML = fields
    .map(
      ([name, label, type = "text", opts = []]) =>
        "<label>" +
        esc(label) +
        (type === "select"
          ? '<select name="' +
            name +
            '">' +
            opts
              .map(
                ([v, t]) =>
                  '<option value="' + esc(v) + '">' + esc(t) + "</option>",
              )
              .join("") +
            "</select>"
          : '<input name="' +
            name +
            '" type="' +
            type +
            '" required ' +
            (type === "number" ? 'step="0.001"' : "") +
            ">") +
        "</label>",
    )
    .join("");
  $("modal").showModal();
  $("form").onsubmit = async (e) => {
    e.preventDefault();
    e.submitter.disabled = true;
    try {
      await save(Object.fromEntries(new FormData(e.target)));
      $("modal").close();
      render();
    } catch (err) {
      $("formError").textContent = err.message;
    } finally {
      e.submitter.disabled = false;
    }
  };
}
$("close").onclick = () => $("modal").close();
