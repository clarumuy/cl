package uy.edu.isbo.control.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import uy.edu.isbo.control.config.Database;

/** Utilidades JDBC. Los repositorios siguen siendo responsables de sus consultas. */
final class Sql {
    private Sql() {}

    static int execute(String sql, Object... values) throws SQLException {
        try (var connection = Database.INSTANCE.connect(); var statement = connection.prepareStatement(sql)) {
            bind(statement, values);
            return statement.executeUpdate();
        }
    }

    static void bind(PreparedStatement statement, Object... values) throws SQLException {
        for (int index = 0; index < values.length; index++) {
            statement.setObject(index + 1, values[index]);
        }
    }
}
