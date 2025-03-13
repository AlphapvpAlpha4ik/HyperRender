package com.hyperrenderx;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HyperRenderX implements ModInitializer {
    public static final String MOD_ID = "hyperrenderx";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing HyperRenderX - FPS Optimization Mod");
    }
}
