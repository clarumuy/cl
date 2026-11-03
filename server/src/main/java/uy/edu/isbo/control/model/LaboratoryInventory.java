package uy.edu.isbo.control.model;

import java.util.List;

public record LaboratoryInventory(Laboratory laboratory, List<Computer> computers) {
    public LaboratoryInventory {
        computers = List.copyOf(computers);
    }
}
