@echo off
setlocal

if not exist build mkdir build

echo Compilando projeto...
javac -encoding UTF-8 --release 8 -cp "lib\sqlite-jdbc.jar" -d build src\app\*.java src\model\*.java src\repository\*.java src\web\*.java
if errorlevel 1 (
    echo.
    echo Erro na compilacao.
    pause
    exit /b 1
)

echo.
echo Executando autoteste...
java -cp "build;lib\sqlite-jdbc.jar" app.Main --self-test
pause

endlocal
