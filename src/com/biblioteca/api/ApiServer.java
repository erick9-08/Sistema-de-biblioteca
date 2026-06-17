package com.biblioteca.api;

import com.biblioteca.exception.*;
import com.biblioteca.model.*;
import com.biblioteca.service.BibliotecaService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Servidor HTTP embutido que expõe o BibliotecaService via API REST/JSON.
 * Usa apenas classes da JDK padrão (com.sun.net.httpserver), sem dependências externas.
 */
public class ApiServer {

    private static final int PORT = 8080;
    private final BibliotecaService service;

    public ApiServer(BibliotecaService service) {
        this.service = service;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/auth/login",    this::handleLogin);
        server.createContext("/api/livros",        this::handleLivros);
        server.createContext("/api/usuarios",      this::handleUsuarios);
        server.createContext("/api/emprestimos",   this::handleEmprestimos);
        server.createContext("/api/relatorios",    this::handleRelatorios);
        server.createContext("/",                  this::handleFrontend);

        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("Servidor web iniciado em http://localhost:" + PORT);
        System.out.println("Abra o navegador e acesse: http://localhost:" + PORT);
    }

    // -------------------------------------------------------------------------
    // Auth
    // -------------------------------------------------------------------------

    private void handleLogin(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equals("POST")) { sendError(ex, 405, "Método não permitido"); return; }
        addCors(ex);
        Map<String, String> body = parseJsonBody(ex);
        String id = body.getOrDefault("id", "");
        String senha = body.getOrDefault("senha", "");
        try {
            Pessoa pessoa = service.autenticar(id, senha);
            String tipo = (pessoa instanceof Administrador) ? "ADMINISTRADOR" : "USUARIO";
            String json = String.format("{\"nome\":\"%s\",\"id\":\"%s\",\"tipo\":\"%s\"}",
                    esc(pessoa.getNome()), esc(pessoa.getId()), tipo);
            sendJson(ex, 200, json);
        } catch (AutenticacaoException e) {
            sendError(ex, 401, e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Livros
    // -------------------------------------------------------------------------

    private void handleLivros(HttpExchange ex) throws IOException {
        addCors(ex);
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();
        String query = ex.getRequestURI().getQuery();

        if (method.equals("OPTIONS")) { sendJson(ex, 204, ""); return; }

        if (method.equals("GET")) {
            if (path.endsWith("/disponiveis")) {
                sendJson(ex, 200, livrosToJson(service.listarDisponiveis()));
            } else if (path.endsWith("/emprestados")) {
                sendJson(ex, 200, livrosToJson(service.listarEmprestados()));
            } else if (query != null && query.contains("q=")) {
                Map<String, String> params = parseQuery(query);
                String termo = params.getOrDefault("q", "");
                String tipo = params.getOrDefault("tipo", "titulo");
                List<Livro> resultado;
                if ("autor".equals(tipo)) resultado = service.buscarPorAutor(termo);
                else if ("isbn".equals(tipo)) {
                    try { resultado = Collections.singletonList(service.buscarPorIsbn(termo)); }
                    catch (LivroNaoEncontradoException e) { resultado = Collections.emptyList(); }
                } else resultado = service.buscarPorTitulo(termo);
                sendJson(ex, 200, livrosToJson(resultado));
            } else {
                sendJson(ex, 200, livrosToJson(service.listarTodosLivros()));
            }
            return;
        }

        if (method.equals("POST")) {
            Map<String, String> body = parseJsonBody(ex);
            try {
                Livro l = service.cadastrarLivro(
                        body.getOrDefault("titulo", ""),
                        body.getOrDefault("autor", ""),
                        Integer.parseInt(body.getOrDefault("ano", "0")),
                        body.getOrDefault("isbn", ""));
                service.salvarTudo();
                sendJson(ex, 201, livroToJson(l));
            } catch (LivroJaCadastradoException e) {
                sendError(ex, 409, e.getMessage());
            } catch (NumberFormatException e) {
                sendError(ex, 400, "Ano inválido");
            }
            return;
        }

        sendError(ex, 405, "Método não permitido");
    }

    // -------------------------------------------------------------------------
    // Usuários
    // -------------------------------------------------------------------------

    private void handleUsuarios(HttpExchange ex) throws IOException {
        addCors(ex);
        String method = ex.getRequestMethod();

        if (method.equals("OPTIONS")) { sendJson(ex, 204, ""); return; }

        if (method.equals("GET")) {
            List<Usuario> usuarios = service.listarUsuarios();
            String json = "[" + usuarios.stream().map(this::usuarioToJson).collect(Collectors.joining(",")) + "]";
            sendJson(ex, 200, json);
            return;
        }

        if (method.equals("POST")) {
            Map<String, String> body = parseJsonBody(ex);
            try {
                Usuario u = service.cadastrarUsuario(
                        body.getOrDefault("nome", ""),
                        body.getOrDefault("id", ""),
                        body.getOrDefault("senha", ""));
                service.salvarTudo();
                sendJson(ex, 201, usuarioToJson(u));
            } catch (UsuarioJaCadastradoException e) {
                sendError(ex, 409, e.getMessage());
            }
            return;
        }

        sendError(ex, 405, "Método não permitido");
    }

    // -------------------------------------------------------------------------
    // Empréstimos
    // -------------------------------------------------------------------------

    private void handleEmprestimos(HttpExchange ex) throws IOException {
        addCors(ex);
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();

        if (method.equals("OPTIONS")) { sendJson(ex, 204, ""); return; }

        if (method.equals("GET")) {
            if (path.endsWith("/atrasados")) {
                List<Emprestimo> atrasados = service.listarEmprestimosAtrasados();
                sendJson(ex, 200, "[" + atrasados.stream().map(this::emprestimoToJson).collect(Collectors.joining(",")) + "]");
            } else {
                // lista todos via relatório top-livros (não há listAll no service, mas podemos derivar)
                sendJson(ex, 200, "[]");
            }
            return;
        }

        if (method.equals("POST")) {
            Map<String, String> body = parseJsonBody(ex);

            if (path.endsWith("/devolver")) {
                try {
                    Emprestimo emp = service.devolverLivro(
                            body.getOrDefault("idUsuario", ""),
                            body.getOrDefault("isbn", ""));
                    service.salvarTudo();
                    sendJson(ex, 200, emprestimoToJson(emp));
                } catch (BibliotecaException e) {
                    sendError(ex, 400, e.getMessage());
                }
            } else {
                try {
                    Emprestimo emp = service.realizarEmprestimo(
                            body.getOrDefault("idUsuario", ""),
                            body.getOrDefault("isbn", ""));
                    service.salvarTudo();
                    sendJson(ex, 201, emprestimoToJson(emp));
                } catch (BibliotecaException e) {
                    sendError(ex, 400, e.getMessage());
                }
            }
            return;
        }

        sendError(ex, 405, "Método não permitido");
    }

    // -------------------------------------------------------------------------
    // Relatórios
    // -------------------------------------------------------------------------

    private void handleRelatorios(HttpExchange ex) throws IOException {
        addCors(ex);
        if (!ex.getRequestMethod().equals("GET")) { sendError(ex, 405, "Método não permitido"); return; }
        String path = ex.getRequestURI().getPath();
        if (path.endsWith("/top-livros")) {
            List<Livro> top = service.relatorioLivrosMaisEmprestados(5);
            sendJson(ex, 200, livrosToJson(top));
        } else if (path.endsWith("/stats")) {
            long total = service.listarTodosLivros().size();
            long disponiveis = service.listarDisponiveis().size();
            long emprestados = service.listarEmprestados().size();
            long usuarios = service.listarUsuarios().size();
            long atrasados = service.listarEmprestimosAtrasados().size();
            String json = String.format(
                    "{\"totalLivros\":%d,\"livrosDisponiveis\":%d,\"livrosEmprestados\":%d,\"totalUsuarios\":%d,\"emprestimosAtrasados\":%d}",
                    total, disponiveis, emprestados, usuarios, atrasados);
            sendJson(ex, 200, json);
        } else {
            sendError(ex, 404, "Endpoint não encontrado");
        }
    }

    // -------------------------------------------------------------------------
    // Frontend estático
    // -------------------------------------------------------------------------

    private void handleFrontend(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/") || path.equals("/index.html")) {
            File f = new File("frontend/index.html");
            if (f.exists()) {
                byte[] bytes = java.nio.file.Files.readAllBytes(f.toPath());
                ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                ex.sendResponseHeaders(200, bytes.length);
                ex.getResponseBody().write(bytes);
                ex.getResponseBody().close();
            } else {
                sendError(ex, 404, "Frontend não encontrado. Coloque index.html em frontend/");
            }
        } else {
            sendError(ex, 404, "Não encontrado");
        }
    }

    // -------------------------------------------------------------------------
    // JSON helpers
    // -------------------------------------------------------------------------

    private String livroToJson(Livro l) {
        return String.format("{\"titulo\":\"%s\",\"autor\":\"%s\",\"ano\":%d,\"isbn\":\"%s\",\"disponivel\":%b,\"quantidadeEmprestimos\":%d}",
                esc(l.getTitulo()), esc(l.getAutor()), l.getAnoPublicacao(), esc(l.getIsbn()),
                l.isDisponivel(), l.getQuantidadeEmprestimos());
    }

    private String livrosToJson(List<Livro> livros) {
        return "[" + livros.stream().map(this::livroToJson).collect(Collectors.joining(",")) + "]";
    }

    private String usuarioToJson(Usuario u) {
        return String.format("{\"id\":\"%s\",\"nome\":\"%s\",\"dataCadastro\":\"%s\",\"emprestimosAtivos\":%d}",
                esc(u.getId()), esc(u.getNome()),
                u.getDataCadastro() != null ? u.getDataCadastro().toString() : "",
                u.quantidadeEmprestimosAtivos());
    }

    private String emprestimoToJson(Emprestimo e) {
        String devReal = e.getDataDevolucaoReal() != null ? "\"" + e.getDataDevolucaoReal() + "\"" : "null";
        return String.format(
                "{\"livroTitulo\":\"%s\",\"livroIsbn\":\"%s\",\"usuarioNome\":\"%s\",\"usuarioId\":\"%s\"," +
                "\"dataEmprestimo\":\"%s\",\"dataDevolucaoPrevista\":\"%s\",\"dataDevolucaoReal\":%s," +
                "\"status\":\"%s\",\"diasAtraso\":%d}",
                esc(e.getLivro().getTitulo()), esc(e.getLivro().getIsbn()),
                esc(e.getUsuario().getNome()), esc(e.getUsuario().getId()),
                e.getDataEmprestimo(), e.getDataDevolucaoPrevista(), devReal,
                e.getStatus().name(), e.diasDeAtraso());
    }

    // -------------------------------------------------------------------------
    // HTTP utilities
    // -------------------------------------------------------------------------

    private void addCors(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private void sendError(HttpExchange ex, int status, String msg) throws IOException {
        String json = "{\"erro\":\"" + esc(msg) + "\"}";
        sendJson(ex, status, json);
    }

    private Map<String, String> parseJsonBody(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> map = new LinkedHashMap<>();
        body = body.trim();
        if (body.startsWith("{")) body = body.substring(1);
        if (body.endsWith("}")) body = body.substring(0, body.length() - 1);
        for (String part : body.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")) {
            String[] kv = part.split(":", 2);
            if (kv.length == 2) {
                String k = kv[0].trim().replaceAll("\"", "");
                String v = kv[1].trim().replaceAll("^\"|\"$", "");
                map.put(k, v);
            }
        }
        return map;
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new LinkedHashMap<>();
        if (query == null) return map;
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                try { map.put(URLDecoder.decode(kv[0], "UTF-8"), URLDecoder.decode(kv[1], "UTF-8")); }
                catch (Exception ignored) {}
            }
        }
        return map;
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
