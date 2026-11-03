package uy.edu.isbo.control.agent;

import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.time.Duration;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

/** Verifica HTTPS usando exclusivamente la CA local de nuestra instalación. */
final class AgentTls {
    private AgentTls() {}

    static HttpClient create(Path caPath) throws Exception {
        var store = KeyStore.getInstance(KeyStore.getDefaultType());
        store.load(null, null);
        try (var input = Files.newInputStream(caPath)) {
            var certificate = CertificateFactory.getInstance("X.509").generateCertificate(input);
            store.setCertificateEntry("laboratorio", certificate);
        }

        var trust = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trust.init(store);
        var context = SSLContext.getInstance("TLS");
        context.init(null, trust.getTrustManagers(), null);

        var parameters = new SSLParameters();
        parameters.setEndpointIdentificationAlgorithm("HTTPS");
        return HttpClient.newBuilder()
                .sslContext(context)
                .sslParameters(parameters)
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }
}
