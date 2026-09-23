@echo off
setlocal enabledelayedexpansion

REM Sobe os quatro servicos, cada um na sua janela.
REM Antes disso confere: JDK, credenciais locais e portas livres.

cd /d "%~dp0"

REM ─────────────────────────────────────────────────────────────
REM 1. JDK
REM ─────────────────────────────────────────────────────────────
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" goto :jdk_ok

REM Procura um JDK extraido na pasta do usuario (padrao do ZIP do Adoptium)
for /d %%D in ("%USERPROFILE%\OpenJDK*") do (
    for /d %%J in ("%%D\jdk-*") do (
        if exist "%%J\bin\javac.exe" (
            set "JAVA_HOME=%%J"
            goto :jdk_ok
        )
    )
)
for /d %%J in ("%USERPROFILE%\jdk*") do (
    if exist "%%J\bin\javac.exe" (
        set "JAVA_HOME=%%J"
        goto :jdk_ok
    )
)

echo.
echo  [ERRO] Nenhum JDK encontrado.
echo.
echo  Baixe o ZIP ^(nao o instalador .msi, que exige administrador^) em:
echo    https://adoptium.net/temurin/releases/?version=17
echo  Extraia em %USERPROFILE% e rode este script de novo.
echo.
pause
exit /b 1

:jdk_ok
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo  JDK: %JAVA_HOME%

REM ─────────────────────────────────────────────────────────────
REM 2. Credenciais locais (nao versionadas)
REM ─────────────────────────────────────────────────────────────
set FALTA=0
call :checa_dev "ms-usuarios\ms-usuarios"
call :checa_dev "ms-frota"
call :checa_dev "ms-operacoes"
call :checa_dev "api-gateway"

if %FALTA% NEQ 0 (
    echo.
    echo  Copie cada application-dev.properties.example para application-dev.properties
    echo  e preencha com os dados do time. Veja docs\task\onboarding.md
    echo.
    pause
    exit /b 1
)

REM ─────────────────────────────────────────────────────────────
REM 3. Portas livres
REM ─────────────────────────────────────────────────────────────
set OCUPADA=0
for %%P in (8080 8081 8082 8083) do (
    netstat -ano | findstr /R /C:":%%P .*LISTENING" >nul 2>&1
    if !errorlevel! EQU 0 (
        echo  [OCUPADA] porta %%P ja esta em uso
        set OCUPADA=1
    )
)

if !OCUPADA! NEQ 0 (
    echo.
    echo  Ha servico rodando de uma execucao anterior. Rode parar-tudo.cmd primeiro.
    echo  ^(Subir por cima faz os novos falharem ao conectar no banco: o pooler do
    echo   Supabase tem limite de conexoes.^)
    echo.
    pause
    exit /b 1
)

REM ─────────────────────────────────────────────────────────────
REM 4. Sobe
REM ─────────────────────────────────────────────────────────────
echo.
echo  Subindo ms-usuarios  :8081
start "ms-usuarios"  cmd /k "set JAVA_HOME=%JAVA_HOME%&& cd /d %~dp0ms-usuarios\ms-usuarios && mvnw.cmd spring-boot:run"

echo  Subindo ms-frota     :8082
start "ms-frota"     cmd /k "set JAVA_HOME=%JAVA_HOME%&& cd /d %~dp0ms-frota && mvnw.cmd spring-boot:run"

echo  Subindo ms-operacoes :8083
start "ms-operacoes" cmd /k "set JAVA_HOME=%JAVA_HOME%&& cd /d %~dp0ms-operacoes && mvnw.cmd spring-boot:run"

echo  Subindo api-gateway  :8080
start "api-gateway"  cmd /k "set JAVA_HOME=%JAVA_HOME%&& cd /d %~dp0api-gateway && mvnw.cmd spring-boot:run"

echo.
echo  Quatro janelas abertas. Aguarde aparecer "Started ...Application" em cada uma.
echo.
echo  O front sobe separado:
echo    cd ..\frontend-app ^&^& npm run dev
echo.
echo  Depois abra http://localhost:5173
echo  Para derrubar tudo: parar-tudo.cmd
echo.
exit /b 0

:checa_dev
if not exist "%~1\src\main\resources\application-dev.properties" (
    echo  [FALTA] %~1\src\main\resources\application-dev.properties
    set FALTA=1
)
goto :eof
