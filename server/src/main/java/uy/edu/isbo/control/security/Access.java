package uy.edu.isbo.control.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;
import uy.edu.isbo.control.repository.UserRepository;
import uy.edu.isbo.control.security.LoginThrottle;
import uy.edu.isbo.control.security.Passwords;

/** Autenticación, protección CSRF y sesiones. Ninguna operación modifica datos sin sesión. */
public final class Access {
    private static final LoginThrottle THROTTLE = new LoginThrottle();
    private final UserRepository store;

    public Access(UserRepository store) { this.store = store; }

    public boolean loggedIn(HttpServletRequest request) throws SQLException {
        var session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Long id)) return false;
        if (store.exists(id)) return true;
        session.invalidate(); // La cuenta pudo eliminarse desde otro navegador.
        return false;
    }

    public String csrf(HttpServletRequest request) {
        var session = request.getSession(true);
        var csrf = (String) session.getAttribute("csrf");
        if (csrf == null) {
            csrf = UUID.randomUUID().toString();
            session.setAttribute("csrf", csrf);
        }
        return csrf;
    }

    public boolean validCsrf(HttpServletRequest request) {
        var session = request.getSession(false);
        return session != null && request.getParameter("csrf") != null
                && Objects.equals(session.getAttribute("csrf"), request.getParameter("csrf"));
    }

    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-store");
        if (!validCsrf(request)) { response.sendError(403); return; }
        var address = clientAddress(request);
        var now = System.currentTimeMillis();
        if (THROTTLE.locked(address, now)) { redirect(request, response, "?error=locked"); return; }
        try {
            var username = request.getParameter("username");
            var password = request.getParameter("password");
            if (username != null && username.length() <= 60 && password != null && password.length() <= 1024) {
                var user = store.findByName(username);
                if (user.isPresent() && Passwords.matches(password, user.get().passwordHash())) {
                    THROTTLE.succeeded(address);
                    var previous = request.getSession(false);
                    if (previous != null) previous.invalidate(); // Evita fijación de sesión.
                    var session = request.getSession(true);
                    session.setAttribute("userId", user.get().id());
                    session.setAttribute("csrf", UUID.randomUUID().toString());
                    session.setMaxInactiveInterval(1800);
                    redirect(request, response, "");
                    return;
                }
            }
        } catch (SQLException e) {
            request.getServletContext().log("Error al consultar usuario", e);
        }
        THROTTLE.failed(address, now);
        redirect(request, response, "?error=1");
    }

    private static String clientAddress(HttpServletRequest req) {
        // El puerto de Tomcat no está publicado. Caddy genera X-Forwarded-For.
        var forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && forwarded.length() <= 200) {
            var first = forwarded.split(",", 2)[0].strip();
            if (first.matches("[0-9a-fA-F:.]{3,45}")) return first;
        }
        return req.getRemoteAddr();
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response, String suffix)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/app" + suffix);
    }
}
