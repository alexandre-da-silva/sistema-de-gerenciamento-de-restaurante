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
