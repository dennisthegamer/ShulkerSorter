package de.dennisthegamer.shulkersorter.platform;

import java.nio.file.Path;

/**
 * Loader abstraction for the few loader API calls the shared code needs.
 * Implementations are provided per loader and discovered via {@link java.util.ServiceLoader}.
 */
public interface Platform {

    Path getConfigDir();

    boolean isModLoaded(String modId);
}
