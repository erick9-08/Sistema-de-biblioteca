package com.biblioteca.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Representa o vínculo entre um {@link Livro} e um {@link Usuario} durante
 * um período de empréstimo. Usa {@code LocalDate} (API moderna de datas do
 * Java) para controlar a data do empréstimo, o prazo de devolução previsto
 * e a data real de devolução.
 */
public class Emprestimo {

    private static final int DIAS_PRAZO_PADRAO = 14;

    private final Livro livro;
    private final Usuario usuario;
    private final LocalDate dataEmprestimo;
    private final LocalDate dataDevolucaoPrevista;
    private LocalDate dataDevolucaoReal;
    private StatusEmprestimo status;

    /** Construtor usado ao registrar um empréstimo novo: hoje + 14 dias de prazo. */
    public Emprestimo(Livro livro, Usuario usuario) {
        this.livro = livro;
        this.usuario = usuario;
        this.dataEmprestimo = LocalDate.now();
        this.dataDevolucaoPrevista = dataEmprestimo.plusDays(DIAS_PRAZO_PADRAO);
        this.status = StatusEmprestimo.EM_ANDAMENTO;
    }

    /** Construtor completo, usado ao recarregar os dados persistidos em arquivo. */
    public Emprestimo(Livro livro, Usuario usuario, LocalDate dataEmprestimo,
                       LocalDate dataDevolucaoPrevista, LocalDate dataDevolucaoReal,
                       StatusEmprestimo status) {
        this.livro = livro;
        this.usuario = usuario;
        this.dataEmprestimo = dataEmprestimo;
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
        this.dataDevolucaoReal = dataDevolucaoReal;
        this.status = status;
    }

    public Livro getLivro() {
        return livro;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public LocalDate getDataDevolucaoPrevista() {
        return dataDevolucaoPrevista;
    }

    public LocalDate getDataDevolucaoReal() {
        return dataDevolucaoReal;
    }

    public StatusEmprestimo getStatus() {
        return status;
    }

    public boolean isAtrasado() {
        if (status == StatusEmprestimo.DEVOLVIDO) return false;
        return LocalDate.now().isAfter(dataDevolucaoPrevista);
    }

    public long diasDeAtraso() {
        if (!isAtrasado()) return 0;
        return ChronoUnit.DAYS.between(dataDevolucaoPrevista, LocalDate.now());
    }

    public void registrarDevolucao() {
        this.dataDevolucaoReal = LocalDate.now();
        this.status = StatusEmprestimo.DEVOLVIDO;
    }

    @Override
    public String toString() {
        String devolucao = dataDevolucaoReal == null ? "—" : dataDevolucaoReal.toString();
        return String.format("Livro: %-30s | Usuário: %-15s | Empréstimo: %s | Previsto: %s | Devolvido: %s | Status: %s",
                livro.getTitulo(), usuario.getNome(), dataEmprestimo, dataDevolucaoPrevista, devolucao, status);
    }
}
