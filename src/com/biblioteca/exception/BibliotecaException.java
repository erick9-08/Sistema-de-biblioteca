package com.biblioteca.exception;

/**
 * Exceção base (abstrata) de todas as exceções de negócio do sistema.
 * Permite que a camada de apresentação (Main) capture um único tipo
 * — {@code BibliotecaException} — e ainda assim trate corretamente
 * qualquer subtipo específico lançado pelas camadas internas
 * (polimorfismo aplicado a exceções).
 */
public abstract class BibliotecaException extends Exception {
    protected BibliotecaException(String mensagem) {
        super(mensagem);
    }
}
