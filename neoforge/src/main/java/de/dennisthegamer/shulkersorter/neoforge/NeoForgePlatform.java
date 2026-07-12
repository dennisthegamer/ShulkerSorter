package de.dennisthegamer.shulkersorter.neoforge;

import de.dennisthegamer.shulkersorter.platform.Platform;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public final class NeoForgePlatform implements Platform {

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createPayloadType(String namespace, String path) {
        // NeoForge runs on the runtime version's mojmap names and the id class was
        // renamed between 1.21.10 (ResourceLocation) and 1.21.11 (Identifier), so it
        // must be resolved reflectively; CustomPacketPayload.Type itself kept its name.
        try {
            Class<?> idClass;
            try {
                idClass = Class.forName("net.minecraft.resources.Identifier"); // 1.21.11+
            } catch (ClassNotFoundException e) {
                idClass = Class.forName("net.minecraft.resources.ResourceLocation"); // <=1.21.10
            }
            Object id = idClass.getMethod("fromNamespaceAndPath", String.class, String.class)
                .invoke(null, namespace, path);
            @SuppressWarnings("unchecked")
            CustomPacketPayload.Type<T> type = (CustomPacketPayload.Type<T>)
                CustomPacketPayload.Type.class.getConstructors()[0].newInstance(id);
            return type;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create payload type " + namespace + ":" + path, e);
        }
    }
}
