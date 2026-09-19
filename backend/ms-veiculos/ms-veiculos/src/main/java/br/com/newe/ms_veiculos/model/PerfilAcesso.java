package br.com.newe.ms_veiculos.model;

public enum PerfilAcesso {

    GESTOR("gestor"),
    OPERADOR("operador");

    private final String role;

    PerfilAcesso(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
