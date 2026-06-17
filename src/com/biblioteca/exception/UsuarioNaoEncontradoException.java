package com.biblioteca.exception;

public class UsuarioNaoEncontradoException extends BibliotecaException {
    public UsuarioNaoEncontradoException(String id) {
        super("Usuário não encontrado para o ID: " + id);
    }
}
