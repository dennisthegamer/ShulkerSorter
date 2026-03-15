package com.shulkersort;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShulkerSort implements ModInitializer {
    public static final String MOD_ID = "shulkersort";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("ShulkerSort initialized!");
    }
}
