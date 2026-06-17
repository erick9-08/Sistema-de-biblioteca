package com.biblioteca.model;

import java.util.Objects;

/**
 * Representa um livro do acervo da biblioteca.
 * Todos os atributos são privados (encapsulamento); a disponibilidade
 * só pode ser alterada através dos métodos de domínio
 * {@link #marcarComoEmprestado()} e {@link #marcarComoDisponivel()},
 * o que evita que outra camada deixe o objeto em um estado inconsistente.
 */
public class Livro {

    private String titulo;
    private String autor;
    private int anoPublicacao;
    private final String isbn;
    private boolean disponivel;
    private int quantidadeEmprestimos; // usado no relatório de livros mais emprestados

    /** Construtor para cadastro de um livro novo (sempre disponível). */
    public Livro(String titulo, String autor, int anoPublicacao, String isbn) {
        this.titulo = titulo;
        this.autor = autor;
        this.anoPublicacao = anoPublicacao;
        this.isbn = isbn;
        this.disponivel = true;
        this.quantidadeEmprestimos = 0;
    }

    /** Construtor completo, usado ao reconstruir o objeto a partir do arquivo de dados. */
    public Livro(String titulo, String autor, int anoPublicacao, String isbn,
                 boolean disponivel, int quantidadeEmprestimos) {
        this.titulo = titulo;
        this.autor = autor;
        this.anoPublicacao = anoPublicacao;
        this.isbn = isbn;
        this.disponivel = disponivel;
        this.quantidadeEmprestimos = quantidadeEmprestimos;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public int getAnoPublicacao() {
        return anoPublicacao;
    }

    public void setAnoPublicacao(int anoPublicacao) {
        this.anoPublicacao = anoPublicacao;
    }

    public String getIsbn() {
        return isbn;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public int getQuantidadeEmprestimos() {
        return quantidadeEmprestimos;
    }

    public void marcarComoEmprestado() {
        this.disponivel = false;
        this.quantidadeEmprestimos++;
    }

    public void marcarComoDisponivel() {
        this.disponivel = true;
    }

    // Dois livros são "iguais" se têm o mesmo ISBN, que é o identificador
    // natural de um livro no mundo real.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Livro)) return false;
        Livro livro = (Livro) o;
        return isbn.equals(livro.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    @Override
    public String toString() {
        return String.format("%-35s | %-20s | %d | ISBN: %-13s | %s",
                titulo, autor, anoPublicacao, isbn,
                disponivel ? "DISPONÍVEL" : "EMPRESTADO");
    }
}
