#!/usr/bin/env bash
set -Eeuo pipefail
[[ ${EUID:-$(id -u)} == 0 ]] || { echo 'Ejecutá como root con sudo.' >&2; exit 1; }
[[ $# == 1 || $# == 2 ]] || { echo 'Uso: sudo bash instalar.sh https://servidor [ca.crt]' >&2; exit 1; }
server=$1; ca=${2:-$(dirname "$0")/ca.crt}
cd "$(dirname "$0")"
[[ -f ControlAgent && -f "$ca" ]] || { echo 'Falta ControlAgent o el certificado ca.crt' >&2; exit 1; }
install -m 755 ControlAgent /usr/local/bin/control-agent
install -d -m 700 /var/lib/control-laboratorios
CONTROL_AGENT_CONFIG=/var/lib/control-laboratorios/agent.properties /usr/local/bin/control-agent --configure "$server" "$ca"
cat >/etc/systemd/system/control-laboratorios-agent.service <<'EOF'
[Unit]
Description=Agente de Control de Laboratorios
After=network-online.target
Wants=network-online.target
[Service]
Type=simple
User=root
Environment=CONTROL_AGENT_CONFIG=/var/lib/control-laboratorios/agent.properties
ExecStart=/usr/local/bin/control-agent
Restart=always
RestartSec=10
NoNewPrivileges=true
ProtectSystem=full
ReadWritePaths=/var/lib/control-laboratorios
[Install]
WantedBy=multi-user.target
EOF
systemctl daemon-reload
systemctl enable --now control-laboratorios-agent.service
echo 'Agente instalado. Aprobalo desde Vincular equipos.'
