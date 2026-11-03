#!/usr/bin/env bash
# Comprobaciones simples de los archivos que incluimos en el ZIP.
set -Eeuo pipefail
cd "$(dirname "$0")/.."
for file in \
    readme.md docs/readme.md docs/consigna/requisitos-proyecto.pdf \
    docs/academico/informe.md docs/academico/informe.pdf \
    server/pom.xml agent/pom.xml pom.xml \
    compose.yaml infra/caddy/Caddyfile database/schema.sql \
    scripts/servidor.sh scripts/servidor.ps1 scripts/servidor.bat \
    .github/workflows/ci.yml .github/workflows/release.yml \
    docs/academico/diagramas/clases.svg docs/academico/diagramas/arquitectura.svg \
    docs/academico/diagramas/base-datos.svg docs/academico/diagramas/apagado.svg; do
    test -s "$file" || { echo "Falta $file" >&2; exit 1; }
done
! find . -name README.md -o -name 'Readme.md' | grep -q . || {
    echo 'Los README deben llamarse readme.md (minúscula).' >&2
    exit 1
}
echo 'Estructura del repositorio y documentación: OK'
