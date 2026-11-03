package uy.edu.isbo.control.web.view;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.stream.Collectors;
import uy.edu.isbo.control.model.Computer;
import uy.edu.isbo.control.model.Laboratory;
import uy.edu.isbo.control.repository.ComputerRepository;
import uy.edu.isbo.control.repository.LaboratoryRepository;
import uy.edu.isbo.control.repository.UserRepository;
import uy.edu.isbo.control.repository.agent.AgentRepository;

/** Reúne la información que muestran los fragmentos JSP del panel. */
public final class DashboardData {
    private final LaboratoryRepository laboratories;
    private final ComputerRepository computers;
    private final UserRepository users;
    private final AgentRepository agents;

    public DashboardData(LaboratoryRepository laboratories,ComputerRepository computers,UserRepository users,AgentRepository agents) {
        this.laboratories=laboratories;this.computers=computers;this.users=users;this.agents=agents;
    }
    public void prepare(HttpServletRequest request,DashboardPage page) throws SQLException {
        var labs=laboratories.findAll();var pcs=computers.findAll();var states=agents.states();
        request.setAttribute("activePage",page.parameter());
        request.setAttribute("pageTitle",page.title());
        request.setAttribute("pageDescription",page.description());
        request.setAttribute("laboratories",labs);
        request.setAttribute("computers",pcs);
        request.setAttribute("states",states);
        request.setAttribute("users",users.findAll());
        request.setAttribute("pendingAgents",agents.pending());
        request.setAttribute("laboratoryNames",labs.stream().collect(Collectors.toMap(Laboratory::id,Laboratory::name)));
        request.setAttribute("computerCounts",pcs.stream().collect(Collectors.groupingBy(Computer::laboratoryId,Collectors.counting())));
        request.setAttribute("onlineCount",states.values().stream().filter(Boolean.TRUE::equals).count());
        if(page==DashboardPage.PENDING) request.setAttribute("installation",AgentInstallation.commands());
    }
}
