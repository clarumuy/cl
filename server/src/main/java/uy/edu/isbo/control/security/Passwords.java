package uy.edu.isbo.control.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class Passwords {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(12);

    private Passwords() {}

    public static String hash(String password) {
        return ENCODER.encode(password);
    }

    public static boolean matches(String password, String stored) {
        return ENCODER.matches(password, stored);
    }
}
