package uy.edu.isbo.control.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import uy.edu.isbo.control.repository.UserRepository;
import uy.edu.isbo.control.security.Passwords;

@WebListener
public final class Bootstrap implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            var users = new UserRepository();
            DatabaseMigrations.apply();

            if (users.findAll().isEmpty()) {
                var password = System.getenv("ADMIN_PASSWORD");
                if (password == null || password.length() < 12) {
                    throw new IllegalStateException(
                            "ADMIN_PASSWORD debe tener al menos 12 caracteres");
                }

                users.save(0, "admin", Passwords.hash(password));
            }

        } catch (Exception e) {
            throw new IllegalStateException("No se pudo inicializar la aplicación", e);
        }
    }
}
