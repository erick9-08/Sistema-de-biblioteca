package com.biblioteca.exception;

public class EmprestimoNaoEncontradoException extends BibliotecaException {
    public EmprestimoNaoEncontradoException(String idUsuario, String isbn) {
        super("Não foi encontrado um empréstimo em andamento para o usuário '" + idUsuario
                + "' com o livro de ISBN " + isbn);
    }
}
