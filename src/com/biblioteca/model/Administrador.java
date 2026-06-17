package com.biblioteca.model;

/**
 * Representa um administrador do sistema, com permissões de gestão
 * (cadastro de livros/usuários, relatórios, etc.).
 * Também herda de {@link Pessoa}, demonstrando que duas classes distintas
 * podem compartilhar a mesma base e, ainda assim, ter comportamentos
 * próprios (polimorfismo) através de {@code getTipo()}.
 */
public class Administrador extends Pessoa {

    private String nivelAcesso;

    public Administrador(String id, String nome, String senha) {
        super(id, nome, senha);
        this.nivelAcesso = "TOTAL";
    }

    public Administrador(String id, String nome, String senha, String nivelAcesso) {
        super(id, nome, senha);
        this.nivelAcesso = nivelAcesso;
    }

    public String getNivelAcesso() {
        return nivelAcesso;
    }

    public void setNivelAcesso(String nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }

    @Override
    public String getTipo() {
        return "ADMINISTRADOR";
    }
}
