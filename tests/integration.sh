#!/usr/bin/env bash
# Prueba HTTP/JDBC real. SOLO para un entorno Docker de prueba desechable.
set -Eeuo pipefail
trap 'printf "Falló la prueba de integración en %s:%s (código %s).\n" "${BASH_SOURCE[0]}" "$LINENO" "$?" >&2' ERR
cd "$(dirname "$0")/.."
command -v docker >/dev/null
command -v curl >/dev/null
command -v openssl >/dev/null
: "${SERVER_HOST:?Configurar SERVER_HOST en .env}"
: "${ADMIN_PASSWORD:?Configurar ADMIN_PASSWORD para la prueba}"
: "${DB_PASSWORD:?Configurar DB_PASSWORD para la prueba}"
origin="https://$SERVER_HOST"
jar=$(mktemp -d)
trap 'rm -rf "$jar"' EXIT
request() {
    # Solo en tests se usa --insecure: el certificado es de la CA interna de Caddy.
    curl --insecure --silent --show-error --connect-timeout 3 --max-time 20 "$@"
}
wait_for_server() {
    for _ in $(seq 1 90); do
        if [[ $(request --output /dev/null --write-out '%{http_code}' "$origin/health" || true) == 200 ]]; then
            return 0
        fi
        sleep 2
    done
    docker compose logs --tail=80 web db caddy
    echo 'El servidor no respondió correctamente a /health' >&2
    return 1
}
wait_for_server
# Bootstrap + sesión y CSRF real. Se renueva tras reiniciar Tomcat.
login() {
# Descartar cookies anteriores permite iniciar sesión aunque Tomcat las conserve.
: > "$jar/cookies"
request --cookie-jar "$jar/cookies" "$origin/app" -o "$jar/login"
csrf=$(grep -oE 'name="csrf" value="[a-f0-9-]{36}"' "$jar/login" | head -1 | cut -d '"' -f4)
[[ -n "$csrf" ]]
status=$(request --cookie "$jar/cookies" --cookie-jar "$jar/cookies" -o /dev/null -w '%{http_code}' \
    --data-urlencode action=login --data-urlencode username=admin \
    --data-urlencode "password=$ADMIN_PASSWORD" --data-urlencode "csrf=$csrf" "$origin/app")
[[ "$status" == 302 ]] || { echo "Login devolvió $status" >&2; exit 1; }
request --cookie "$jar/cookies" "$origin/app?page=labs" -o "$jar/panel"
grep -q 'Agregar laboratorio' "$jar/panel"
csrf=$(grep -oE 'name="csrf" value="[a-f0-9-]{36}"' "$jar/panel" | head -1 | cut -d '"' -f4)
[[ -n "$csrf" ]]
}
login
# Una operación sin token no puede cambiar nada.
status=$(request --cookie "$jar/cookies" --data action=save-lab -o /dev/null -w '%{http_code}' "$origin/app")
[[ "$status" == 403 ]]
# Alta de laboratorio por la UI, comprobación con SQL y vinculación de agente.
status=$(request --cookie "$jar/cookies" --data-urlencode action=save-lab --data-urlencode page=labs \
    --data-urlencode 'name=Laboratorio CI' --data-urlencode 'room=Prueba' \
    --data-urlencode "csrf=$csrf" -o /dev/null -w '%{http_code}' "$origin/app")
[[ "$status" == 302 ]]
query() {
    docker compose exec -T -e MYSQL_PWD="$DB_PASSWORD" db mariadb -ucontrol --batch --skip-column-names control -e "$1"
}
lab=$(query "SELECT id FROM laboratories WHERE name='Laboratorio CI'" | head -1)
[[ "$lab" =~ ^[0-9]+$ ]]
# CRUD completo de laboratorio: modificar antes de continuar.
status=$(request --cookie "$jar/cookies" --data-urlencode action=save-lab --data-urlencode page=labs \
    --data-urlencode "id=$lab" --data-urlencode 'name=Laboratorio CI' \
    --data-urlencode 'room=Aula editada' --data-urlencode "csrf=$csrf" \
    -o /dev/null -w '%{http_code}' "$origin/app")
[[ "$status" == 302 ]]
[[ "$(query "SELECT room FROM laboratories WHERE id=$lab")" == 'Aula editada' ]]
# CRUD de computadora manual: inventario, NO control remoto.
status=$(request --cookie "$jar/cookies" --data-urlencode action=save-computer --data-urlencode page=pcs \
    --data-urlencode 'name=Manual CI' --data-urlencode 'ip=192.168.250.123' \
    --data-urlencode "laboratoryId=$lab" --data-urlencode "csrf=$csrf" \
    -o /dev/null -w '%{http_code}' "$origin/app")
[[ "$status" == 302 ]]
manual=$(query "SELECT id FROM computers WHERE name='Manual CI'" | head -1)
[[ "$manual" =~ ^[0-9]+$ ]]
status=$(request --cookie "$jar/cookies" --data-urlencode action=save-computer --data-urlencode page=pcs \
    --data-urlencode "id=$manual" --data-urlencode 'name=Manual actualizada' \
    --data-urlencode 'ip=192.168.250.123' --data-urlencode "laboratoryId=$lab" \
    --data-urlencode "csrf=$csrf" -o /dev/null -w '%{http_code}' "$origin/app")
[[ "$status" == 302 ]]
[[ "$(query "SELECT name FROM computers WHERE id=$manual")" == 'Manual actualizada' ]]
status=$(request --cookie "$jar/cookies" --data-urlencode action=delete-computer \
    --data-urlencode "id=$manual" --data-urlencode "csrf=$csrf" \
    -o /dev/null -w '%{http_code}' "$origin/app")
[[ "$status" == 302 ]]
[[ "$(query "SELECT COUNT(*) FROM computers WHERE id=$manual")" == 0 ]]
# CRUD usuarios: nunca borrar la sesión de admin actual ni el último usuario.
status=$(request --cookie "$jar/cookies" --data-urlencode action=save-user --data-urlencode page=users \
    --data-urlencode 'username=prueba_ci' --data-urlencode 'password=ContrasenaCI2026Segura!' \
    --data-urlencode "csrf=$csrf" -o /dev/null -w '%{http_code}' "$origin/app")
[[ "$status" == 302 ]]
user=$(query "SELECT id FROM users WHERE username='prueba_ci'" | head -1)
[[ "$user" =~ ^[0-9]+$ ]]
status=$(request --cookie "$jar/cookies" --data-urlencode action=save-user --data-urlencode page=users \
    --data-urlencode "id=$user" --data-urlencode 'username=prueba_ci_editada' \
    --data-urlencode "csrf=$csrf" -o /dev/null -w '%{http_code}' "$origin/app")
[[ "$status" == 302 ]]
[[ "$(query "SELECT username FROM users WHERE id=$user")" == 'prueba_ci_editada' ]]
status=$(request --cookie "$jar/cookies" --data-urlencode action=delete-user \
    --data-urlencode "id=$user" --data-urlencode "csrf=$csrf" \
    -o /dev/null -w '%{http_code}' "$origin/app")
[[ "$status" == 302 ]]
[[ "$(query "SELECT COUNT(*) FROM users WHERE id=$user")" == 0 ]]
id=12a2a52e-c395-449e-8c1b-01b9e47ceabe
token=2d4debf2eaff799215a2e5f0c343802ec28d4f1e350da4659b00234a71d90992
poll() {
    request --header "Authorization: Bearer $token" \
       --data-urlencode "id=$id" --data-urlencode name=ComputadoraCI "$origin/agent-api/poll"
}
[[ "$(poll)" == none ]]
status=$(request --cookie "$jar/cookies" --data-urlencode action=approve-agent \
    --data-urlencode "agentId=$id" --data-urlencode "laboratoryId=$lab" \
    --data-urlencode page=pending --data-urlencode "csrf=$csrf" \
    -o /dev/null -w '%{http_code}' "$origin/app")
[[ "$status" == 302 ]]
pc=$(query "SELECT computer_id FROM agent_nodes WHERE id='$id'" | head -1)
[[ "$pc" =~ ^[0-9]+$ ]]
status=$(request --cookie "$jar/cookies" --data-urlencode action=shutdown \
    --data-urlencode "id=$pc" --data-urlencode "csrf=$csrf" \
    -o /dev/null -w '%{http_code}' "$origin/app")
[[ "$status" == 302 ]]
# Se comprueba el transporte de la orden, NO se apaga una máquina durante CI.
command=$(poll)
[[ "$command" =~ ^shutdown\|([0-9]+)\|([0-9]+)$ ]] || { echo "Orden inesperada: $command" >&2;exit 1; }
command_id=${BASH_REMATCH[1]}
retry=$(poll)
[[ "$retry" =~ ^shutdown\|([0-9]+)\|([0-9]+)$ ]]
[[ "${BASH_REMATCH[1]}" == "$command_id" ]] # El tiempo restante puede disminuir.
accepted=$(request --header "Authorization: Bearer $token" --data-urlencode "id=$id" \
  --data-urlencode "commandId=$command_id" "$origin/agent-api/ack")
[[ "$accepted" == accepted ]]
[[ "$(poll)" == none ]]
# En esta prueba no apagamos físicamente nada. Cerramos la orden individual
# para poder verificar otra orden sobre el mismo agente sin esperar el plazo.
query "UPDATE agent_commands SET expires_at=UTC_TIMESTAMP(3) - INTERVAL 1 SECOND WHERE id=$command_id" >/dev/null
# Agregar un segundo agente y enviar una orden a toda la sala.
id2=bf615bc4-d19b-43c2-a4d0-8f6352529f64
token2=1e0f4da976a30b615dc761d555089908e2fcdb036be90532b919004276856fa
poll2() {
    request --header "Authorization: Bearer $token2" \
        --data-urlencode "id=$id2" --data-urlencode name=ComputadoraCI2 "$origin/agent-api/poll"
}
[[ "$(poll2)" == none ]]
status=$(request --cookie "$jar/cookies" --data-urlencode action=approve-agent \
    --data-urlencode "agentId=$id2" --data-urlencode "laboratoryId=$lab" \
    --data-urlencode page=pending --data-urlencode "csrf=$csrf" \
    -o /dev/null -w '%{http_code}' "$origin/app")
[[ "$status" == 302 ]]
[[ "$(poll2)" == none ]]
status=$(request --cookie "$jar/cookies" --data-urlencode action=shutdown-lab \
    --data-urlencode "id=$lab" --data-urlencode "csrf=$csrf" \
    -o /dev/null -w '%{http_code}' "$origin/app")
[[ "$status" == 302 ]]
group1=$(poll); group2=$(poll2)
[[ "$group1" =~ ^shutdown\|([0-9]+)\|([0-9]+)$ ]]
command1=${BASH_REMATCH[1]}
[[ "$group2" =~ ^shutdown\|([0-9]+)\|([0-9]+)$ ]]
command2=${BASH_REMATCH[1]}
# Las dos órdenes del laboratorio se programan para el mismo instante.
execute1=$(query "SELECT execute_at FROM agent_commands WHERE id=$command1")
execute2=$(query "SELECT execute_at FROM agent_commands WHERE id=$command2")
[[ "$execute1" == "$execute2" ]]
[[ "$(request --header "Authorization: Bearer $token" --data-urlencode "id=$id" --data-urlencode "commandId=$command1" "$origin/agent-api/ack")" == accepted ]]
[[ "$(request --header "Authorization: Bearer $token2" --data-urlencode "id=$id2" --data-urlencode "commandId=$command2" "$origin/agent-api/ack")" == accepted ]]
[[ "$(poll)" == none ]]
[[ "$(poll2)" == none ]]
wrong=$(request -o /dev/null -w '%{http_code}' --header "Authorization: Bearer ${token}x" \
    --data-urlencode "id=$id" --data-urlencode name=ComputadoraCI "$origin/agent-api/poll")
[[ "$wrong" == 401 ]]
request --cookie "$jar/cookies" "$origin/app?page=pcs" -o "$jar/pcs"
grep -q ComputadoraCI "$jar/pcs"
# El borrado revoca el agente; no se re-registra con su token anterior.
status=$(request --cookie "$jar/cookies" --data-urlencode action=delete-computer \
    --data-urlencode "id=$pc" --data-urlencode "csrf=$csrf" \
    -o /dev/null -w '%{http_code}' "$origin/app")
[[ "$status" == 302 ]]
[[ "$(query "SELECT revoked FROM agent_nodes WHERE id='$id'")" == 1 ]]
wrong=$(request -o /dev/null -w '%{http_code}' --header "Authorization: Bearer $token" \
    --data-urlencode "id=$id" --data-urlencode name=ComputadoraCI "$origin/agent-api/poll")
[[ "$wrong" == 401 ]]
docker compose restart db >/dev/null
for _ in $(seq 1 30); do
    [[ "$(docker inspect --format '{{.State.Health.Status}}' "$(docker compose ps -q db)" 2>/dev/null || true)" == healthy ]] && break
    sleep 2
done
docker compose restart web >/dev/null
wait_for_server
[[ "$(query "SELECT COUNT(*) FROM laboratories WHERE id=$lab")" == 1 ]]
[[ "$(query "SELECT revoked FROM agent_nodes WHERE id='$id'")" == 1 ]]
# Borrado de laboratorio: la FK impide borrar una sala con PCs, luego permite hacerlo.
# Los datos persisten en MariaDB; la sesión HTTP anterior puede haber caducado.
login
status=$(request --cookie "$jar/cookies" --data-urlencode action=delete-lab \
    --data-urlencode "id=$lab" --data-urlencode "csrf=$csrf" \
    -o /dev/null -w '%{http_code}' "$origin/app")
[[ "$status" == 302 ]]
[[ "$(query "SELECT COUNT(*) FROM laboratories WHERE id=$lab")" == 1 ]]
pc2=$(query "SELECT computer_id FROM agent_nodes WHERE id='$id2'" | head -1)
[[ "$pc2" =~ ^[0-9]+$ ]]
status=$(request --cookie "$jar/cookies" --data-urlencode action=delete-computer \
    --data-urlencode "id=$pc2" --data-urlencode "csrf=$csrf" \
    -o /dev/null -w '%{http_code}' "$origin/app")
[[ "$status" == 302 ]]
status=$(request --cookie "$jar/cookies" --data-urlencode action=delete-lab \
    --data-urlencode "id=$lab" --data-urlencode "csrf=$csrf" \
    -o /dev/null -w '%{http_code}' "$origin/app")
[[ "$status" == 302 ]]
[[ "$(query "SELECT COUNT(*) FROM laboratories WHERE id=$lab")" == 0 ]]
echo 'Integración Docker: HTTP, login, CSRF, CRUD completo, agentes, órdenes y persistencia OK'
