package com.biblioteca.model;

import com.biblioteca.repository.LivroRepository;
import com.biblioteca.repository.UsuarioRepository;

/**
 * Representa a biblioteca física em si — um nome, um endereço e os
 * repositórios responsáveis por armazenar livros e pessoas cadastradas.
 * É o "modelo" central do sistema (camada Model do padrão MVC); a lógica
 * de negócio em si fica isolada em {@code BibliotecaService} (camada que
 * faz o papel de Controller), e o {@code Main} cuida da View no terminal.
 */
public class Biblioteca {

    private String nome;
    private String endereco;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;

    public Biblioteca(String nome, String endereco) {
        this.nome = nome;
        this.endereco = endereco;
        this.livroRepository = new LivroRepository();
        this.usuarioRepository = new UsuarioRepository();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public LivroRepository getLivroRepository() {
        return livroRepository;
    }

    public UsuarioRepository getUsuarioRepository() {
        return usuarioRepository;
    }
}
