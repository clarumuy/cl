param([string]$ServerAddress)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path -Parent $PSScriptRoot)
Get-Command docker -ErrorAction Stop | Out-Null
function Invoke-Docker([string[]]$Arguments) {
    $ErrorActionPreference = 'Continue'
    & docker @Arguments 2>&1 | ForEach-Object { Write-Host "$_" }
    if ($LASTEXITCODE -ne 0) {
        throw "Docker terminó con error $LASTEXITCODE. Revisá el mensaje anterior."
    }
}
Invoke-Docker -Arguments @('compose', 'version')
$path = Join-Path (Get-Location) '.env'
$lines = if (Test-Path -LiteralPath $path) {
    @(Get-Content -LiteralPath $path)
}
else {
    @()
}
function Get-Setting([string]$Key) {
    $entry = $lines | Where-Object { $_ -match ('^' + $Key + '=') } | Select-Object -First 1
    if ($entry) {
        return ($entry -split '=', 2)[1].Trim()
    }
    return ''
}
if (-not $ServerAddress) {
    $ServerAddress = Get-Setting 'SERVER_HOST'
}
if (-not $ServerAddress) {
    $ServerAddress = (Get-Setting 'PUBLIC_SERVER_URL') -replace '^https://', '' -replace '/$', ''
}
if (-not $ServerAddress) {
    $route = Get-NetRoute -AddressFamily IPv4 -DestinationPrefix '0.0.0.0/0' | Sort-Object RouteMetric | Select-Object -First 1
    if ($route) {
        $address = Get-NetIPAddress -AddressFamily IPv4 -InterfaceIndex $route.InterfaceIndex |
            Where-Object { -not $_.SkipAsSource -and $_.IPAddress -notlike '127.*' } | Select-Object -First 1
        if ($address) {
            $ServerAddress = $address.IPAddress
        }
    }
}
if ($ServerAddress -notmatch '^[A-Za-z0-9][A-Za-z0-9.-]*$' -or $ServerAddress -eq 'localhost' -or
    $ServerAddress -like '127.*' -or $ServerAddress -eq '0.0.0.0') {
    throw 'No pude detectar una dirección LAN. Ejecutá scripts\servidor.bat -ServerAddress IP-DEL-SERVIDOR.'
}
if ($ServerAddress -match '^\d+\.\d+\.\d+\.\d+$') {
    $localAddress = Get-NetIPAddress -AddressFamily IPv4 -IPAddress $ServerAddress -ErrorAction SilentlyContinue
    if (-not $localAddress) {
        throw "La IP $ServerAddress no pertenece a este servidor. Revisá DHCP o indicá la dirección actual."
    }
}
function New-Secret {
    $bytes = New-Object byte[] 32
    $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $rng.GetBytes($bytes)
    }
    finally {
        $rng.Dispose()
    }
    return [BitConverter]::ToString($bytes).Replace('-', '').ToLowerInvariant()
}
$temporary = $path + '.' + [Guid]::NewGuid().ToString('N')
try {
    if (Test-Path -LiteralPath $path) {
        $lines = @($lines | Where-Object { $_ -notmatch '^(SERVER_HOST|PUBLIC_SERVER_URL|CADDY_CA_URL)=' })
        Write-Host 'Se conservan las credenciales existentes.'
    }
    else {
        $admin = New-Secret
        $lines = @("DB_PASSWORD=$(New-Secret)", "DB_ROOT_PASSWORD=$(New-Secret)", "ADMIN_PASSWORD=$admin")
        Write-Host "Cuenta inicial: admin. Contraseña inicial: $admin"
    }
    $lines += "SERVER_HOST=$ServerAddress"
    [IO.File]::WriteAllLines($temporary, $lines, (New-Object Text.UTF8Encoding($false)))
    $identity = [Security.Principal.WindowsIdentity]::GetCurrent().Name
    & icacls.exe $temporary /inheritance:r /grant:r "${identity}:F" | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw 'No se pudieron proteger los permisos de .env.'
    }
    Move-Item -Force -LiteralPath $temporary -Destination $path
    Write-Host "Dirección del servidor: https://$ServerAddress"
    Invoke-Docker -Arguments @('compose', 'up', '-d', '--wait', 'db')
    Invoke-Docker -Arguments @('compose', 'exec', '-T', 'db', 'sh', '/opt/control/check-database.sh')
    Invoke-Docker -Arguments @('compose', 'up', '--build', '-d', '--remove-orphans', '--force-recreate', 'web', 'caddy')
    function Test-ServerReady {
        $ErrorActionPreference = 'Continue'
        $status = & docker compose exec -T caddy wget -q -T 2 -O - http://web:8080/health 2>$null
        return ($LASTEXITCODE -eq 0 -and "$status".Trim() -eq 'ok')
    }
    $ready = $false
    for ($i = 0; $i -lt 60; $i++) {
        if (Test-ServerReady) {
            $ready = $true; break
        }
        Start-Sleep -Seconds 1
    }
    if (-not $ready) {
        Invoke-Docker -Arguments @('compose', 'logs', '--tail=30', 'web', 'caddy')
        throw 'Los servicios no quedaron listos. Corregí el problema y repetí este mismo comando.'
    }
    Invoke-Docker -Arguments @('compose', 'exec', '-T', 'caddy', 'caddy', 'validate', '--config', '/etc/caddy/Caddyfile', '--adapter', 'caddyfile')
    Write-Host "Servidor listo. Abrí https://$ServerAddress en esta computadora."
    $caFolder = Join-Path (Get-Location) 'secrets'
    New-Item -ItemType Directory -Force -Path $caFolder | Out-Null
    $caText = & docker compose exec -T caddy cat /data/caddy/pki/authorities/local/root.crt
    if ($LASTEXITCODE -ne 0 -or ($caText -join "`n") -notmatch 'BEGIN CERTIFICATE') {throw 'No se pudo exportar la CA pública.'}
    [IO.File]::WriteAllText((Join-Path $caFolder 'ca.crt'), (($caText -join "`n") + "`n"), [Text.Encoding]::ASCII)
    Write-Host 'CA pública guardada en secrets\ca.crt. Copiala al paquete del agente.'
    Write-Host 'Vincular equipos muestra el comando de instalación por plataforma.' 
    Write-Host 'Permití TCP 443 desde la LAN y reservá la dirección del servidor en DHCP.'
    Write-Host 'Los agentes generan sus credenciales; no hay tokens para copiar.'
}
finally {
    if (Test-Path -LiteralPath $temporary) {
        Remove-Item -Force -LiteralPath $temporary
    }
}
