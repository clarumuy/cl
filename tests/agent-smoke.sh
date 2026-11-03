#!/usr/bin/env bash
set -Eeuo pipefail
cd "$(dirname "$0")/.."
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
javac --release 17 -encoding UTF-8 -d "$work/classes" $(find agent/src/main/java -name '*.java')
export CONTROL_AGENT_CONFIG="$work/identity/agent.properties"
java -cp "$work/classes" uy.edu.isbo.control.agent.Agent --configure 'https://lab.example' tests/fixtures/test-ca.crt
id=$(grep '^id=' "$CONTROL_AGENT_CONFIG")
java -cp "$work/classes" uy.edu.isbo.control.agent.Agent --configure 'https://lab.example' tests/fixtures/test-ca.crt
test "$(grep '^id=' "$CONTROL_AGENT_CONFIG")" = "$id"
test -f "$work/identity/ca.crt"
# Una instalación no se puede trasladar silenciosamente a otro servidor.
if java -cp "$work/classes" uy.edu.isbo.control.agent.Agent --configure 'https://otro.example' tests/fixtures/test-ca.crt >/dev/null 2>&1; then
    echo 'Error: se aceptó una migración sin autorización' >&2;exit 1
fi
echo 'Agente: compilación y configuración seguras OK'
