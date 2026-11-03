package uy.edu.isbo.control.web.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import uy.edu.isbo.control.repository.agent.AgentRepository;
import uy.edu.isbo.control.repository.agent.CommandRepository;
import uy.edu.isbo.control.service.agent.AgentService;

/** Dos operaciones pequeñas: consultar orden y confirmar su recepción. */
@WebServlet("/agent-api/*")
public final class AgentServlet extends HttpServlet {
    private final AgentService agents=new AgentService(new AgentRepository(),new CommandRepository());

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res) throws IOException {
        req.setCharacterEncoding("UTF-8");res.setHeader("Cache-Control","no-store");res.setContentType("text/plain; charset=UTF-8");
        if(req.getContentType()==null || !req.getContentType().toLowerCase(Locale.ROOT).startsWith("application/x-www-form-urlencoded")) {res.sendError(415);return;}
        String authorization=req.getHeader("Authorization");
        String token=authorization!=null && authorization.startsWith("Bearer ") ? authorization.substring(7):"";
        if(!token.matches("[A-Za-z0-9._~-]{24,512}")) {res.sendError(401);return;}
        try {
            String id=req.getParameter("id");
            switch(req.getPathInfo()==null ? "" : req.getPathInfo()) {
                case "/poll" -> res.getWriter().print(agents.poll(id,req.getParameter("name"),token));
                case "/ack" -> {
                    long commandId=Long.parseLong(req.getParameter("commandId"));
                    if(commandId<=0) throw new IllegalArgumentException("Orden inválida");
                    if(agents.accept(id,commandId,token)) res.getWriter().print("accepted");
                    else res.sendError(409,"Orden vencida o credenciales no autorizadas");
                }
                default -> res.sendError(404);
            }
        } catch(IllegalArgumentException e) {res.sendError(400);}
        catch(SecurityException e) {res.sendError(401);}
        catch(SQLException e) {getServletContext().log("Error de comunicación con agente",e);res.sendError(503);}
    }
}
