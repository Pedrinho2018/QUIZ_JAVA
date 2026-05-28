@echo off
setlocal

echo Construindo e iniciando Empresa Segura com Docker...
echo Porta publicada: 8080 ^(altere APP_PORT no arquivo .env se necessario^)
docker compose up --build

endlocal
