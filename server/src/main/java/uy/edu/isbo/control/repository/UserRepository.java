package uy.edu.isbo.control.repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import uy.edu.isbo.control.config.Database;
import uy.edu.isbo.control.model.User;

public final class UserRepository {
    public List<User> findAll() throws SQLException {
        var users = new ArrayList<User>();
        try (var connection = Database.INSTANCE.connect();
             var statement = connection.prepareStatement("SELECT id,username,password_hash FROM users ORDER BY username");
             var rows = statement.executeQuery()) {
            while (rows.next()) users.add(new User(rows.getLong(1),rows.getString(2),rows.getString(3)));
        }
        return users;
    }

    public Optional<User> findByName(String name) throws SQLException {
        try (var connection = Database.INSTANCE.connect();
             var statement = connection.prepareStatement("SELECT id,username,password_hash FROM users WHERE username=?")) {
            statement.setString(1,name);
            try (var rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(new User(rows.getLong(1),rows.getString(2),rows.getString(3))) : Optional.empty();
            }
        }
    }

    public boolean exists(long id) throws SQLException {
        try (var connection = Database.INSTANCE.connect(); var statement = connection.prepareStatement("SELECT 1 FROM users WHERE id=?")) {
            statement.setLong(1,id); try (var rows=statement.executeQuery()) { return rows.next(); }
        }
    }

    public void save(long id, String username, String hash) throws SQLException {
        if (id == 0) Sql.execute("INSERT INTO users(username,password_hash) VALUES (?,?)", username, hash);
        else if ((hash == null ? Sql.execute("UPDATE users SET username=? WHERE id=?",username,id)
                : Sql.execute("UPDATE users SET username=?,password_hash=? WHERE id=?",username,hash,id)) == 0)
            throw new IllegalArgumentException("Usuario inexistente");
    }

    public void delete(long id, long currentUserId) throws SQLException {
        try (var connection = Database.INSTANCE.connect()) {
            connection.setAutoCommit(false);
            try {
                int count = 0;
                // Bloqueamos filas para que no se eliminen al mismo tiempo las últimas cuentas.
                try (var statement=connection.prepareStatement("SELECT id FROM users FOR UPDATE");var rows=statement.executeQuery()) {
                    while(rows.next()) count++;
                }
                if (count <= 1 || id == currentUserId)
                    throw new IllegalArgumentException("No podés borrar tu cuenta ni el último usuario");
                try (var statement=connection.prepareStatement("DELETE FROM users WHERE id=?")) {
                    statement.setLong(1,id);
                    if (statement.executeUpdate() == 0) throw new IllegalArgumentException("Usuario inexistente");
                }
                connection.commit();
            } catch(SQLException | RuntimeException e) { connection.rollback(); throw e; }
        }
    }
}
