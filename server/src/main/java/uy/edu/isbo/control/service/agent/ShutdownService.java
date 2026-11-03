package uy.edu.isbo.control.service.agent;

import java.sql.SQLException;
import uy.edu.isbo.control.model.LaboratoryInventory;
import uy.edu.isbo.control.repository.ComputerRepository;
import uy.edu.isbo.control.repository.LaboratoryRepository;
import uy.edu.isbo.control.repository.agent.CommandRepository;

public final class ShutdownService {
    private final ComputerRepository computers;
    private final LaboratoryRepository laboratories;
    private final CommandRepository commands;
    public ShutdownService(ComputerRepository computers,LaboratoryRepository laboratories,CommandRepository commands) {
        this.computers=computers;this.laboratories=laboratories;this.commands=commands;
    }
    public String computer(long id) throws SQLException {
        var computer=computers.findById(id).orElseThrow(()->new IllegalArgumentException("Computadora inexistente"));
        if(!commands.queueComputer(id))throw new IllegalStateException("Sin agente disponible o ya tiene una orden pendiente");
        return "Orden programada para " + computer.name();
    }
    public String laboratory(long id) throws SQLException {
        var laboratory=laboratories.findById(id).orElseThrow(()->new IllegalArgumentException("Laboratorio inexistente"));
        // Agregación de POO: el inventario no es dueño de los equipos que reúne.
        var inventory=new LaboratoryInventory(laboratory,computers.findByLaboratory(id));
        int sent=commands.queueLaboratory(id);
        return "Órdenes programadas: "+sent+" de "+inventory.computers().size();
    }
}
