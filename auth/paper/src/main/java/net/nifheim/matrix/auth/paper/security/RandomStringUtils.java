package net.nifheim.matrix.auth.paper.security;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Utility for generating random strings.
 */
public final class RandomStringUtils {

    private static final char[] CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final Random RANDOM = new SecureRandom();
    private static final int HEX_MAX_INDEX = 16;

    // Utility class
    private RandomStringUtils() {
    }

    /**
     * Generate a random hexadecimal string of the given length. In other words, the generated string
     * contains characters only within the range [0-9a-f].
     *
     * @param length The length of the random string to generate
     * @return The random hexadecimal string
     */
    public static String generateHex(int length) {
        return generateString(length, HEX_MAX_INDEX);
    }

    private static String generateString(int length, int maxIndex) {
        if (length < 0) {
            throw new IllegalArgumentException("Length must be positive but was " + length);
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; ++i) {
            sb.append(CHARS[RANDOM.nextInt(maxIndex)]);
        }
        return sb.toString();
    }
}
