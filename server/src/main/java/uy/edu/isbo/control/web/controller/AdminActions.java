package uy.edu.isbo.control.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import uy.edu.isbo.control.repository.*;
import uy.edu.isbo.control.repository.agent.*;
import uy.edu.isbo.control.service.*;
import uy.edu.isbo.control.service.agent.*;
import uy.edu.isbo.control.web.support.FormFields;

/** Solo distribuye acciones de formularios; las reglas están en los servicios. */
final class AdminActions {
    private final LaboratoryService laboratories;
    private final ComputerService computers;
    private final UserService users;
    private final AgentService agents;
    private final ShutdownService shutdown;

    AdminActions(LaboratoryRepository labs,ComputerRepository pcs,UserRepository users,AgentRepository agents,CommandRepository commands) {
        laboratories=new LaboratoryService(labs);
        computers=new ComputerService(pcs,agents);
        this.users=new UserService(users);
        this.agents=new AgentService(agents,commands);
        shutdown=new ShutdownService(pcs,labs,commands);
    }
    String execute(HttpServletRequest req) throws SQLException {
        return switch(req.getParameter("action") == null ? "" : req.getParameter("action")) {
            case "save-lab" -> {laboratories.save(id(req),field(req,"name",100),field(req,"room",80));yield "Laboratorio guardado";}
            case "delete-lab" -> {laboratories.delete(requiredId(req));yield "Laboratorio eliminado";}
            case "save-computer" -> {computers.save(id(req),field(req,"name",100),req.getParameter("ip"),FormFields.positiveId(req.getParameter("laboratoryId")));yield "Computadora guardada";}
            case "delete-computer" -> {computers.delete(requiredId(req));yield "Computadora eliminada";}
            case "save-user" -> {users.save(id(req),field(req,"username",60),req.getParameter("password"));yield "Usuario guardado";}
            case "delete-user" -> {users.delete(requiredId(req),(Long)req.getSession(false).getAttribute("userId"));yield "Usuario eliminado";}
            case "approve-agent" -> {agents.approve(field(req,"agentId",36),FormFields.positiveId(req.getParameter("laboratoryId")));yield "Equipo vinculado";}
            case "reject-agent" -> {agents.reject(field(req,"agentId",36));yield "Solicitud rechazada";}
            case "shutdown" -> shutdown.computer(requiredId(req));
            case "shutdown-lab" -> shutdown.laboratory(requiredId(req));
            default -> throw new IllegalArgumentException("Acción inválida");
        };
    }
    private static long id(HttpServletRequest req) {return FormFields.optionalId(req.getParameter("id"));}
    private static long requiredId(HttpServletRequest req) {return FormFields.positiveId(req.getParameter("id"));}
    private static String field(HttpServletRequest req,String key,int max) {return FormFields.required(req.getParameter(key),key,max);}
}
