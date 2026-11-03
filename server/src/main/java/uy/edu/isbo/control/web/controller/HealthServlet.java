package uy.edu.isbo.control.web.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import uy.edu.isbo.control.config.Database;

@WebServlet("/health")
public final class HealthServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/plain; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        try (var db = Database.INSTANCE.connect()) {
            if (!db.isValid(2)) {
                throw new IOException("Base no disponible");
            }
            response.getWriter().print("ok");
        } catch (Exception e) {
            response.setStatus(503);
            response.getWriter().print("El servidor todavía no está listo");
        }
    }
}
