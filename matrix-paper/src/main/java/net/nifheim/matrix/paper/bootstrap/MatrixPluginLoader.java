package net.nifheim.matrix.paper.bootstrap;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

public class MatrixPluginLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addDependency(new Dependency(new DefaultArtifact("com.github.ben-manes.caffeine:caffeine:3.1.1"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.slf4j:slf4j-api:1.7.32"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.slf4j:slf4j-nop:1.7.32"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.zaxxer:HikariCP:5.0.1"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.mariadb.jdbc:mariadb-java-client:2.7.3"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.apache.commons:commons-pool2:2.11.1"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("redis.clients:jedis:3.9.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.mongodb:bson:4.7.1"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.mongodb:mongodb-driver-core:4.7.1"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.mongodb:mongodb-driver-sync:4.7.1"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("dev.morphia.morphia:morphia-core:2.4.12"), null));

        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build());

        classpathBuilder.addLibrary(resolver);
    }
}
