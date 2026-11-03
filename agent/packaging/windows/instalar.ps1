# Ejecutar desde el paquete de GitHub Releases con PowerShell como administrador.
param(
    [Parameter(Mandatory=$true)][string]$Server,
    [string]$CaPath = (Join-Path $PSScriptRoot 'ca.crt')
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$admin = [Security.Principal.WindowsPrincipal]([Security.Principal.WindowsIdentity]::GetCurrent())
if (-not $admin.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    throw 'Abrí PowerShell como administrador.'
}
if ($Server -notmatch '^https://[a-zA-Z0-9.-]+(:[0-9]+)?/?$') {
    throw 'Dirección inválida. Usá https://IP-DEL-SERVIDOR'
}
$binary = Join-Path $PSScriptRoot 'ControlAgent.exe'
if (-not (Test-Path -LiteralPath $binary)) { throw 'Falta ControlAgent.exe' }
if (-not (Test-Path -LiteralPath $CaPath)) { throw 'Falta ca.crt. Copialo del servidor a esta carpeta.' }
$binDir = Join-Path $env:ProgramFiles 'ControlLaboratoriosAgent'
$dataDir = Join-Path $env:ProgramData 'ControlLaboratorios'
New-Item -ItemType Directory -Force -Path $binDir,$dataDir | Out-Null
foreach ($dir in @($binDir,$dataDir)) {
    & icacls.exe $dir /inheritance:r /grant:r '*S-1-5-18:(OI)(CI)F' '*S-1-5-32-544:(OI)(CI)F' | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "No se pudo proteger $dir" }
}
$task = 'ControlLaboratoriosAgent'
$existing = Get-ScheduledTask -TaskName $task -ErrorAction SilentlyContinue
if ($existing) {
    Stop-ScheduledTask -TaskName $task -ErrorAction SilentlyContinue
    Unregister-ScheduledTask -TaskName $task -Confirm:$false
}
$target = Join-Path $binDir 'ControlAgent.exe'
Copy-Item -LiteralPath $binary -Destination $target -Force
& $target --configure ($Server.TrimEnd('/')) (Resolve-Path -LiteralPath $CaPath).Path
if ($LASTEXITCODE -ne 0) { throw 'No se pudo configurar el agente' }
$action = New-ScheduledTaskAction -Execute $target
$trigger = New-ScheduledTaskTrigger -AtStartup
$principal = New-ScheduledTaskPrincipal -UserId 'SYSTEM' -LogonType ServiceAccount -RunLevel Highest
$settings = New-ScheduledTaskSettingsSet -RestartCount 3 -RestartInterval (New-TimeSpan -Minutes 1) -ExecutionTimeLimit (New-TimeSpan -Seconds 0)
Register-ScheduledTask -TaskName $task -Action $action -Trigger $trigger -Principal $principal -Settings $settings -Force | Out-Null
Start-ScheduledTask -TaskName $task
Write-Host 'Agente instalado. Entrá a Vincular equipos para aprobarlo.'
