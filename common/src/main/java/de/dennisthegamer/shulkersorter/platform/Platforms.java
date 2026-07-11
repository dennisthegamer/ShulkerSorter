package de.dennisthegamer.shulkersorter.platform;

import java.util.ServiceLoader;

public final class Platforms {
    private static final Platform INSTANCE = ServiceLoader.load(Platform.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No ShulkerSorter Platform implementation on classpath"));

    private Platforms() {
    }

    public static Platform get() {
        return INSTANCE;
    }
}
