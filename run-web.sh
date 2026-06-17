#!/bin/bash
# Compila e inicia o servidor web do Sistema de Biblioteca

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$PROJECT_DIR/src"
OUT_DIR="$PROJECT_DIR/out"

echo "=== Sistema de Biblioteca - Modo Web ==="
echo "Compilando..."

mkdir -p "$OUT_DIR"

find "$SRC_DIR" -name "*.java" > /tmp/sources.txt

javac -encoding UTF-8 -d "$OUT_DIR" @/tmp/sources.txt 2>&1

if [ $? -ne 0 ]; then
  echo "ERRO: Falha na compilação."
  exit 1
fi

echo "Compilação concluída!"
echo ""
cd "$PROJECT_DIR"
java -cp "$OUT_DIR" com.biblioteca.api.ApiMain
