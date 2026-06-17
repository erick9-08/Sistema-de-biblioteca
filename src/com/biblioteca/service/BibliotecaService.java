package com.biblioteca.service;

import com.biblioteca.exception.*;
import com.biblioteca.model.*;
import com.biblioteca.repository.LivroRepository;
import com.biblioteca.repository.UsuarioRepository;
import com.biblioteca.util.ArquivoUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Camada de serviço (faz o papel de "Controller" no padrão MVC):
 * concentra toda a lógica de negócio do sistema — cadastro, autenticação,
 * empréstimo, devolução, buscas, listagens, relatórios e persistência —
 * orquestrando os repositórios e o modelo. O {@code Main} (camada View)
 * nunca acessa os repositórios diretamente, apenas este serviço.
 */
public class BibliotecaService {

    private static final String ARQUIVO_EMPRESTIMOS = "data/emprestimos.txt";
    private static final String SEPARADOR_LEITURA = "\\|";
    private static final String SEPARADOR_ESCRITA = "|";

    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final List<Emprestimo> emprestimos; // ArrayList: preserva a ordem cronológica

    public BibliotecaService(Biblioteca biblioteca) {
        this.livroRepository = biblioteca.getLivroRepository();
        this.usuarioRepository = biblioteca.getUsuarioRepository();
        this.emprestimos = new ArrayList<>();
    }

    // ------------------------- Cadastro -------------------------

    public Livro cadastrarLivro(String titulo, String autor, int ano, String isbn)
            throws LivroJaCadastradoException {
        Livro livro = new Livro(titulo, autor, ano, isbn);
        livroRepository.adicionar(livro);
        return livro;
    }

    public Usuario cadastrarUsuario(String nome, String id, String senha)
            throws UsuarioJaCadastradoException {
        Usuario usuario = new Usuario(id, nome, senha);
        usuarioRepository.adicionarUsuario(usuario);
        return usuario;
    }

    public Administrador cadastrarAdministrador(String nome, String id, String senha)
            throws UsuarioJaCadastradoException {
        Administrador administrador = new Administrador(id, nome, senha);
        usuarioRepository.adicionarAdministrador(administrador);
        return administrador;
    }

    // ------------------------- Autenticação -------------------------

    /**
     * Retorna a {@link Pessoa} autenticada. O tipo de retorno é a
     * superclasse de propósito — quem chama decide o que fazer usando
     * {@code instanceof}/polimorfismo (ver Main), sem o serviço precisar
     * saber qual menu mostrar.
     */
    public Pessoa autenticar(String id, String senha) throws AutenticacaoException {
        return usuarioRepository.autenticar(id, senha);
    }

    // ------------------------- Empréstimo / Devolução -------------------------

    public Emprestimo realizarEmprestimo(String idUsuario, String isbn)
            throws UsuarioNaoEncontradoException, LivroNaoEncontradoException, LivroIndisponivelException {
        Usuario usuario = usuarioRepository.buscarUsuarioPorId(idUsuario);
        Livro livro = livroRepository.buscarPorIsbn(isbn);

        if (!livro.isDisponivel()) {
            throw new LivroIndisponivelException(livro.getTitulo());
        }

        livro.marcarComoEmprestado();
        Emprestimo emprestimo = new Emprestimo(livro, usuario);
        emprestimos.add(emprestimo);
        usuario.adicionarEmprestimo(emprestimo);
        return emprestimo;
    }

    public Emprestimo devolverLivro(String idUsuario, String isbn)
            throws UsuarioNaoEncontradoException, LivroNaoEncontradoException, EmprestimoNaoEncontradoException {
        Usuario usuario = usuarioRepository.buscarUsuarioPorId(idUsuario);
        Livro livro = livroRepository.buscarPorIsbn(isbn);

        Emprestimo emprestimo = emprestimos.stream()
                .filter(e -> e.getStatus() != StatusEmprestimo.DEVOLVIDO)
                .filter(e -> e.getLivro().equals(livro))
                .filter(e -> e.getUsuario().getId().equals(usuario.getId()))
                .findFirst()
                .orElseThrow(() -> new EmprestimoNaoEncontradoException(idUsuario, isbn));

        emprestimo.registrarDevolucao();
        livro.marcarComoDisponivel();
        return emprestimo;
    }

    // ------------------------- Buscas -------------------------

    public List<Livro> buscarPorTitulo(String titulo) {
        return livroRepository.buscarPorTitulo(titulo);
    }

    public List<Livro> buscarPorAutor(String autor) {
        return livroRepository.buscarPorAutor(autor);
    }

    public Livro buscarPorIsbn(String isbn) throws LivroNaoEncontradoException {
        return livroRepository.buscarPorIsbn(isbn);
    }

    // ------------------------- Listagens -------------------------

    public List<Livro> listarDisponiveis() {
        return livroRepository.listarDisponiveis();
    }

    public List<Livro> listarEmprestados() {
        return livroRepository.listarEmprestados();
    }

    public List<Livro> listarTodosLivros() {
        return livroRepository.listarTodos();
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.listarUsuarios();
    }

    public List<Emprestimo> listarEmprestimosAtrasados() {
        return emprestimos.stream().filter(Emprestimo::isAtrasado).collect(Collectors.toList());
    }

    // ------------------------- Relatórios -------------------------

    /** Relatório dos livros mais emprestados, ordenado de forma decrescente. */
    public List<Livro> relatorioLivrosMaisEmprestados(int top) {
        return livroRepository.listarTodos().stream()
                .sorted(Comparator.comparingInt(Livro::getQuantidadeEmprestimos).reversed())
                .limit(top)
                .collect(Collectors.toList());
    }

    // ------------------------- Persistência -------------------------

    public void salvarTudo() {
        livroRepository.salvar();
        usuarioRepository.salvar();
        salvarEmprestimos();
    }

    public void carregarTudo() {
        livroRepository.carregar();
        usuarioRepository.carregar();
        carregarEmprestimos();
    }

    /** Formato: isbn|idUsuario|dataEmprestimo|dataDevolucaoPrevista|dataDevolucaoReal|status */
    private void salvarEmprestimos() {
        List<String> linhas = new ArrayList<>();
        for (Emprestimo e : emprestimos) {
            linhas.add(String.join(SEPARADOR_ESCRITA,
                    e.getLivro().getIsbn(), e.getUsuario().getId(),
                    e.getDataEmprestimo().toString(), e.getDataDevolucaoPrevista().toString(),
                    e.getDataDevolucaoReal() == null ? "null" : e.getDataDevolucaoReal().toString(),
                    e.getStatus().name()));
        }
        try {
            ArquivoUtil.escreverLinhas(ARQUIVO_EMPRESTIMOS, linhas);
        } catch (IOException ex) {
            System.err.println("Erro ao salvar empréstimos: " + ex.getMessage());
        }
    }

    private void carregarEmprestimos() {
        try {
            List<String> linhas = ArquivoUtil.lerLinhas(ARQUIVO_EMPRESTIMOS);
            for (String linha : linhas) {
                String[] c = linha.split(SEPARADOR_LEITURA);
                if (c.length < 6) continue;
                try {
                    Livro livro = livroRepository.buscarPorIsbn(c[0]);
                    Usuario usuario = usuarioRepository.buscarUsuarioPorId(c[1]);
                    LocalDate dataEmprestimo = LocalDate.parse(c[2]);
                    LocalDate dataPrevista = LocalDate.parse(c[3]);
                    LocalDate dataReal = c[4].equals("null") ? null : LocalDate.parse(c[4]);
                    StatusEmprestimo status = StatusEmprestimo.valueOf(c[5]);

                    Emprestimo emprestimo = new Emprestimo(livro, usuario, dataEmprestimo, dataPrevista, dataReal, status);
                    emprestimos.add(emprestimo);
                    usuario.adicionarEmprestimo(emprestimo);
                } catch (LivroNaoEncontradoException | UsuarioNaoEncontradoException ex) {
                    System.err.println("Inconsistência ao recarregar empréstimo: " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            System.err.println("Erro ao carregar empréstimos: " + ex.getMessage());
        }
    }
}
