package com.hyperrenderx;

import com.hyperrenderx.client.HudRenderer;
import com.hyperrenderx.client.ParticleOptimizer;
import com.hyperrenderx.config.HyperRenderXConfig;
import com.hyperrenderx.util.opti.async.AsyncChunkLoader;
import com.hyperrenderx.util.opti.BambooLODRenderer;
import com.hyperrenderx.util.DynamicRenderDistanceManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HyperRenderXClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("HyperRenderX");
    private static HyperRenderXConfig config;
    private static DynamicRenderDistanceManager renderDistanceManager;
    private static AsyncChunkLoader chunkLoader;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r);
        t.setDaemon(true); // Потоки завершатся при закрытии приложения
        return t;
    });

    // Общий кэш чанков
    private static final ConcurrentHashMap<ChunkPos, Boolean> cachedChunks = new ConcurrentHashMap<>();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing HyperRenderX Client...");
        config = new HyperRenderXConfig();
        config.load();

        // Устанавливаем профиль оптимизации освещения
        try {
            Class<?> lightingOptimizationMixin = Class.forName("com.hyperrenderx.mixin.client.world.LightingOptimizationMixin");
            java.lang.reflect.Method setOptimizationProfileMethod = lightingOptimizationMixin.getDeclaredMethod("setOptimizationProfile", String.class);
            setOptimizationProfileMethod.setAccessible(true);
            setOptimizationProfileMethod.invoke(null, config.getOptimizationProfile());
        } catch (Exception e) {
            LOGGER.error("Failed to set optimization profile: " + e.getMessage());
        }

        // Инициализируем менеджер дальности прорисовки
        renderDistanceManager = new DynamicRenderDistanceManager();
        LOGGER.info("DynamicRenderDistanceManager initialized.");

        // Регистрируем HUD рендерер
        HudRenderCallback.EVENT.register(new HudRenderer());

        // Инициализируем оптимизатор партиклов
        ParticleOptimizer.init();

        // Инициализируем рендерер бамбука с LOD
        LOGGER.info("Initializing Bamboo LOD Renderer...");
        BambooLODRenderer.init();

        // Инициализируем AsyncChunkLoader с ExecutorService
        chunkLoader = new AsyncChunkLoader(executorService);
        LOGGER.info("AsyncChunkLoader initialized with ExecutorService.");

        // Регистрация события рендеринга мира
        WorldRenderEvents.END.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.world != null && client.player != null) {
                Vec3d playerPos = client.player.getPos();
                World world = client.world;
                int renderDistance = client.options.getViewDistance().getValue();

                int playerX = (int) playerPos.x;
                int playerY = (int) playerPos.y;
                int playerZ = (int) playerPos.z;

                // Проверяем блоки в радиусе вокруг игрока
                for (int dx = -renderDistance; dx <= renderDistance; dx++) {
                    for (int dz = -renderDistance; dz <= renderDistance; dz++) {
                        for (int dy = -4; dy <= 4; dy++) { // Ограничиваем высоту поиска
                            BlockPos pos = new BlockPos(playerX + dx, playerY + dy, playerZ + dz);
                            if (world.getBlockState(pos).getBlock() == Blocks.BAMBOO) {
                                BambooLODRenderer.renderBambooLOD(context.matrixStack(), context.consumers(), pos);
                            }
                        }
                    }
                }
            }
        });

        // Регистрация события тиков для обработки горячей клавиши
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_F6)) {
                getRenderDistanceManager();
                LOGGER.info("Forced render distance update triggered by F6.");
            }
        });

        // Регистрация события завершения игры
        ClientLifecycleEvents.CLIENT_STOPPING.register(minecraftClient -> {
            if (chunkLoader != null) {
                chunkLoader.shutdown();
                LOGGER.info("AsyncChunkLoader shut down.");
            }
            if (renderDistanceManager != null) {
                LOGGER.info("DynamicRenderDistanceManager resources released.");
            }
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    LOGGER.warn("ExecutorService принудительно завершён при закрытии клиента.");
                } else {
                    LOGGER.info("ExecutorService корректно завершён при закрытии клиента.");
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
                LOGGER.error("Ошибка при завершении ExecutorService: {}", e.getMessage());
            }
            cachedChunks.clear(); // Очищаем кэш при закрытии
            LOGGER.info("HyperRenderXClient cache cleared.");
        });

        LOGGER.info("HyperRenderX Client initialized with async chunk loading and dynamic render distance.");
    }

    public static HyperRenderXConfig getConfig() {
        return config;
    }

    public static void getRenderDistanceManager() {
        if (renderDistanceManager == null) {
            LOGGER.warn("renderDistanceManager is null, initializing now...");
            renderDistanceManager = new DynamicRenderDistanceManager();
        }
    }

    public static void setDisableClouds(boolean disable) {
        HyperRenderXConfig config = HyperRenderXClient.getConfig();
        CloudRenderMode newMode = disable ? CloudRenderMode.OFF : config.getPreferredCloudMode();
        if (newMode != config.getPreferredCloudMode()) {
            config.setPreferredCloudMode(newMode);
            config.save();
            LOGGER.info("Clouds {}abled (new mode: {})", disable ? "dis" : "en", newMode);
        }
    }

    // Методы для работы с общим кэшем
    public static void setCachedChunk(ChunkPos chunkPos, boolean value) {
        cachedChunks.put(chunkPos, value);
    }

    public static boolean isChunkCached(ChunkPos chunkPos) {
        return cachedChunks.getOrDefault(chunkPos, false);
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }
}