package uy.edu.isbo.control.repository.agent;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uy.edu.isbo.control.config.Database;
import uy.edu.isbo.control.model.agent.PendingAgent;
import uy.edu.isbo.control.security.AgentCredentials;

public final class AgentRepository {
    /** Un UUID conocido nunca puede reclamar una credencial distinta. */
    public boolean registerOrTouch(String id, String name, String token) throws SQLException {
        try (var connection=Database.INSTANCE.connect()) {
            connection.setAutoCommit(false);
            try {
                String hash=null;
                boolean exists=false, revoked=false;
                try (var statement=connection.prepareStatement("SELECT token_hash,revoked FROM agent_nodes WHERE id=? FOR UPDATE")) {
                    statement.setString(1,id);
                    try (var rows=statement.executeQuery()) {
                        if (rows.next()) { exists=true; hash=rows.getString(1); revoked=rows.getBoolean(2); }
                    }
                }
                if (exists && (revoked || !AgentCredentials.matches(hash,token))) {
                    connection.rollback(); return false;
                }
                if (exists) {
                    try (var statement=connection.prepareStatement("UPDATE agent_nodes SET name=?,last_seen=UTC_TIMESTAMP(3) WHERE id=?")) {
                        statement.setString(1,name);statement.setString(2,id);statement.executeUpdate();
                    }
                } else {
                    try (var statement=connection.prepareStatement("INSERT INTO agent_nodes(id,name,last_seen,token_hash) VALUES(?,?,UTC_TIMESTAMP(3),?)")) {
                        statement.setString(1,id);statement.setString(2,name);statement.setString(3,AgentCredentials.hash(token));statement.executeUpdate();
                    }
                }
                connection.commit();return true;
            } catch(SQLException | RuntimeException e) { connection.rollback();throw e; }
        }
    }

    public boolean authorized(String id, String token) throws SQLException {
        try (var connection=Database.INSTANCE.connect();
             var statement=connection.prepareStatement("SELECT token_hash FROM agent_nodes WHERE id=? AND revoked=FALSE AND computer_id IS NOT NULL")) {
            statement.setString(1,id);
            try (var rows=statement.executeQuery()) { return rows.next() && AgentCredentials.matches(rows.getString(1),token); }
        }
    }

    public List<PendingAgent> pending() throws SQLException {
        var result=new ArrayList<PendingAgent>();
        try (var connection=Database.INSTANCE.connect();
             var statement=connection.prepareStatement("SELECT id,name,COALESCE(CAST(last_seen AS CHAR),'') FROM agent_nodes WHERE computer_id IS NULL AND revoked=FALSE ORDER BY last_seen DESC");
             var rows=statement.executeQuery()) {
            while(rows.next()) result.add(new PendingAgent(rows.getString(1),rows.getString(2),rows.getString(3)));
        }
        return result;
    }

    public Map<Long,Boolean> states() throws SQLException {
        var states=new HashMap<Long,Boolean>();
        try (var connection=Database.INSTANCE.connect();
             var statement=connection.prepareStatement("SELECT computer_id,last_seen > UTC_TIMESTAMP(3) - INTERVAL 20 SECOND FROM agent_nodes WHERE computer_id IS NOT NULL AND revoked=FALSE");
             var rows=statement.executeQuery()) {
            while(rows.next()) states.put(rows.getLong(1),rows.getBoolean(2));
        }
        return states;
    }

    public boolean linked(long computerId) throws SQLException {
        try (var connection=Database.INSTANCE.connect();var statement=connection.prepareStatement("SELECT 1 FROM agent_nodes WHERE computer_id=? AND revoked=FALSE")) {
            statement.setLong(1,computerId);try(var rows=statement.executeQuery()) {return rows.next();}
        }
    }

    public void approve(String id,long laboratoryId) throws SQLException {
        try (var connection=Database.INSTANCE.connect()) {
            connection.setAutoCommit(false);
            try {
                String name;
                try (var statement=connection.prepareStatement("SELECT name FROM agent_nodes WHERE id=? AND computer_id IS NULL AND revoked=FALSE FOR UPDATE")) {
                    statement.setString(1,id);
                    try(var rows=statement.executeQuery()) { if (!rows.next()) throw new IllegalArgumentException("Agente no disponible");name=rows.getString(1); }
                }
                long computerId;
                try (var statement=connection.prepareStatement("INSERT INTO computers(name,ip,laboratory_id) VALUES(?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1,name);statement.setString(2,"agent:"+id);statement.setLong(3,laboratoryId);statement.executeUpdate();
                    try(var rows=statement.getGeneratedKeys()) {if (!rows.next()) throw new SQLException("No se generó el ID del equipo");computerId=rows.getLong(1);}
                }
                try(var statement=connection.prepareStatement("UPDATE agent_nodes SET computer_id=? WHERE id=?")) {
                    statement.setLong(1,computerId);statement.setString(2,id);statement.executeUpdate();
                }
                connection.commit();
            } catch(SQLException | RuntimeException e) {connection.rollback();throw e;}
        }
    }

    public void reject(String id) throws SQLException {
        try(var connection=Database.INSTANCE.connect();var statement=connection.prepareStatement("UPDATE agent_nodes SET revoked=TRUE WHERE id=? AND computer_id IS NULL")) {
            statement.setString(1,id);
            if(statement.executeUpdate()==0) throw new IllegalArgumentException("Solicitud inexistente");
        }
    }
}
