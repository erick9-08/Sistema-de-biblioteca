package com.biblioteca.exception;

public class UsuarioJaCadastradoException extends BibliotecaException {
    public UsuarioJaCadastradoException(String id) {
        super("Já existe um usuário cadastrado com o ID: " + id);
    }
}
