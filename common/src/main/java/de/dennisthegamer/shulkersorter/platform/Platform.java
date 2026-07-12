package de.dennisthegamer.shulkersorter.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.nio.file.Path;

/**
 * Loader abstraction for the few loader API calls the shared code needs.
 * Implementations are provided per loader and discovered via {@link java.util.ServiceLoader}.
 */
public interface Platform {

    Path getConfigDir();

    boolean isModLoaded(String modId);

    /**
     * Creates a namespaced payload type without the shared code referencing the
     * id class directly: its mojmap name changed between 1.21.10 (ResourceLocation)
     * and 1.21.11 (Identifier), and NeoForge jars run on the runtime version's
     * mojmap names — a direct reference crashes on 1.21.9/1.21.10.
     */
    <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createPayloadType(String namespace, String path);
}
