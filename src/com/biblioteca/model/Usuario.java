package com.biblioteca.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa um usuário (leitor) da biblioteca.
 * Herda id/nome/senha de {@link Pessoa} e adiciona o histórico de
 * empréstimos, controlado com {@code ArrayList} (requisito do projeto).
 */
public class Usuario extends Pessoa {

    private LocalDate dataCadastro;
    private final List<Emprestimo> historicoEmprestimos;

    /** Construtor usado ao cadastrar um usuário novo (data de cadastro = hoje). */
    public Usuario(String id, String nome, String senha) {
        super(id, nome, senha);
        this.dataCadastro = LocalDate.now();
        this.historicoEmprestimos = new ArrayList<>();
    }

    /** Construtor usado ao reconstruir o objeto a partir do arquivo de dados. */
    public Usuario(String id, String nome, String senha, LocalDate dataCadastro) {
        super(id, nome, senha);
        this.dataCadastro = dataCadastro;
        this.historicoEmprestimos = new ArrayList<>();
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public List<Emprestimo> getHistoricoEmprestimos() {
        return historicoEmprestimos;
    }

    public void adicionarEmprestimo(Emprestimo emprestimo) {
        historicoEmprestimos.add(emprestimo);
    }

    public long quantidadeEmprestimosAtivos() {
        return historicoEmprestimos.stream()
                .filter(e -> e.getStatus() != StatusEmprestimo.DEVOLVIDO)
                .count();
    }

    // Implementação concreta do método abstrato de Pessoa (polimorfismo)
    @Override
    public String getTipo() {
        return "USUARIO";
    }
}
