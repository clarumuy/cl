package uy.edu.isbo.control.agent;

import java.util.Locale;

/** Solo permite un apagado normal, nunca ejecutar órdenes arbitrarias de la red. */
final class ShutdownExecutor {
    private ShutdownExecutor() {}

    static void shutdown() throws Exception {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String[] command;
        if (os.contains("win")) {
            command = new String[] {"shutdown.exe", "/s", "/t", "0"};
        } else if (os.contains("mac")) {
            command = new String[] {"/sbin/shutdown", "-h", "now"};
        } else {
            command = new String[] {"/usr/sbin/shutdown", "-h", "now"};
        }

        var process = new ProcessBuilder(command).inheritIO().start();
        if (process.waitFor() != 0) {
            throw new IllegalStateException("El sistema operativo rechazó el apagado");
        }
    }
}
