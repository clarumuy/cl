package uy.edu.isbo.control.service;
import java.sql.SQLException;
import uy.edu.isbo.control.repository.LaboratoryRepository;

public final class LaboratoryService {
    private final LaboratoryRepository laboratories;
    public LaboratoryService(LaboratoryRepository laboratories) {this.laboratories=laboratories;}
    public void save(long id,String name,String room) throws SQLException {laboratories.save(id,name,room);}
    public void delete(long id) throws SQLException {laboratories.delete(id);}
}
