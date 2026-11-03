param([switch]$BorrarIdentidad)
$ErrorActionPreference='Stop'
$admin=[Security.Principal.WindowsPrincipal]([Security.Principal.WindowsIdentity]::GetCurrent())
if (-not $admin.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {throw 'Abrí PowerShell como administrador.'}
$task=Get-ScheduledTask -TaskName 'ControlLaboratoriosAgent' -ErrorAction SilentlyContinue
if($task){Stop-ScheduledTask -TaskName 'ControlLaboratoriosAgent' -ErrorAction SilentlyContinue;Unregister-ScheduledTask -TaskName 'ControlLaboratoriosAgent' -Confirm:$false}
Remove-Item -Recurse -Force -ErrorAction SilentlyContinue (Join-Path $env:ProgramFiles 'ControlLaboratoriosAgent')
if($BorrarIdentidad){Remove-Item -Recurse -Force -ErrorAction SilentlyContinue (Join-Path $env:ProgramData 'ControlLaboratorios')}
Write-Host 'Agente desinstalado. Desvinculá el equipo desde el panel si ya no se utilizará.'
