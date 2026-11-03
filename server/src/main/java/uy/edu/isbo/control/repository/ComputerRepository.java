package uy.edu.isbo.control.repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import uy.edu.isbo.control.config.Database;
import uy.edu.isbo.control.model.Computer;

public final class ComputerRepository {
    public List<Computer> findAll() throws SQLException {
        var result = new ArrayList<Computer>();
        try (var connection = Database.INSTANCE.connect();
             var statement = connection.prepareStatement("SELECT id,name,ip,laboratory_id FROM computers ORDER BY name");
             var rows = statement.executeQuery()) {
            while (rows.next()) result.add(read(rows));
        }
        return result;
    }

    public List<Computer> findByLaboratory(long laboratoryId) throws SQLException {
        var result = new ArrayList<Computer>();
        try (var connection = Database.INSTANCE.connect();
             var statement = connection.prepareStatement("SELECT id,name,ip,laboratory_id FROM computers WHERE laboratory_id=? ORDER BY name")) {
            statement.setLong(1,laboratoryId);
            try (var rows = statement.executeQuery()) { while (rows.next()) result.add(read(rows)); }
        }
        return result;
    }

    public Optional<Computer> findById(long id) throws SQLException {
        try (var connection = Database.INSTANCE.connect();
             var statement = connection.prepareStatement("SELECT id,name,ip,laboratory_id FROM computers WHERE id=?")) {
            statement.setLong(1,id);
            try (var rows = statement.executeQuery()) { return rows.next() ? Optional.of(read(rows)) : Optional.empty(); }
        }
    }

    private static Computer read(java.sql.ResultSet row) throws SQLException {
        return new Computer(row.getLong(1), row.getString(2), row.getString(3), row.getLong(4));
    }

    public void save(long id, String name, String ip, long laboratoryId) throws SQLException {
        if (id == 0) Sql.execute("INSERT INTO computers(name,ip,laboratory_id) VALUES (?,?,?)", name, ip, laboratoryId);
        else if (Sql.execute("UPDATE computers SET name=?,ip=?,laboratory_id=? WHERE id=?",name,ip,laboratoryId,id) == 0)
            throw new IllegalArgumentException("Computadora inexistente");
    }

    public void updateLinked(long id, String name, long laboratoryId) throws SQLException {
        if (Sql.execute("UPDATE computers SET name=?,laboratory_id=? WHERE id=?",name,laboratoryId,id) == 0)
            throw new IllegalArgumentException("Computadora inexistente");
    }

    public void delete(long id) throws SQLException {
        try (var connection = Database.INSTANCE.connect()) {
            connection.setAutoCommit(false);
            try {
                // Un equipo eliminado no vuelve a aparecer como solicitud pendiente.
                try (var statement = connection.prepareStatement("UPDATE agent_nodes SET revoked=TRUE,computer_id=NULL WHERE computer_id=?")) {
                    statement.setLong(1,id); statement.executeUpdate();
                }
                try (var statement = connection.prepareStatement("DELETE FROM computers WHERE id=?")) {
                    statement.setLong(1,id);
                    if (statement.executeUpdate() == 0) throw new IllegalArgumentException("Computadora inexistente");
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) { connection.rollback(); throw e; }
        }
    }
}
