package com.biblioteca.exception;

public class LivroJaCadastradoException extends BibliotecaException {
    public LivroJaCadastradoException(String isbn) {
        super("Já existe um livro cadastrado com o ISBN: " + isbn);
    }
}
