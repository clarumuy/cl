package uy.edu.isbo.control.web.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import uy.edu.isbo.control.repository.*;
import uy.edu.isbo.control.repository.agent.*;
import uy.edu.isbo.control.security.Access;
import uy.edu.isbo.control.web.view.*;

/** Controlador MVC: recibe HTTP y delega la lectura o escritura de los datos. */
@WebServlet("/app")
public final class AppServlet extends HttpServlet {
    private final LaboratoryRepository laboratories=new LaboratoryRepository();
    private final ComputerRepository computers=new ComputerRepository();
    private final UserRepository users=new UserRepository();
    private final AgentRepository agents=new AgentRepository();
    private final CommandRepository commands=new CommandRepository();
    private final Access access=new Access(users);
    private final AdminActions actions=new AdminActions(laboratories,computers,users,agents,commands);
    private final DashboardData dashboard=new DashboardData(laboratories,computers,users,agents);

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        res.setHeader("Cache-Control","no-store");
        try {
            if(!access.loggedIn(req)) {
                req.setAttribute("csrf",access.csrf(req));view(req,res,"login");return;
            }
            dashboard.prepare(req,DashboardPage.from(req.getParameter("page")));
            req.setAttribute("csrf",access.csrf(req));
            var session=req.getSession(false);
            req.setAttribute("message",session.getAttribute("message"));session.removeAttribute("message");
            view(req,res,"dashboard");
        } catch(SQLException e) {getServletContext().log("Error en panel",e);res.sendError(503,"Datos no disponibles");}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res) throws IOException {
        req.setCharacterEncoding("UTF-8");
        if("login".equals(req.getParameter("action"))) {access.login(req,res);return;}
        try {
            if(!access.loggedIn(req)) {res.sendError(401);return;}
            if(!access.validCsrf(req)) {res.sendError(403);return;}
            if("logout".equals(req.getParameter("action"))) {req.getSession(false).invalidate();res.sendRedirect(req.getContextPath()+"/app");return;}
            try {req.getSession(false).setAttribute("message",actions.execute(req));}
            catch(IllegalArgumentException | IllegalStateException e) {req.getSession(false).setAttribute("message","No se pudo completar: "+e.getMessage());}
            catch(SQLException e) {getServletContext().log("Error de administración",e);req.getSession(false).setAttribute("message","No se pudo completar. Revisá los datos.");}
            res.sendRedirect(req.getContextPath()+"/app?page="+DashboardPage.from(req.getParameter("page")).parameter());
        } catch(SQLException e) {getServletContext().log("Error de sesión",e);res.sendError(503);}
    }
    private void view(HttpServletRequest req,HttpServletResponse res,String name) throws ServletException,IOException {
        req.getRequestDispatcher("/WEB-INF/views/"+name+".jsp").forward(req,res);
    }
}
