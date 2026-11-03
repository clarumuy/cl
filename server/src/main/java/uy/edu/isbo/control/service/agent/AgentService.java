package uy.edu.isbo.control.service.agent;

import java.sql.SQLException;
import java.util.UUID;
import uy.edu.isbo.control.repository.agent.AgentRepository;
import uy.edu.isbo.control.repository.agent.CommandRepository;

public final class AgentService {
    private final AgentRepository agents;
    private final CommandRepository commands;
    public AgentService(AgentRepository agents,CommandRepository commands) {this.agents=agents;this.commands=commands;}

    public String poll(String id,String name,String token) throws SQLException {
        validate(id);
        if(name==null || name.isBlank() || name.length()>100 || name.codePoints().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Nombre del agente inválido");
        if(!agents.registerOrTouch(id,name.strip(),token)) throw new SecurityException("Credenciales rechazadas");
        return commands.pending(id).map(offer -> offer.wire()).orElse("none");
    }

    public boolean accept(String id,long commandId,String token) throws SQLException {
        validate(id);
        return agents.authorized(id,token) && commands.accept(id,commandId);
    }

    public void approve(String id,long laboratoryId) throws SQLException {validate(id);agents.approve(id,laboratoryId);}
    public void reject(String id) throws SQLException {validate(id);agents.reject(id);}

    private static void validate(String id) {
        try {
            if(id==null || !UUID.fromString(id).toString().equals(id)) throw new IllegalArgumentException();
        } catch(IllegalArgumentException e) {throw new IllegalArgumentException("Identidad de agente inválida");}
    }
}
