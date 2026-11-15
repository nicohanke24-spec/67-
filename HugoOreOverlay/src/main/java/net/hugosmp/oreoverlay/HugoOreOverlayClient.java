package net.hugosmp.oreoverlay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HugoOreOverlay 0.3.
 *
 * Scans block states currently loaded by the client. In singleplayer the
 * configured seed is verified against the integrated server. In multiplayer
 * the client cannot verify the remote server seed, so scanning is allowed
 * without that check. No unloaded chunks are predicted.
 */
public final class HugoOreOverlayClient implements ClientModInitializer {
    private static final long REQUIRED_SEED = -2394875239847523984L;
    private static final int HORIZONTAL_RADIUS = 256;
    private static final int VERTICAL_RADIUS = 16;
    private static final Map<Block, String> ORES = new LinkedHashMap<>();
    private static KeyBinding scanKey;

    static {
        ORES.put(Blocks.DIAMOND_ORE, "Diamant");
        ORES.put(Blocks.DEEPSLATE_DIAMOND_ORE, "Tiefenschiefer-Diamant");
        ORES.put(Blocks.GOLD_ORE, "Gold");
        ORES.put(Blocks.DEEPSLATE_GOLD_ORE, "Tiefenschiefer-Gold");
        ORES.put(Blocks.IRON_ORE, "Eisen");
        ORES.put(Blocks.DEEPSLATE_IRON_ORE, "Tiefenschiefer-Eisen");
        ORES.put(Blocks.REDSTONE_ORE, "Redstone");
        ORES.put(Blocks.DEEPSLATE_REDSTONE_ORE, "Tiefenschiefer-Redstone");
        ORES.put(Blocks.LAPIS_ORE, "Lapislazuli");
        ORES.put(Blocks.DEEPSLATE_LAPIS_ORE, "Tiefenschiefer-Lapis");
        ORES.put(Blocks.EMERALD_ORE, "Smaragd");
        ORES.put(Blocks.DEEPSLATE_EMERALD_ORE, "Tiefenschiefer-Smaragd");
        ORES.put(Blocks.COPPER_ORE, "Kupfer");
        ORES.put(Blocks.DEEPSLATE_COPPER_ORE, "Tiefenschiefer-Kupfer");
        ORES.put(Blocks.COAL_ORE, "Kohle");
        ORES.put(Blocks.DEEPSLATE_COAL_ORE, "Tiefenschiefer-Kohle");
        ORES.put(Blocks.NETHER_GOLD_ORE, "Nether-Gold");
        ORES.put(Blocks.NETHER_QUARTZ_ORE, "Netherquarz");
        ORES.put(Blocks.ANCIENT_DEBRIS, "Ancient Debris");
    }

    @Override
    public void onInitializeClient() {
        scanKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hugooreoverlay.scan",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                KeyBinding.Category.create(Identifier.of("hugooreoverlay", "controls"))
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (scanKey.wasPressed()) scanNearby(client);
        });
    }

    private static void scanNearby(MinecraftClient client) {
        if (client.world == null || client.player == null) return;

        boolean singleplayer = client.isInSingleplayer() && client.getServer() != null;
        if (singleplayer) {
            long actualSeed = client.getServer().getOverworld().getSeed();
            if (actualSeed != REQUIRED_SEED) {
                client.player.sendMessage(Text.literal(
                        "§cFalscher Welt-Seed. Erwartet: §f" + REQUIRED_SEED +
                        " §c| Aktuell: §f" + actualSeed
                ), false);
                return;
            }
        }

        BlockPos center = client.player.getBlockPos();
        Map<String, BlockPos> nearest = new LinkedHashMap<>();
        Map<String, Double> distances = new LinkedHashMap<>();
        int checkedChunks = 0;

        int minChunkX = Math.floorDiv(center.getX() - HORIZONTAL_RADIUS, 16);
        int maxChunkX = Math.floorDiv(center.getX() + HORIZONTAL_RADIUS, 16);
        int minChunkZ = Math.floorDiv(center.getZ() - HORIZONTAL_RADIUS, 16);
        int maxChunkZ = Math.floorDiv(center.getZ() + HORIZONTAL_RADIUS, 16);
        int minY = Math.max(client.world.getBottomY(), center.getY() - VERTICAL_RADIUS);
        int maxY = Math.min(client.world.getTopYInclusive(), center.getY() + VERTICAL_RADIUS);

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                int chunkMinX = chunkX * 16;
                int chunkMinZ = chunkZ * 16;
                int fromX = Math.max(center.getX() - HORIZONTAL_RADIUS, chunkMinX);
                int toX = Math.min(center.getX() + HORIZONTAL_RADIUS, chunkMinX + 15);
                int fromZ = Math.max(center.getZ() - HORIZONTAL_RADIUS, chunkMinZ);
                int toZ = Math.min(center.getZ() + HORIZONTAL_RADIUS, chunkMinZ + 15);

                BlockPos chunkProbe = new BlockPos(chunkMinX, center.getY(), chunkMinZ);
                if (!client.world.isChunkLoaded(chunkProbe)) continue;
                checkedChunks++;

                for (int x = fromX; x <= toX; x++) {
                    for (int z = fromZ; z <= toZ; z++) {
                        for (int y = minY; y <= maxY; y++) {
                            BlockPos pos = new BlockPos(x, y, z);
                            String name = ORES.get(client.world.getBlockState(pos).getBlock());
                            if (name == null) continue;
                            double distance = center.getSquaredDistance(pos);
                            if (distance < distances.getOrDefault(name, Double.MAX_VALUE)) {
                                distances.put(name, distance);
                                nearest.put(name, pos.toImmutable());
                            }
                        }
                    }
                }
            }
        }

        String seedStatus = singleplayer
                ? "Seed bestätigt"
                : "Multiplayer: Seed serverseitig nicht prüfbar";
        client.player.sendMessage(Text.literal(
                "§6HugoOreOverlay 0.3 §7| " + seedStatus + " | Radius " +
                HORIZONTAL_RADIUS + " | geladene Chunks: " + checkedChunks
        ), false);
        if (nearest.isEmpty()) {
            client.player.sendMessage(Text.literal(
                    "§7Keine passenden Erze in den aktuell geladenen Blöcken gefunden."
            ), false);
        } else {
            nearest.forEach((name, pos) -> client.player.sendMessage(Text.literal(
                    "§e" + name + " §7bei §fX " + pos.getX() + " / Y " +
                    pos.getY() + " / Z " + pos.getZ()
            ), false));
        }
        client.player.sendMessage(Text.literal(
                "§8Hinweis: Es werden nur vom Client geladene Blockdaten gescannt. " +
                "Nicht geladene oder vom Server maskierte Erze werden nicht vorausgesagt."
        ), false);
    }
}
