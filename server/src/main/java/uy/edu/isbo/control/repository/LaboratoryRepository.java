package uy.edu.isbo.control.repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import uy.edu.isbo.control.config.Database;
import uy.edu.isbo.control.model.Laboratory;

public final class LaboratoryRepository {
    public List<Laboratory> findAll() throws SQLException {
        var result = new ArrayList<Laboratory>();
        try (var connection = Database.INSTANCE.connect();
             var statement = connection.prepareStatement("SELECT id,name,room FROM laboratories ORDER BY name");
             var rows = statement.executeQuery()) {
            while (rows.next()) result.add(new Laboratory(rows.getLong(1), rows.getString(2), rows.getString(3)));
        }
        return result;
    }

    public Optional<Laboratory> findById(long id) throws SQLException {
        try (var connection = Database.INSTANCE.connect();
             var statement = connection.prepareStatement("SELECT id,name,room FROM laboratories WHERE id=?")) {
            statement.setLong(1, id);
            try (var rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(new Laboratory(rows.getLong(1), rows.getString(2), rows.getString(3))) : Optional.empty();
            }
        }
    }

    public void save(long id, String name, String room) throws SQLException {
        if (id == 0) Sql.execute("INSERT INTO laboratories(name,room) VALUES (?,?)", name, room);
        else if (Sql.execute("UPDATE laboratories SET name=?,room=? WHERE id=?", name, room, id) == 0)
            throw new IllegalArgumentException("Laboratorio inexistente");
    }

    public void delete(long id) throws SQLException {
        if (Sql.execute("DELETE FROM laboratories WHERE id=?", id) == 0)
            throw new IllegalArgumentException("Laboratorio inexistente");
    }
}
