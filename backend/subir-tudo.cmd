@echo off
REM Sobe os quatro servicos, cada um na sua janela.
REM Nao precisa configurar nada: JAVA_HOME e resolvido aqui e as credenciais
REM ficam nos application-dev.properties de cada modulo (fora do git).

set "JAVA_HOME=C:\Users\igor.martins\OpenJDK17U-jdk_x64_windows_hotspot_17.0.20.1_1\jdk-17.0.20.1+1"
set "PATH=%JAVA_HOME%\bin;%PATH%"

if not exist "%JAVA_HOME%\bin\javac.exe" (
    echo.
    echo  [ERRO] JDK nao encontrado em:
    echo    %JAVA_HOME%
    echo.
    echo  Ajuste a variavel JAVA_HOME no topo deste arquivo.
    echo.
    pause
    exit /b 1
)

cd /d "%~dp0"

echo Subindo ms-usuarios  :8081
start "ms-usuarios"  cmd /k "cd /d %~dp0ms-usuarios\ms-usuarios && mvnw.cmd spring-boot:run"

echo Subindo ms-frota     :8082
start "ms-frota"     cmd /k "cd /d %~dp0ms-frota && mvnw.cmd spring-boot:run"

echo Subindo ms-operacoes :8083
start "ms-operacoes" cmd /k "cd /d %~dp0ms-operacoes && mvnw.cmd spring-boot:run"

echo Subindo api-gateway  :8080
start "api-gateway"  cmd /k "cd /d %~dp0api-gateway && mvnw.cmd spring-boot:run"

echo.
echo  Quatro janelas abertas. O front sobe separado:
echo    cd frontend-app ^&^& npm run dev
echo.
echo  Depois abra http://localhost:5173
echo.
