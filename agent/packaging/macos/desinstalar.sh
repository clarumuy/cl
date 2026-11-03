#!/usr/bin/env bash
set -Eeuo pipefail
[[ $(id -u) == 0 ]] || { echo 'Ejecutá con sudo.' >&2; exit 1; }
plist=/Library/LaunchDaemons/uy.edu.isbo.control-agent.plist
launchctl bootout system "$plist" >/dev/null 2>&1 || true
rm -f "$plist" /usr/local/libexec/control-laboratorios-agent
[[ ${1:-} == --borrar-identidad ]] && rm -rf '/Library/Application Support/ControlLaboratorios' || true
echo 'Desinstalado. Desvinculá el equipo en el panel.'
