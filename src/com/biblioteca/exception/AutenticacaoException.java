package com.biblioteca.exception;

public class AutenticacaoException extends BibliotecaException {
    public AutenticacaoException() {
        super("ID ou senha inválidos.");
    }
}
