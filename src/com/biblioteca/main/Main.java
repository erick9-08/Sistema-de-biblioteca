package com.biblioteca.main;

import com.biblioteca.exception.*;
import com.biblioteca.model.*;
import com.biblioteca.service.BibliotecaService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Ponto de entrada do sistema (camada View no padrão MVC).
 * Responsável apenas por exibir menus, ler a entrada do usuário e
 * delegar toda a lógica de negócio para {@link BibliotecaService}.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static BibliotecaService service;

    public static void main(String[] args) {
        Biblioteca biblioteca = new Biblioteca("Biblioteca Municipal Central", "Av. Principal, 123");
        service = new BibliotecaService(biblioteca);

        service.carregarTudo();
        seedDadosIniciais(); // cria dados de exemplo apenas se ainda não houver nenhum

        System.out.println("=========================================");
        System.out.println(" SISTEMA DE GERENCIAMENTO DE BIBLIOTECA");
        System.out.println("=========================================");

        boolean continuarExecutando = true;
        while (continuarExecutando) {
            continuarExecutando = telaLogin();
        }

        service.salvarTudo();
        System.out.println("\nDados salvos. Até logo!");
    }

    /** Cria um administrador padrão e alguns dados de exemplo apenas na primeira execução. */
    private static void seedDadosIniciais() {
        if (service.listarUsuarios().isEmpty()) {
            try {
                service.cadastrarAdministrador("Administrador Geral", "admin", "admin123");
                service.cadastrarUsuario("Maria Silva", "u1", "1234");
                service.cadastrarUsuario("João Souza", "u2", "1234");

                service.cadastrarLivro("Clean Code", "Robert C. Martin", 2008, "9780132350884");
                service.cadastrarLivro("Effective Java", "Joshua Bloch", 2017, "9780134685991");
                service.cadastrarLivro("O Senhor dos Anéis", "J.R.R. Tolkien", 1954, "9788533613379");
                service.cadastrarLivro("1984", "George Orwell", 1949, "9780451524935");

                System.out.println("(Primeira execução detectada: dados de exemplo criados.)");
                System.out.println("(Login do administrador padrão -> ID: admin | Senha: admin123)");
            } catch (BibliotecaException e) {
                System.err.println("Erro ao criar dados iniciais: " + e.getMessage());
            }
        }
    }

    // ------------------------- LOGIN -------------------------

    private static boolean telaLogin() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Senha: ");
        String senha = scanner.nextLine().trim();

        try {
            Pessoa pessoa = service.autenticar(id, senha);
            System.out.println("\nBem-vindo(a), " + pessoa.getNome() + "! [" + pessoa.getTipo() + "]");

            // Polimorfismo: o menu exibido depende do tipo REAL do objeto autenticado
            if (pessoa instanceof Administrador) {
                menuAdministrador((Administrador) pessoa);
            } else if (pessoa instanceof Usuario) {
                menuUsuario((Usuario) pessoa);
            }
        } catch (AutenticacaoException e) {
            System.out.println("Erro: " + e.getMessage());
            System.out.print("Tentar novamente? (s/n): ");
            return scanner.nextLine().trim().equalsIgnoreCase("s");
        }
        return perguntarNovoLogin();
    }

    private static boolean perguntarNovoLogin() {
        System.out.print("\nFazer login com outra conta? (s/n): ");
        return scanner.nextLine().trim().equalsIgnoreCase("s");
    }

    // ------------------------- MENU ADMINISTRADOR -------------------------

    private static void menuAdministrador(Administrador admin) {
        boolean sair = false;
        while (!sair) {
            System.out.println("\n----- MENU ADMINISTRADOR -----");
            System.out.println("1. Cadastrar livro");
            System.out.println("2. Cadastrar usuário");
            System.out.println("3. Buscar livro (título/autor/ISBN)");
            System.out.println("4. Listar livros disponíveis");
            System.out.println("5. Listar livros emprestados");
            System.out.println("6. Listar usuários cadastrados");
            System.out.println("7. Relatório: livros mais emprestados");
            System.out.println("8. Listar empréstimos atrasados");
            System.out.println("9. Realizar empréstimo (em nome de um usuário)");
            System.out.println("10. Registrar devolução");
            System.out.println("0. Logout");
            System.out.print("Escolha uma opção: ");

            String opcao = scanner.nextLine().trim();
            try {
                switch (opcao) {
                    case "1": cadastrarLivro(); break;
                    case "2": cadastrarUsuario(); break;
                    case "3": buscarLivro(); break;
                    case "4": listarLivros(service.listarDisponiveis(), "LIVROS DISPONÍVEIS"); break;
                    case "5": listarLivros(service.listarEmprestados(), "LIVROS EMPRESTADOS"); break;
                    case "6": listarUsuarios(); break;
                    case "7": relatorioMaisEmprestados(); break;
                    case "8": listarAtrasados(); break;
                    case "9": realizarEmprestimo(); break;
                    case "10": registrarDevolucao(); break;
                    case "0": sair = true; break;
                    default: System.out.println("Opção inválida.");
                }
            } catch (BibliotecaException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

    // ------------------------- MENU USUÁRIO -------------------------

    private static void menuUsuario(Usuario usuario) {
        boolean sair = false;
        while (!sair) {
            System.out.println("\n----- MENU USUÁRIO -----");
            System.out.println("1. Buscar livro (título/autor/ISBN)");
            System.out.println("2. Listar livros disponíveis");
            System.out.println("3. Pegar livro emprestado");
            System.out.println("4. Devolver livro");
            System.out.println("5. Meu histórico de empréstimos");
            System.out.println("0. Logout");
            System.out.print("Escolha uma opção: ");

            String opcao = scanner.nextLine().trim();
            try {
                switch (opcao) {
                    case "1":
                        buscarLivro();
                        break;
                    case "2":
                        listarLivros(service.listarDisponiveis(), "LIVROS DISPONÍVEIS");
                        break;
                    case "3": {
                        System.out.print("ISBN do livro desejado: ");
                        String isbn = scanner.nextLine().trim();
                        Emprestimo emp = service.realizarEmprestimo(usuario.getId(), isbn);
                        System.out.println("Empréstimo realizado! Devolução prevista para: " + emp.getDataDevolucaoPrevista());
                        break;
                    }
                    case "4": {
                        System.out.print("ISBN do livro a devolver: ");
                        String isbnDevolucao = scanner.nextLine().trim();
                        service.devolverLivro(usuario.getId(), isbnDevolucao);
                        System.out.println("Devolução registrada com sucesso!");
                        break;
                    }
                    case "5":
                        historicoUsuario(usuario);
                        break;
                    case "0":
                        sair = true;
                        break;
                    default:
                        System.out.println("Opção inválida.");
                }
            } catch (BibliotecaException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

    // ------------------------- AÇÕES AUXILIARES -------------------------

    private static void cadastrarLivro() throws LivroJaCadastradoException {
        System.out.print("Título: ");
        String titulo = scanner.nextLine().trim();
        System.out.print("Autor: ");
        String autor = scanner.nextLine().trim();
        System.out.print("Ano de publicação: ");
        int ano = lerInteiro();
        System.out.print("ISBN: ");
        String isbn = scanner.nextLine().trim();

        service.cadastrarLivro(titulo, autor, ano, isbn);
        System.out.println("Livro cadastrado com sucesso!");
    }

    private static void cadastrarUsuario() throws UsuarioJaCadastradoException {
        System.out.print("Nome: ");
        String nome = scanner.nextLine().trim();
        System.out.print("ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Senha: ");
        String senha = scanner.nextLine().trim();

        service.cadastrarUsuario(nome, id, senha);
        System.out.println("Usuário cadastrado com sucesso!");
    }

    private static void buscarLivro() {
        System.out.println("Buscar por: 1) Título  2) Autor  3) ISBN");
        String escolha = scanner.nextLine().trim();
        System.out.print("Termo de busca: ");
        String termo = scanner.nextLine().trim();

        List<Livro> resultado = new ArrayList<>();
        switch (escolha) {
            case "1":
                resultado = service.buscarPorTitulo(termo);
                break;
            case "2":
                resultado = service.buscarPorAutor(termo);
                break;
            case "3":
                try {
                    resultado.add(service.buscarPorIsbn(termo));
                } catch (LivroNaoEncontradoException e) {
                    System.out.println("Erro: " + e.getMessage());
                    return;
                }
                break;
            default:
                System.out.println("Opção inválida.");
                return;
        }
        listarLivros(resultado, "RESULTADO DA BUSCA");
    }

    private static void realizarEmprestimo()
            throws UsuarioNaoEncontradoException, LivroNaoEncontradoException, LivroIndisponivelException {
        System.out.print("ID do usuário: ");
        String idUsuario = scanner.nextLine().trim();
        System.out.print("ISBN do livro: ");
        String isbn = scanner.nextLine().trim();
        Emprestimo emp = service.realizarEmprestimo(idUsuario, isbn);
        System.out.println("Empréstimo registrado. Devolução prevista: " + emp.getDataDevolucaoPrevista());
    }

    private static void registrarDevolucao()
            throws UsuarioNaoEncontradoException, LivroNaoEncontradoException, EmprestimoNaoEncontradoException {
        System.out.print("ID do usuário: ");
        String idUsuario = scanner.nextLine().trim();
        System.out.print("ISBN do livro: ");
        String isbn = scanner.nextLine().trim();
        service.devolverLivro(idUsuario, isbn);
        System.out.println("Devolução registrada com sucesso!");
    }

    private static void listarLivros(List<Livro> livros, String titulo) {
        System.out.println("\n--- " + titulo + " (" + livros.size() + ") ---");
        if (livros.isEmpty()) {
            System.out.println("Nenhum livro encontrado.");
        }
        for (Livro l : livros) {
            System.out.println(l);
        }
    }

    private static void listarUsuarios() {
        List<Usuario> usuarios = service.listarUsuarios();
        System.out.println("\n--- USUÁRIOS CADASTRADOS (" + usuarios.size() + ") ---");
        for (Usuario u : usuarios) {
            System.out.println(u + " | Empréstimos ativos: " + u.quantidadeEmprestimosAtivos());
        }
    }

    private static void relatorioMaisEmprestados() {
        System.out.println("\n--- TOP 5 LIVROS MAIS EMPRESTADOS ---");
        List<Livro> top = service.relatorioLivrosMaisEmprestados(5);
        int posicao = 1;
        for (Livro l : top) {
            System.out.println(posicao++ + "º - " + l.getTitulo() + " (" + l.getQuantidadeEmprestimos() + " empréstimo(s))");
        }
    }

    private static void listarAtrasados() {
        List<Emprestimo> atrasados = service.listarEmprestimosAtrasados();
        System.out.println("\n--- EMPRÉSTIMOS ATRASADOS (" + atrasados.size() + ") ---");
        for (Emprestimo e : atrasados) {
            System.out.println(e + " | Dias de atraso: " + e.diasDeAtraso());
        }
    }

    private static void historicoUsuario(Usuario usuario) {
        System.out.println("\n--- MEU HISTÓRICO DE EMPRÉSTIMOS ---");
        if (usuario.getHistoricoEmprestimos().isEmpty()) {
            System.out.println("Você ainda não realizou nenhum empréstimo.");
        }
        for (Emprestimo e : usuario.getHistoricoEmprestimos()) {
            System.out.println(e);
        }
    }

    private static int lerInteiro() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Valor inválido. Digite um número: ");
            }
        }
    }
}
