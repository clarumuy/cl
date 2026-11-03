package uy.edu.isbo.control.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uy.edu.isbo.control.repository.agent.AgentRepository;
import uy.edu.isbo.control.repository.ComputerRepository;
import uy.edu.isbo.control.repository.UserRepository;

class ServiceValidationTest {
    private final ComputerService computers = new ComputerService(new ComputerRepository(), new AgentRepository());
    private final UserService users = new UserService(new UserRepository());

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {
                "8.8.8.8",
                "127.0.0.1",
                "172.32.0.1",
                "192.168.0.256",
                "10.0.0",
                "10.0.0.a",
                "10.0.0.1:80"
            })
    void rejectsInvalidManualAddressesBeforeAccessingTheDatabase(String address) {
        assertThrows(
                IllegalArgumentException.class, () -> computers.save(0, "PC", address, 1));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "short"})
    void requiresAStrongPasswordForNewUsers(String password) {
        assertThrows(
                IllegalArgumentException.class, () -> users.save(0, "new-user", password));
    }
}
