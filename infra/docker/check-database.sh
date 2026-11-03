#!/bin/sh
# Se ejecuta dentro de db; las claves no salen del contenedor.
set -eu
check_app() {
    MYSQL_PWD="$MARIADB_PASSWORD" mariadb --protocol=tcp -h db -u control control \
        --batch --skip-column-names -e 'SELECT 1' >/dev/null 2>&1
}
if check_app; then
    exit 0
fi
if ! MYSQL_PWD="$MARIADB_ROOT_PASSWORD" mariadb -u root -e 'SELECT 1' >/dev/null 2>&1; then
    echo 'MariaDB rechaza las credenciales. Restaurá DB_PASSWORD y DB_ROOT_PASSWORD del .env original de este volumen y repetí el arranque. No borres el volumen db_data.' >&2
    exit 1
fi
echo 'Sincronizando la contraseña de control con la configuración actual; se conservan los datos.'
# Hexadecimal evita interpolar caracteres SQL de la contraseña.
password_hex=$(printf '%s' "$MARIADB_PASSWORD" | od -An -v -tx1 | tr -d ' \n')
if ! printf "SET PASSWORD FOR 'control'@'%%' = PASSWORD(0x%s);\n" "$password_hex" |
    MYSQL_PWD="$MARIADB_ROOT_PASSWORD" mariadb -u root >/dev/null 2>&1; then
    echo 'No se pudo actualizar el usuario control. Revisá los usuarios de MariaDB con el administrador; los datos se conservan.' >&2
    exit 1
fi
if ! check_app; then
    echo 'El usuario control todavía no puede conectarse por TCP a la base control. Revisá sus permisos en MariaDB.' >&2
    exit 1
fi
