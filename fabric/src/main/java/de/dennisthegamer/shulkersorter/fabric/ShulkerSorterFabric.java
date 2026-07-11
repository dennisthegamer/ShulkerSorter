package de.dennisthegamer.shulkersorter.fabric;

import de.dennisthegamer.shulkersorter.ShulkerSorter;
import net.fabricmc.api.ModInitializer;

public final class ShulkerSorterFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ShulkerSorter.init();
    }
}
