package uy.edu.isbo.control.repository.agent;

import java.sql.SQLException;
import java.util.Optional;
import uy.edu.isbo.control.config.Database;
import uy.edu.isbo.control.model.agent.CommandOffer;

/** Guarda órdenes en MariaDB, no en la memoria del servidor. */
public final class CommandRepository {
    /** Se prepara una única orden para un equipo que respondió recientemente. */
    public boolean queueComputer(long computerId) throws SQLException {
        return queue("a.computer_id=?",computerId,15)==1;
    }

    /** Un único INSERT asigna el mismo instante objetivo a toda la sala. */
    public int queueLaboratory(long laboratoryId) throws SQLException {
        return queue("c.laboratory_id=?",laboratoryId,25);
    }

    private int queue(String condition,long id,int seconds) throws SQLException {
        String sql = """
            INSERT INTO agent_commands(agent_id,action,status,execute_at,expires_at)
            SELECT a.id,'shutdown','pending',
                DATE_ADD(UTC_TIMESTAMP(3),INTERVAL ? SECOND),
                DATE_ADD(UTC_TIMESTAMP(3),INTERVAL ? SECOND)
            FROM agent_nodes a JOIN computers c ON c.id=a.computer_id
            WHERE %s AND a.revoked=FALSE
                AND a.last_seen > UTC_TIMESTAMP(3) - INTERVAL 20 SECOND
                AND NOT EXISTS (
                  SELECT 1 FROM agent_commands cmd WHERE cmd.agent_id=a.id
                    AND cmd.status IN ('pending','accepted')
                    AND cmd.expires_at > UTC_TIMESTAMP(3)
                )
            """.formatted(condition);
        try(var connection=Database.INSTANCE.connect();var statement=connection.prepareStatement(sql)) {
            statement.setInt(1,seconds);statement.setInt(2,seconds+15);statement.setLong(3,id);
            return statement.executeUpdate();
        }
    }

    public Optional<CommandOffer> pending(String agentId) throws SQLException {
        try(var connection=Database.INSTANCE.connect();
            var statement=connection.prepareStatement("""
                SELECT c.id, GREATEST(0,TIMESTAMPDIFF(MICROSECOND,UTC_TIMESTAMP(3),c.execute_at) DIV 1000)
                FROM agent_commands c JOIN agent_nodes a ON a.id=c.agent_id
                WHERE c.agent_id=? AND c.status='pending' AND a.revoked=FALSE
                    AND a.computer_id IS NOT NULL AND c.execute_at>UTC_TIMESTAMP(3)
                ORDER BY c.id LIMIT 1
                """)) {
            statement.setString(1,agentId);
            try(var rows=statement.executeQuery()) {
                return rows.next()?Optional.of(new CommandOffer(rows.getLong(1),rows.getLong(2))):Optional.empty();
            }
        }
    }

    /** Si falla la respuesta del ACK, el agente puede repetirlo con el mismo ID. */
    public boolean accept(String agentId,long commandId) throws SQLException {
        try(var connection=Database.INSTANCE.connect()) {
            connection.setAutoCommit(false);
            try {
                int changed;
                try(var statement=connection.prepareStatement("""
                    UPDATE agent_commands SET status='accepted',accepted_at=UTC_TIMESTAMP(3)
                    WHERE id=? AND agent_id=? AND status='pending'
                      AND execute_at>UTC_TIMESTAMP(3) AND expires_at>UTC_TIMESTAMP(3)
                    """)) {
                    statement.setLong(1,commandId);statement.setString(2,agentId);changed=statement.executeUpdate();
                }
                boolean accepted=changed==1;
                if (!accepted) {
                    try(var statement=connection.prepareStatement("""
                        SELECT 1 FROM agent_commands WHERE id=? AND agent_id=?
                        AND status='accepted' AND execute_at>UTC_TIMESTAMP(3)
                        """)) {
                        statement.setLong(1,commandId);statement.setString(2,agentId);
                        try(var rows=statement.executeQuery()) {accepted=rows.next();}
                    }
                }
                connection.commit();return accepted;
            } catch(SQLException | RuntimeException e) {connection.rollback();throw e;}
        }
    }
}
