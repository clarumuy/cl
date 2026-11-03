package uy.edu.isbo.control.service;

import java.sql.SQLException;
import uy.edu.isbo.control.repository.UserRepository;
import uy.edu.isbo.control.security.Passwords;

public final class UserService {
    private final UserRepository users;
    public UserService(UserRepository users) {this.users=users;}
    public void save(long id,String username,String password) throws SQLException {
        boolean supplied=password!=null && !password.isBlank();
        if(id==0 && !supplied)throw new IllegalArgumentException("La contraseña es obligatoria");
        if(supplied && password.length()<12) throw new IllegalArgumentException("La contraseña debe tener al menos 12 caracteres");
        if(supplied && password.length()>1024)throw new IllegalArgumentException("Contraseña demasiado larga");
        users.save(id,username,supplied?Passwords.hash(password):null);
    }
    public void delete(long id,long currentUserId) throws SQLException {users.delete(id,currentUserId);}
}
