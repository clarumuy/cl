package uy.edu.isbo.control.service;

import java.sql.SQLException;
import uy.edu.isbo.control.repository.ComputerRepository;
import uy.edu.isbo.control.repository.agent.AgentRepository;

public final class ComputerService {
    private final ComputerRepository computers;
    private final AgentRepository agents;
    public ComputerService(ComputerRepository computers,AgentRepository agents) {this.computers=computers;this.agents=agents;}

    public void save(long id,String name,String ip,long laboratoryId) throws SQLException {
        if (id>0 && agents.linked(id)) {computers.updateLinked(id,name,laboratoryId);return;}
        if (!privateIp(ip)) throw new IllegalArgumentException("Usá una dirección IPv4 privada válida");
        computers.save(id,name,ip.strip(),laboratoryId);
    }
    public void delete(long id) throws SQLException {computers.delete(id);}

    /** Las computadoras sin agente solo se pueden registrar en una IP de la LAN. */
    public static boolean privateIp(String ip) {
        if(ip==null || ip.isBlank() || ip.length()>45) return false;
        String[] parts=ip.split("\\.",-1);
        if(parts.length!=4)return false;
        int[] octets=new int[4];
        for(int i=0;i<4;i++) {
            if(!parts[i].matches("[0-9]{1,3}"))return false;
            octets[i]=Integer.parseInt(parts[i]);
            if(octets[i]>255)return false;
        }
        return octets[0]==10 || (octets[0]==172 && octets[1]>=16 && octets[1]<=31)
                || (octets[0]==192 && octets[1]==168);
    }
}
