#!/usr/bin/env bash
set -Eeuo pipefail
cd "$(dirname "$0")/.."
bash -n agent/packaging/linux/*.sh agent/packaging/macos/*.sh scripts/servidor.sh
for os in linux macos; do
    grep -q 'ca.crt' "agent/packaging/$os/instalar.sh"
    grep -q -- '--configure' "agent/packaging/$os/instalar.sh"
done
echo 'Sintaxis de instaladores Unix OK'
