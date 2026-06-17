package com.biblioteca.repository;

import com.biblioteca.exception.LivroJaCadastradoException;
import com.biblioteca.exception.LivroNaoEncontradoException;
import com.biblioteca.model.Livro;
import com.biblioteca.util.ArquivoUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Camada de persistência dos livros. Usa {@code HashMap<String, Livro>}
 * com o ISBN como chave, garantindo busca em tempo O(1) — afinal o ISBN
 * é o identificador único e natural de um livro. As buscas por título e
 * autor, por não terem chave direta, percorrem os valores do mapa.
 */
public class LivroRepository {

    private static final String ARQUIVO_DADOS = "data/livros.txt";
    private static final String SEPARADOR_LEITURA = "\\|";
    private static final String SEPARADOR_ESCRITA = "|";

    private final Map<String, Livro> livros = new HashMap<>();

    public void adicionar(Livro livro) throws LivroJaCadastradoException {
        if (livros.containsKey(livro.getIsbn())) {
            throw new LivroJaCadastradoException(livro.getIsbn());
        }
        livros.put(livro.getIsbn(), livro);
    }

    public void remover(String isbn) throws LivroNaoEncontradoException {
        if (!livros.containsKey(isbn)) {
            throw new LivroNaoEncontradoException(isbn);
        }
        livros.remove(isbn);
    }

    public Livro buscarPorIsbn(String isbn) throws LivroNaoEncontradoException {
        Livro livro = livros.get(isbn);
        if (livro == null) {
            throw new LivroNaoEncontradoException(isbn);
        }
        return livro;
    }

    public boolean existePorIsbn(String isbn) {
        return livros.containsKey(isbn);
    }

    public List<Livro> buscarPorTitulo(String trecho) {
        List<Livro> resultado = new ArrayList<>();
        String busca = trecho.toLowerCase();
        for (Livro l : livros.values()) {
            if (l.getTitulo().toLowerCase().contains(busca)) {
                resultado.add(l);
            }
        }
        return resultado;
    }

    public List<Livro> buscarPorAutor(String trecho) {
        List<Livro> resultado = new ArrayList<>();
        String busca = trecho.toLowerCase();
        for (Livro l : livros.values()) {
            if (l.getAutor().toLowerCase().contains(busca)) {
                resultado.add(l);
            }
        }
        return resultado;
    }

    public List<Livro> listarTodos() {
        return new ArrayList<>(livros.values());
    }

    public List<Livro> listarDisponiveis() {
        List<Livro> resultado = new ArrayList<>();
        for (Livro l : livros.values()) {
            if (l.isDisponivel()) resultado.add(l);
        }
        return resultado;
    }

    public List<Livro> listarEmprestados() {
        List<Livro> resultado = new ArrayList<>();
        for (Livro l : livros.values()) {
            if (!l.isDisponivel()) resultado.add(l);
        }
        return resultado;
    }

    // ---------------------- Persistência ----------------------

    /** Salva todos os livros no formato: titulo|autor|ano|isbn|disponivel|qtdEmprestimos */
    public void salvar() {
        List<String> linhas = new ArrayList<>();
        for (Livro l : livros.values()) {
            linhas.add(String.join(SEPARADOR_ESCRITA,
                    l.getTitulo(), l.getAutor(), String.valueOf(l.getAnoPublicacao()),
                    l.getIsbn(), String.valueOf(l.isDisponivel()), String.valueOf(l.getQuantidadeEmprestimos())));
        }
        try {
            ArquivoUtil.escreverLinhas(ARQUIVO_DADOS, linhas);
        } catch (IOException e) {
            System.err.println("Erro ao salvar livros: " + e.getMessage());
        }
    }

    public void carregar() {
        try {
            List<String> linhas = ArquivoUtil.lerLinhas(ARQUIVO_DADOS);
            for (String linha : linhas) {
                String[] campos = linha.split(SEPARADOR_LEITURA);
                if (campos.length < 6) continue;
                Livro livro = new Livro(campos[0], campos[1], Integer.parseInt(campos[2]),
                        campos[3], Boolean.parseBoolean(campos[4]), Integer.parseInt(campos[5]));
                livros.put(livro.getIsbn(), livro);
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar livros: " + e.getMessage());
        }
    }
}
