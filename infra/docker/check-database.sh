#!/bin/sh
# Se ejecuta dentro de db; las claves no salen del contenedor.
set -eu
check_app() {
    # La comprobación ocurre en el propio contenedor de MariaDB.
    MYSQL_PWD="$MARIADB_PASSWORD" mariadb --no-defaults --protocol=tcp \
        --skip-ssl -h db -u control control --connect-timeout=5 \
        --batch --skip-column-names -e 'SELECT 1' 2>&1
}
if app_error=$(check_app); then
    exit 0
fi
case "$app_error" in
    *'Access denied for user '*) ;;
    *) printf 'No se pudo comprobar la conexión a MariaDB: %s\n' "$app_error" >&2; exit 1 ;;
esac
if ! MYSQL_PWD="$MARIADB_ROOT_PASSWORD" mariadb -u root -e 'SELECT 1' >/dev/null 2>&1; then
    echo 'MariaDB rechaza las credenciales. Restaurá DB_PASSWORD y DB_ROOT_PASSWORD del .env original de este volumen y repetí el arranque. No borres el volumen db_data.' >&2
    exit 1
fi
echo 'Sincronizando la contraseña de control con la configuración actual; se conservan los datos.'
# Hexadecimal evita interpolar caracteres SQL de la contraseña.
password_hex=$(printf '%s' "$MARIADB_PASSWORD" | od -An -v -tx1 | tr -d ' \n')
# SELECT admite expresiones; SET PASSWORD requiere un literal de texto.
if ! password_hash=$(printf 'SELECT PASSWORD(0x%s);\n' "$password_hex" |
    MYSQL_PWD="$MARIADB_ROOT_PASSWORD" mariadb -u root --batch --skip-column-names 2>/dev/null); then
    echo 'No se pudo calcular la contraseña de control.' >&2
    exit 1
fi
case "$password_hash" in
    \*[0-9A-Fa-f]*) [ "${#password_hash}" -eq 41 ] || exit 1 ;;
    *) echo 'MariaDB devolvió un hash de contraseña inesperado.' >&2; exit 1 ;;
esac
case "${password_hash#\*}" in
    *[!0-9A-Fa-f]*) echo 'MariaDB devolvió un hash de contraseña inválido.' >&2; exit 1 ;;
esac
if ! printf "SET PASSWORD FOR 'control'@'%%' = '%s';\n" "$password_hash" |
    MYSQL_PWD="$MARIADB_ROOT_PASSWORD" mariadb -u root >/dev/null 2>&1; then
    echo 'No se pudo actualizar el usuario control. Revisá los usuarios de MariaDB con el administrador; los datos se conservan.' >&2
    exit 1
fi
if ! app_error=$(check_app); then
    echo 'El usuario control todavía no puede conectarse por TCP a la base control. Revisá sus permisos en MariaDB.' >&2
    printf '%s\n' "$app_error" >&2
    exit 1
fi
