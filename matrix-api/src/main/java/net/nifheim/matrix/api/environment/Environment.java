package net.nifheim.matrix.api.environment;

/**
 * The environment where the application is running.
 *
 * @author Jaime Suárez
 */
public interface Environment {

    /**
     * Get the environment type.
     *
     * @return environment type.
     */
    EnvironmentType environmentType();

    /**
     * Get if the environment is a proxy.
     *
     * @return true if the environment is a proxy, false otherwise.
     */
    boolean isProxy();

    /**
     * Create a new environment instance.
     *
     * @param environmentType the environment type.
     * @param isProxy         if the environment is a proxy.
     */
    record EnvironmentImpl(EnvironmentType environmentType, boolean isProxy) implements Environment {

        public static Environment PAPER = create(EnvironmentType.MINECRAFT_SERVER, false);
        public static Environment PROXY = create(EnvironmentType.MINECRAFT_SERVER, true);
        public static Environment STANDALONE = create(EnvironmentType.STANDALONE, false);

        public static Environment create(EnvironmentType environmentType, boolean isProxy) {
            return new EnvironmentImpl(environmentType, isProxy);
        }
    }
}
