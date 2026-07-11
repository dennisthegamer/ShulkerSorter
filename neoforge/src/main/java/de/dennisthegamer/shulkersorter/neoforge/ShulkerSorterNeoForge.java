package de.dennisthegamer.shulkersorter.neoforge;

import de.dennisthegamer.shulkersorter.ShulkerSorter;
import de.dennisthegamer.shulkersorter.ShulkerSorterClient;
import de.dennisthegamer.shulkersorter.config.ConfigScreen;
import de.dennisthegamer.shulkersorter.platform.Platforms;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(ShulkerSorter.MOD_ID)
public final class ShulkerSorterNeoForge {

    public ShulkerSorterNeoForge(ModContainer container) {
        ShulkerSorter.init();

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            ShulkerSorterClient.init();

            if (Platforms.get().isModLoaded("yet_another_config_lib_v3")) {
                container.registerExtensionPoint(IConfigScreenFactory.class,
                        (mc, parent) -> ConfigScreen.create(parent));
            }
        }
    }
}
