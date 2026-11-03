package uy.edu.isbo.control.web.support;

/** Valida entradas externas antes de usarlas en servicios o SQL. */
public final class FormFields {
    private FormFields() {}

    public static String required(String value, String name) { return required(value, name, 100); }
    public static String required(String value, String name, int max) {
        if (value == null || value.isBlank() || value.length() > max || value.codePoints().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Campo inválido: " + name);
        return value.strip();
    }
    public static long optionalId(String value) {
        return value == null || value.isBlank() || "0".equals(value) ? 0 : positiveId(value);
    }
    public static long positiveId(String value) {
        try {
            long id = Long.parseLong(value);
            if (id > 0) return id;
        } catch (NumberFormatException ignored) { /* ID recibido desde un formulario. */ }
        throw new IllegalArgumentException("Identificador inválido");
    }
}
