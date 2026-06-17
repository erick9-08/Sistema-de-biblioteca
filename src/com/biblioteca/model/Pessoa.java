package com.biblioteca.model;

/**
 * Classe abstrata que representa qualquer pessoa cadastrada no sistema.
 * É a superclasse de {@link Usuario} e {@link Administrador}, concentrando
 * os atributos e comportamentos comuns (encapsulamento) e definindo um
 * método abstrato que cada subclasse deve implementar de forma própria
 * (polimorfismo).
 */
public abstract class Pessoa {

    // Atributos protegidos: visíveis às subclasses, mas não ao mundo externo
    protected String id;
    protected String nome;
    protected String senha;

    protected Pessoa(String id, String nome, String senha) {
        this.id = id;
        this.nome = nome;
        this.senha = senha;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public boolean verificarSenha(String senhaDigitada) {
        return this.senha.equals(senhaDigitada);
    }

    /**
     * Método abstrato: cada subclasse (Usuario, Administrador) devolve seu
     * próprio "tipo". É o ponto-chave de polimorfismo usado no login:
     * o sistema decide qual menu mostrar com base no tipo real do objeto,
     * sem precisar de cadeias de "if instanceof" espalhadas pelo código.
     */
    public abstract String getTipo();

    @Override
    public String toString() {
        return String.format("[%s] ID: %s | Nome: %s", getTipo(), id, nome);
    }
}
