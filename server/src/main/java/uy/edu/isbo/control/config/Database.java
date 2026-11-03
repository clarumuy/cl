package uy.edu.isbo.control.config;

import java.sql.Connection;
import java.sql.SQLException;
import org.mariadb.jdbc.MariaDbPoolDataSource;

public enum Database {
    INSTANCE;

    private final MariaDbPoolDataSource dataSource;

    Database() {
        try {
            var pool = new MariaDbPoolDataSource();
            pool.setUser(System.getenv().getOrDefault("DB_USER", "control"));
            pool.setPassword(System.getenv("DB_PASSWORD"));
            pool.setUrl(
                    System.getenv()
                            .getOrDefault("DB_URL", "jdbc:mariadb://localhost:3306/control"));
            dataSource = pool;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo inicializar el pool de conexiones", e);
        }
    }

    public Connection connect() throws SQLException {
        return dataSource.getConnection();
    }
}
