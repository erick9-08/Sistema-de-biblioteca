package com.biblioteca.exception;

public class LivroIndisponivelException extends BibliotecaException {
    public LivroIndisponivelException(String titulo) {
        super("O livro \"" + titulo + "\" não está disponível para empréstimo no momento.");
    }
}
