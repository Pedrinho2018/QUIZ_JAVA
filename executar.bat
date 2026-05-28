@echo off
setlocal

if not exist build mkdir build

echo Compilando projeto...
javac --release 8 -d build src\app\*.java src\model\*.java src\web\*.java
if errorlevel 1 (
    echo.
    echo Erro na compilacao.
    pause
    exit /b 1
)

echo.
echo Iniciando Empresa Segura no navegador...
java -cp build app.Main

endlocal
