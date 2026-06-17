package com.biblioteca.repository;

import com.biblioteca.exception.AutenticacaoException;
import com.biblioteca.exception.UsuarioJaCadastradoException;
import com.biblioteca.exception.UsuarioNaoEncontradoException;
import com.biblioteca.model.Administrador;
import com.biblioteca.model.Pessoa;
import com.biblioteca.model.Usuario;
import com.biblioteca.util.ArquivoUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Camada de persistência das pessoas do sistema. Um único
 * {@code HashMap<String, Pessoa>} guarda tanto usuários comuns quanto
 * administradores — já que {@link Administrador} e {@link Usuario} são
 * subtipos de {@link Pessoa} — o que é especialmente útil no login, onde
 * não se sabe de antemão qual dos dois tipos está autenticando
 * (resolução polimórfica em tempo de execução).
 */
public class UsuarioRepository {

    private static final String ARQUIVO_DADOS = "data/usuarios.txt";
    private static final String SEPARADOR_LEITURA = "\\|";
    private static final String SEPARADOR_ESCRITA = "|";

    private final Map<String, Pessoa> pessoas = new HashMap<>();

    public void adicionarUsuario(Usuario usuario) throws UsuarioJaCadastradoException {
        if (pessoas.containsKey(usuario.getId())) {
            throw new UsuarioJaCadastradoException(usuario.getId());
        }
        pessoas.put(usuario.getId(), usuario);
    }

    public void adicionarAdministrador(Administrador administrador) throws UsuarioJaCadastradoException {
        if (pessoas.containsKey(administrador.getId())) {
            throw new UsuarioJaCadastradoException(administrador.getId());
        }
        pessoas.put(administrador.getId(), administrador);
    }

    public boolean existeId(String id) {
        return pessoas.containsKey(id);
    }

    public Usuario buscarUsuarioPorId(String id) throws UsuarioNaoEncontradoException {
        Pessoa pessoa = pessoas.get(id);
        if (!(pessoa instanceof Usuario)) {
            throw new UsuarioNaoEncontradoException(id);
        }
        return (Usuario) pessoa;
    }

    /** Autentica uma pessoa (usuário OU administrador) por id e senha. */
    public Pessoa autenticar(String id, String senha) throws AutenticacaoException {
        Pessoa pessoa = pessoas.get(id);
        if (pessoa == null || !pessoa.verificarSenha(senha)) {
            throw new AutenticacaoException();
        }
        return pessoa;
    }

    public List<Usuario> listarUsuarios() {
        List<Usuario> resultado = new ArrayList<>();
        for (Pessoa p : pessoas.values()) {
            if (p instanceof Usuario) resultado.add((Usuario) p);
        }
        return resultado;
    }

    public List<Administrador> listarAdministradores() {
        List<Administrador> resultado = new ArrayList<>();
        for (Pessoa p : pessoas.values()) {
            if (p instanceof Administrador) resultado.add((Administrador) p);
        }
        return resultado;
    }

    // ---------------------- Persistência ----------------------

    /**
     * Salva todas as pessoas. Formato:
     * USUARIO|id|nome|senha|dataCadastro
     * ADMIN|id|nome|senha|nivelAcesso
     */
    public void salvar() {
        List<String> linhas = new ArrayList<>();
        for (Pessoa p : pessoas.values()) {
            if (p instanceof Usuario) {
                Usuario u = (Usuario) p;
                linhas.add(String.join(SEPARADOR_ESCRITA,
                        "USUARIO", u.getId(), u.getNome(), u.getSenha(), u.getDataCadastro().toString()));
            } else if (p instanceof Administrador) {
                Administrador a = (Administrador) p;
                linhas.add(String.join(SEPARADOR_ESCRITA,
                        "ADMIN", a.getId(), a.getNome(), a.getSenha(), a.getNivelAcesso()));
            }
        }
        try {
            ArquivoUtil.escreverLinhas(ARQUIVO_DADOS, linhas);
        } catch (IOException e) {
            System.err.println("Erro ao salvar usuários: " + e.getMessage());
        }
    }

    public void carregar() {
        try {
            List<String> linhas = ArquivoUtil.lerLinhas(ARQUIVO_DADOS);
            for (String linha : linhas) {
                String[] c = linha.split(SEPARADOR_LEITURA);
                if (c.length < 5) continue;
                if (c[0].equals("USUARIO")) {
                    Usuario u = new Usuario(c[1], c[2], c[3], LocalDate.parse(c[4]));
                    pessoas.put(u.getId(), u);
                } else if (c[0].equals("ADMIN")) {
                    Administrador a = new Administrador(c[1], c[2], c[3], c[4]);
                    pessoas.put(a.getId(), a);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar usuários: " + e.getMessage());
        }
    }
}
