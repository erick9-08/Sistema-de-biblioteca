package com.biblioteca.model;

/**
 * Representa os possíveis estados de um {@link Emprestimo}.
 * Usar um enum em vez de Strings ou códigos numéricos evita valores
 * inválidos e torna o código mais legível e seguro em tempo de compilação.
 */
public enum StatusEmprestimo {
    EM_ANDAMENTO,
    DEVOLVIDO,
    ATRASADO
}
