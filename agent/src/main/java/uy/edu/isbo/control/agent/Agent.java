package uy.edu.isbo.control.agent;

import java.net.InetAddress;
import java.nio.file.Path;

/** Consulta órdenes desde que arranca el equipo hasta que se apaga. */
public final class Agent {
    private Agent() {}

    public static void main(String[] args) {
        try {
            run(args);
        } catch (Exception e) {
            System.err.println("Agente: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void run(String[] args) throws Exception {
        if (args.length == 3 && args[0].equals("--configure")) {
            var config = AgentConfig.configure(args[1], Path.of(args[2]));
            System.out.println("Equipo configurado: " + config.id());
            return;
        }
        if (args.length != 0) {
            throw new IllegalArgumentException("Uso: ControlAgent [--configure https://servidor ca.crt]");
        }

        var config = AgentConfig.load();
        var client = new AgentClient(AgentTls.create(config.ca()), config);
        String name = System.getenv().getOrDefault("COMPUTERNAME", InetAddress.getLocalHost().getHostName());
        System.out.println("Agente iniciado: " + name);

        while (true) {
            try {
                var offer = client.poll(name);
                if (offer != null && !config.alreadyHandled(offer.id())) {
                    if (!config.shutdownEnabled()) {
                        System.err.println("Apagado deshabilitado localmente");
                    } else if (client.acknowledge(offer.id())) {
                        // Guardamos el ID antes de ejecutar para no repetir la orden tras reiniciar.
                        config.remember(offer.id());
                        Thread.sleep(offer.delayMillis());
                        ShutdownExecutor.shutdown();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                System.err.println("Comunicación: " + e.getMessage());
            }
            Thread.sleep(5_000);
        }
    }
}
