package uy.edu.isbo.control.web.view;

/** Instrucciones para el paquete oficial de GitHub Releases, ya extraído. */
public record AgentInstallation(String windows,String unix,String error) {
    public static AgentInstallation commands() {
        String host=System.getenv("PUBLIC_SERVER_URL");
        if(host==null || !host.matches("https://[a-zA-Z0-9.-]+"))
            return new AgentInstallation("","","Configurá la dirección LAN del servidor para instalar agentes.");
        return new AgentInstallation(
            "powershell -NoProfile -ExecutionPolicy Bypass -File .\\instalar.ps1 -Server '"+host+"' -CaPath '.\\ca.crt'",
            "sudo bash ./instalar.sh '"+host+"' './ca.crt'","");
    }
}
