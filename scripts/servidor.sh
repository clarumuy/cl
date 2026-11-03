#!/usr/bin/env bash
set -Eeuo pipefail
umask 077
cd "$(dirname "$0")/.."
trap 'echo "No se completó el arranque. Revisá el mensaje anterior; los datos y las claves existentes se conservan." >&2' ERR
command -v docker > /dev/null || {
    echo 'Instalá Docker con Compose antes de continuar.' >&2
    exit 1
}
docker compose version > /dev/null
read_setting() {
    [[ -f .env ]] && awk -F= -v key="$1" '$1 == key {sub(/^[^=]*=/, ""); gsub(/\r/, ""); print; exit}' .env || true
}
host=${1:-$(read_setting SERVER_HOST)}
if [[ -z "$host" ]]; then
    previous=$(read_setting PUBLIC_SERVER_URL)
    [[ -z "$previous" ]] || host=${previous#https://}
    host=${host%/}
fi
if [[ -z "$host" ]]; then
    if command -v ip > /dev/null; then
        host=$(ip -4 route get 1.1.1.1 | awk '{for(i=1;i<=NF;i++) if($i=="src") {print $(i+1);exit}}')
    elif [[ $(uname -s) == Darwin ]]; then
        interface=$(route -n get default | awk '/interface:/ {print $2;exit}')
        host=$(ipconfig getifaddr "$interface")
    fi
fi
[[ "$host" =~ ^[A-Za-z0-9][A-Za-z0-9.-]*$ && "$host" != localhost && "$host" != 127.* && "$host" != 0.0.0.0 ]] || {
    echo 'No pude detectar una dirección LAN. Ejecutá: bash scripts/servidor.sh IP-DEL-SERVIDOR' >&2
    exit 1
}
if command -v ip > /dev/null && [[ "$host" =~ ^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    addresses=$(ip -o -4 addr show | awk '{split($4,a,"/"); print a[1]}')
    if [[ -n "$addresses" ]] && ! grep -Fxq "$host" <<< "$addresses"; then
        echo "La dirección $host no está asignada a este servidor. Revisá DHCP o ejecutá bash servidor.sh IP-ACTUAL." >&2
        exit 1
    fi
fi
temporary=$(mktemp .env.XXXXXXXX)
trap 'rm -f "$temporary"' EXIT
if [[ -f .env ]]; then
    awk '!/^(SERVER_HOST|PUBLIC_SERVER_URL|CADDY_CA_URL)=/' .env > "$temporary"
    echo 'Se conservan las credenciales existentes.'
else
    command -v openssl > /dev/null || {
        echo 'Falta openssl para generar las claves iniciales.' >&2
        exit 1
    }
    admin=$(openssl rand -hex 16)
    printf 'DB_PASSWORD=%s\nDB_ROOT_PASSWORD=%s\nADMIN_PASSWORD=%s\n' \
        "$(openssl rand -hex 24)" "$(openssl rand -hex 24)" "$admin" > "$temporary"
    printf 'Cuenta inicial: admin\nContraseña inicial: %s\n' "$admin"
fi
printf '\nSERVER_HOST=%s\n' "$host" >> "$temporary"
chmod 600 "$temporary"
mv -f "$temporary" .env
printf 'Dirección del servidor: https://%s\n' "$host"
docker compose up -d --wait db
docker compose exec -T db sh /opt/control/check-database.sh
docker compose up --build -d --remove-orphans --force-recreate web caddy
ready=false
for attempt in {1..60}; do
    if docker compose exec -T caddy wget -q -T 2 -O - http://web:8080/health 2> /dev/null | grep -qx ok; then
        ready=true
        break
    fi
    sleep 1
done
if [[ "$ready" != true ]]; then
    docker compose logs --tail=30 web caddy
    echo 'Los servicios no quedaron listos. Corregí los errores y repetí este mismo comando.' >&2
    exit 1
fi
docker compose exec -T caddy caddy validate --config /etc/caddy/Caddyfile --adapter caddyfile > /dev/null
printf '\nServidor listo. Abrí https://%s en esta computadora.\n' "$host"
mkdir -p secrets
# Solo se exporta el certificado público, nunca las claves privadas de la CA.
docker compose exec -T caddy cat /data/caddy/pki/authorities/local/root.crt > secrets/ca.crt
chmod 600 secrets/ca.crt
printf 'CA pública guardada en secrets/ca.crt. Copiala al paquete de cada agente.\n'
echo 'Vincular equipos muestra el comando de instalación por plataforma.' 
echo 'Los clientes solo necesitan llegar al TCP 443 de este servidor. Permitilo desde la LAN.'
echo 'Los agentes generan sus propias credenciales; no hay tokens para copiar.'
echo 'Reservá esta dirección en DHCP para que los clientes no tengan que reconfigurarse.'
