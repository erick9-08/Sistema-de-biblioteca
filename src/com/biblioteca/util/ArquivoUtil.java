package com.biblioteca.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitária responsável por toda a leitura/escrita de arquivos
 * de texto (.txt) usados como persistência do sistema. Os repositórios
 * chamam estes métodos para não precisar lidar diretamente com streams
 * de I/O — eles só sabem "transformar objeto em linha de texto" e
 * vice-versa.
 */
public class ArquivoUtil {

    // Construtor privado: classe utilitária não deve ser instanciada
    private ArquivoUtil() {
    }

    /** Garante que o diretório do arquivo exista antes de tentar escrever nele. */
    public static void garantirDiretorio(String caminhoArquivo) {
        try {
            Path path = Paths.get(caminhoArquivo).getParent();
            if (path != null && !Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            System.err.println("Erro ao criar diretório de dados: " + e.getMessage());
        }
    }

    /** Escreve uma lista de linhas em um arquivo de texto, sobrescrevendo o conteúdo anterior. */
    public static void escreverLinhas(String caminhoArquivo, List<String> linhas) throws IOException {
        garantirDiretorio(caminhoArquivo);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(caminhoArquivo), StandardCharsets.UTF_8))) {
            for (String linha : linhas) {
                writer.write(linha);
                writer.newLine();
            }
        }
    }

    /** Lê todas as linhas não vazias de um arquivo de texto. Retorna lista vazia se o arquivo não existir ainda. */
    public static List<String> lerLinhas(String caminhoArquivo) throws IOException {
        List<String> linhas = new ArrayList<>();
        File arquivo = new File(caminhoArquivo);
        if (!arquivo.exists()) {
            return linhas; // primeira execução: o arquivo de dados ainda não existe
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(arquivo), StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (!linha.isBlank()) {
                    linhas.add(linha);
                }
            }
        }
        return linhas;
    }
}
