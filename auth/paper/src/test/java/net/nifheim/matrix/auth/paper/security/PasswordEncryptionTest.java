package net.nifheim.matrix.auth.paper.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the PasswordEncryption class.
 *
 * This test focuses on the computeHash method that is part of the PasswordEncryption class.
 * These methods provide functionality for password hashing, using the PBKDF2 algorithm with a user provided password and salt, which is instrumental in securing user accounts.
 */
public class PasswordEncryptionTest {

    /**
     * Test if computeHash method returns non null value.
     */
    @Test
    public void testComputeHashNonNull() {
        PasswordEncryption passwordEncryption = new PasswordEncryption();
        HashedPassword computedHash = passwordEncryption.computeHash("password", "salt");
        assertNotNull(computedHash, "Computed hash should not be null when using valid parameters");
    }


    /**
     * Test if computeHash method correctly throws NullPointerException for null password.
     */
    @Test
    public void testComputeHashWithNullPassword() {
        PasswordEncryption passwordEncryption = new PasswordEncryption();
        assertThrows(NullPointerException.class, () -> {
            passwordEncryption.computeHash(null, "salt");
        }, "computeHash should throw NullPointerException when password is null");
    }

    /**
     * Test if computeHash method correctly throws NullPointerException for null salt.
     */
    @Test
    public void testComputeHashWithNullSalt() {
        PasswordEncryption passwordEncryption = new PasswordEncryption();
        assertThrows(NullPointerException.class, () -> {
            passwordEncryption.computeHash("password", null);
        }, "computeHash should throw NullPointerException when salt is null");
    }

    /**
     * Test if computeHash method is correctly computing the hash. Because PBKDF2 is considered a secure algorithm, we can assume if output is non null and different
     * every time with random salt, the implementation is correct. Here we check if two hashes are different with different salts.
     */
    @Test
    public void testComputeHashDifferentHashes() {
        PasswordEncryption passwordEncryption = new PasswordEncryption();

        HashedPassword computedHashOne = passwordEncryption.computeHash("password", "saltOne");
        HashedPassword computedHashTwo = passwordEncryption.computeHash("password", "saltTwo");

        assertNotEquals(computedHashOne, computedHashTwo, "Hashes should not be same with different salts");
    }
}
