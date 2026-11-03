#!/usr/bin/env bash
set -Eeuo pipefail
[[ $(id -u) == 0 ]] || { echo 'Ejecutá con sudo.' >&2; exit 1; }
[[ $# == 1 || $# == 2 ]] || { echo 'Uso: sudo bash instalar.sh https://servidor [ca.crt]' >&2; exit 1; }
server=$1; cd "$(dirname "$0")"; ca=${2:-./ca.crt}
[[ -f ControlAgent && -f "$ca" ]] || { echo 'Falta ControlAgent o ca.crt' >&2; exit 1; }
install -d -m 755 /usr/local/libexec
install -m 755 ControlAgent /usr/local/libexec/control-laboratorios-agent
install -d -m 700 '/Library/Application Support/ControlLaboratorios'
/usr/local/libexec/control-laboratorios-agent --configure "$server" "$ca"
plist=/Library/LaunchDaemons/uy.edu.isbo.control-agent.plist
launchctl bootout system "$plist" >/dev/null 2>&1 || true
cat >"$plist" <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0"><dict>
<key>Label</key><string>uy.edu.isbo.control-agent</string>
<key>ProgramArguments</key><array><string>/usr/local/libexec/control-laboratorios-agent</string></array>
<key>RunAtLoad</key><true/>
<key>KeepAlive</key><true/>
</dict></plist>
EOF
chown root:wheel "$plist"; chmod 644 "$plist"
launchctl bootstrap system "$plist"
echo 'Agente instalado. Aprobalo desde Vincular equipos.'
