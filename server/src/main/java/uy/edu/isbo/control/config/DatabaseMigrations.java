package uy.edu.isbo.control.config;

import java.sql.SQLException;

/** Adapta bases instaladas con versiones anteriores sin borrar equipos ni usuarios. */
public final class DatabaseMigrations {
    private DatabaseMigrations() {}

    public static void apply() throws SQLException {
        try(var connection=Database.INSTANCE.connect();var statement=connection.createStatement()) {
            // MariaDB permite IF NOT EXISTS; una instalación nueva ya trae estas columnas.
            statement.executeUpdate("ALTER TABLE agent_commands ADD COLUMN IF NOT EXISTS status VARCHAR(12) NOT NULL DEFAULT 'pending'");
            statement.executeUpdate("ALTER TABLE agent_commands ADD COLUMN IF NOT EXISTS execute_at DATETIME(3) NULL");
            statement.executeUpdate("ALTER TABLE agent_commands ADD COLUMN IF NOT EXISTS expires_at DATETIME(3) NULL");
            statement.executeUpdate("ALTER TABLE agent_commands ADD COLUMN IF NOT EXISTS accepted_at DATETIME(3) NULL");
            // Las órdenes del protocolo viejo no se deben ejecutar después de actualizar.
            statement.executeUpdate("UPDATE agent_commands SET status='expired',execute_at=UTC_TIMESTAMP(3),expires_at=UTC_TIMESTAMP(3) WHERE execute_at IS NULL OR expires_at IS NULL");
        }
    }
}
