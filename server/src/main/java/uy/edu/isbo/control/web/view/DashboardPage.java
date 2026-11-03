package uy.edu.isbo.control.web.view;

import java.util.Arrays;

public enum DashboardPage {
    OVERVIEW("overview", "Resumen", "Una vista general de los laboratorios y sus equipos."),
    LABORATORIES(
            "labs",
            "Laboratorios",
            "Organizá las salas y administrá los equipos de cada laboratorio."),
    COMPUTERS(
            "pcs", "Computadoras", "Consultá la conexión de los equipos y administrá su registro."),
    PENDING("pending", "Vincular equipos", "Asigná los equipos detectados a un laboratorio."),
    USERS("users", "Usuarios", "Administrá las cuentas con acceso al sistema.");

    private final String parameter;
    private final String title;
    private final String description;

    DashboardPage(String parameter, String title, String description) {
        this.parameter = parameter;
        this.title = title;
        this.description = description;
    }

    public String parameter() {
        return parameter;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public static DashboardPage from(String parameter) {
        return Arrays.stream(values())
                .filter(page -> page.parameter.equals(parameter))
                .findFirst()
                .orElse(OVERVIEW);
    }
}
