package uy.edu.isbo.control.agent;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

/** Guarda la identidad, la credencial y la CA pública de esta computadora. */
final class AgentConfig {
    static final Path FILE = configFile();
    private final Properties values;

    private AgentConfig(Properties values) {
        this.values = values;
    }

    String id() { return values.getProperty("id"); }
    String token() { return values.getProperty("token"); }
    String server() { return values.getProperty("server"); }
    Path ca() { return FILE.resolveSibling("ca.crt"); }
    boolean shutdownEnabled() { return Boolean.parseBoolean(values.getProperty("shutdown", "true")); }

    boolean alreadyHandled(long id) {
        return Long.parseLong(values.getProperty("lastCommand", "0")) >= id;
    }

    static AgentConfig load() throws IOException {
        if (!Files.isRegularFile(FILE)) {
            throw new IOException("El agente no está configurado. Ejecutá el instalador.");
        }
        var values = new Properties();
        try (var input = Files.newInputStream(FILE)) {
            values.load(input);
        }
        validateServer(values.getProperty("server", ""));
        UUID.fromString(values.getProperty("id", ""));
        if (!values.getProperty("token", "").matches("[a-f0-9]{64}")) {
            throw new IOException("Credencial inválida");
        }
        if (!Files.isRegularFile(FILE.resolveSibling("ca.crt"))) {
            throw new IOException("Falta la CA del servidor");
        }
        return new AgentConfig(values);
    }

    static AgentConfig configure(String server, Path caFile) throws Exception {
        server = validateServer(server);
        if (!Files.isRegularFile(caFile)) {
            throw new IOException("No encontramos el certificado ca.crt");
        }
        try (var input = Files.newInputStream(caFile)) {
            var certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
                    .generateCertificate(input);
            certificate.checkValidity();
            if (certificate.getBasicConstraints() < 0) {
                throw new IOException("El archivo no es una CA válida");
            }
            certificate.verify(certificate.getPublicKey());
        }

        // Al reinstalar conservamos la identidad, el token y el servidor anterior.
        var values = new Properties();
        if (Files.exists(FILE)) {
            try (var input = Files.newInputStream(FILE)) {
                values.load(input);
            }
        }
        String oldServer = values.getProperty("server", server);
        if (!oldServer.equals(server)) {
            throw new IllegalArgumentException("Este equipo ya pertenece a otro servidor");
        }
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        values.putIfAbsent("token", HexFormat.of().formatHex(random));
        values.putIfAbsent("id", UUID.randomUUID().toString());
        values.putIfAbsent("shutdown", "true");
        values.setProperty("server", server);

        Files.createDirectories(FILE.getParent());
        Path storedCa = FILE.resolveSibling("ca.crt");
        if (Files.exists(storedCa) &&
                !Arrays.equals(Files.readAllBytes(storedCa), Files.readAllBytes(caFile))) {
            throw new IOException("La CA del servidor cambió. Consultá al administrador antes de reinstalar.");
        }
        if (!Files.exists(storedCa)) Files.copy(caFile, storedCa);
        write(values);
        return new AgentConfig(values);
    }

    void remember(long commandId) throws IOException {
        long previous = Long.parseLong(values.getProperty("lastCommand", "0"));
        values.setProperty("lastCommand", Long.toString(Math.max(commandId, previous)));
        write(values);
    }

    private static String validateServer(String server) {
        URI url = URI.create(server);
        if (!"https".equals(url.getScheme()) || url.getHost() == null ||
                url.getUserInfo() != null || url.getQuery() != null || url.getFragment() != null ||
                (!url.getPath().isEmpty() && !"/".equals(url.getPath()))) {
            throw new IllegalArgumentException("La dirección del servidor debe ser https://IP o https://nombre");
        }
        return server.replaceAll("/+$", "");
    }

    private static Path configFile() {
        String explicit = System.getenv("CONTROL_AGENT_CONFIG");
        if (explicit != null && !explicit.isBlank()) return Path.of(explicit);

        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return Path.of(System.getenv().getOrDefault("ProgramData", "C:\\ProgramData"),
                    "ControlLaboratorios", "agent.properties");
        }
        if (os.contains("mac")) {
            return Path.of("/Library/Application Support/ControlLaboratorios/agent.properties");
        }
        return Path.of("/var/lib/control-laboratorios/agent.properties");
    }

    private static void write(Properties values) throws IOException {
        Files.createDirectories(FILE.getParent());
        Path temporary = Files.createTempFile(FILE.getParent(), ".agent-", ".tmp");
        try {
            if (Files.getFileStore(temporary).supportsFileAttributeView("posix")) {
                Files.setPosixFilePermissions(temporary, PosixFilePermissions.fromString("rw-------"));
            }
            try (var output = Files.newOutputStream(temporary)) {
                values.store(output, "Control de Laboratorios - no compartir");
            }
            try {
                Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
