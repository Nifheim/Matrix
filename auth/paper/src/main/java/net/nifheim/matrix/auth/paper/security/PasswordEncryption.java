package net.nifheim.matrix.auth.paper.security;

import de.rtner.misc.BinTools;
import de.rtner.security.auth.spi.PBKDF2Engine;
import de.rtner.security.auth.spi.PBKDF2Parameters;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * The PasswordEncryption class provides methods for password encryption,
 * salt generation, and password verification.
 */
public class PasswordEncryption {

    public static final int ROUNDS = 25_000;

    /**
     * Computes the hash of a password using the PBKDF2 algorithm.
     *
     * @param password The password to be hashed
     * @param salt     The salt value used in the hashing process
     * @return The computed password hash
     * @throws NullPointerException if password or salt is null
     */
    public static @NotNull HashedPassword computeHash(@NotNull String password, @NotNull String salt) {
        Objects.requireNonNull(password, "password");
        Objects.requireNonNull(salt, "salt");
        PBKDF2Parameters params = new PBKDF2Parameters("HmacSHA256", "UTF-8", salt.getBytes(), ROUNDS);
        PBKDF2Engine engine = new PBKDF2Engine(params);

        return new HashedPassword(ROUNDS, BinTools.bin2hex(engine.deriveKey(password, 64)), salt);
    }

    /**
     * Computes the hash of a password using the PBKDF2 algorithm.
     *
     * @param password The password to be hashed
     * @return The computed PasswordHash object containing the password hash
     * @throws NullPointerException if password is null
     */
    public static HashedPassword computeHash(String password) {
        String salt = generateSalt();
        return computeHash(password, salt);
    }

    /**
     * Compares a given password with a password hash and returns whether they match.
     *
     * @param password     The password to compare
     * @param hashedPassword The password hash to compare against
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean comparePassword(String password, @NotNull HashedPassword hashedPassword) {
        int iterations = hashedPassword.iterations();
        byte[] derivedKey = BinTools.hex2bin(hashedPassword.hash());
        String salt = hashedPassword.salt();
        PBKDF2Parameters params = new PBKDF2Parameters("HmacSHA256", "UTF-8", salt.getBytes(), iterations, derivedKey);
        PBKDF2Engine engine = new PBKDF2Engine(params);
        return engine.verifyKey(password);
    }

    /**
     * Returns the length of the salt value used in the hashing process.
     *
     * @return The length of the salt value
     */
    public static int getSaltLength() {
        return 24;
    }

    /**
     * Generates a random salt value for password hashing.
     *
     * @return The generated salt value as a hexadecimal string
     */
    public static String generateSalt() {
        return RandomStringUtils.generateHex(getSaltLength());
    }
}
