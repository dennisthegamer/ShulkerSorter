package de.dennisthegamer.shulkersorter.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.dennisthegamer.shulkersorter.config.ConfigScreen;
import de.dennisthegamer.shulkersorter.platform.Platforms;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (Platforms.get().isModLoaded("yet_another_config_lib_v3")) {
            return ConfigScreen::create;
        }
        return parent -> null;
    }
}
