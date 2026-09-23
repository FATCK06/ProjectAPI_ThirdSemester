@echo off
REM Derruba os servicos do projeto.
REM
REM Criterio: processo java que esteja escutando em 8080-8083, ou cuja linha de
REM comando aponte para esta pasta. Nunca encerra o java do SQL Developer nem
REM qualquer outro que nao case com isso - matar "todo java.exe" derrubaria
REM extensao do VS Code junto.

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$raiz = Split-Path -Parent '%~f0';" ^
  "$portas = 8080,8081,8082,8083;" ^
  "$pids = @();" ^
  "foreach ($p in $portas) { $pids += (Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue).OwningProcess };" ^
  "$alvos = Get-CimInstance Win32_Process -Filter \"Name='java.exe'\" | Where-Object { ($pids -contains $_.ProcessId) -or ($_.CommandLine -like \"*$raiz*\") } | Where-Object { $_.CommandLine -notlike '*oracle.sql-developer*' };" ^
  "if (-not $alvos) { Write-Host '  Nenhum servico do projeto rodando.'; exit 0 };" ^
  "foreach ($a in $alvos) { Write-Host ('  encerrando PID ' + $a.ProcessId); Stop-Process -Id $a.ProcessId -Force -ErrorAction SilentlyContinue };" ^
  "Start-Sleep -Seconds 2;" ^
  "$restou = foreach ($p in $portas) { if (Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue) { $p } };" ^
  "if ($restou) { Write-Host ('  ainda ocupadas: ' + ($restou -join ', ')) } else { Write-Host '  Portas 8080-8083 livres.' }"

echo.
pause
