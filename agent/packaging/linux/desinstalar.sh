#!/usr/bin/env bash
set -Eeuo pipefail
[[ ${EUID:-$(id -u)} == 0 ]] || { echo 'Ejecutá con sudo.' >&2; exit 1; }
systemctl disable --now control-laboratorios-agent.service 2>/dev/null || true
rm -f /etc/systemd/system/control-laboratorios-agent.service /usr/local/bin/control-agent
systemctl daemon-reload
[[ ${1:-} == --borrar-identidad ]] && rm -rf /var/lib/control-laboratorios || true
echo 'Desinstalado. Desvinculá el equipo también desde el panel.'
