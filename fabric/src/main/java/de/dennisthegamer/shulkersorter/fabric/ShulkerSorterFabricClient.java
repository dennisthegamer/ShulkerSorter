package de.dennisthegamer.shulkersorter.fabric;

import de.dennisthegamer.shulkersorter.ShulkerSorterClient;
import net.fabricmc.api.ClientModInitializer;

public final class ShulkerSorterFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ShulkerSorterClient.init();
    }
}
