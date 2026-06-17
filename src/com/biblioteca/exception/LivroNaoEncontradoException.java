package com.biblioteca.exception;

public class LivroNaoEncontradoException extends BibliotecaException {
    public LivroNaoEncontradoException(String isbn) {
        super("Livro não encontrado para o ISBN: " + isbn);
    }
}
