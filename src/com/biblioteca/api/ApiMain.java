package com.biblioteca.api;

import com.biblioteca.exception.BibliotecaException;
import com.biblioteca.model.*;
import com.biblioteca.service.BibliotecaService;

import java.io.IOException;

/**
 * Ponto de entrada para o servidor web (modo API REST + frontend).
 * Execute este main em vez de com.biblioteca.main.Main para usar a interface web.
 */
public class ApiMain {

    public static void main(String[] args) throws IOException {
        Biblioteca biblioteca = new Biblioteca("Biblioteca Municipal Central", "Av. Principal, 123");
        BibliotecaService service = new BibliotecaService(biblioteca);

        service.carregarTudo();
        seedDadosIniciais(service);

        ApiServer server = new ApiServer(service);
        server.start();
    }

    private static void seedDadosIniciais(BibliotecaService service) {
        if (service.listarUsuarios().isEmpty()) {
            try {
                service.cadastrarAdministrador("Administrador Geral", "admin", "admin123");
                service.cadastrarUsuario("Maria Silva", "u1", "1234");
                service.cadastrarUsuario("João Souza", "u2", "1234");

                service.cadastrarLivro("Clean Code", "Robert C. Martin", 2008, "9780132350884");
                service.cadastrarLivro("Effective Java", "Joshua Bloch", 2017, "9780134685991");
                service.cadastrarLivro("O Senhor dos Anéis", "J.R.R. Tolkien", 1954, "9788533613379");
                service.cadastrarLivro("1984", "George Orwell", 1949, "9780451524935");
                service.cadastrarLivro("O Hobbit", "J.R.R. Tolkien", 1937, "9788533604278");

                service.salvarTudo();
                System.out.println("Dados iniciais criados. Admin: ID=admin / Senha=admin123");
            } catch (BibliotecaException e) {
                System.err.println("Erro ao criar dados iniciais: " + e.getMessage());
            }
        }
    }
}
