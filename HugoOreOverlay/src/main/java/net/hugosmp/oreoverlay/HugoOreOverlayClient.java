package net.hugosmp.oreoverlay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HugoOreOverlay 0.2.
 *
 * Scans only world/entity data currently available to the local client.
 * It does not reveal unloaded chunks, bypass anti-xray, or recover entities
 * that the server does not send to this client.
 */
public final class HugoOreOverlayClient implements ClientModInitializer {
    private static KeyBinding scanKey;
    private static final int HORIZONTAL_RADIUS = 16;
    private static final int VERTICAL_RADIUS = 12;
    private static final Map<Block, String> ORES = new LinkedHashMap<>();

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

        BlockPos center = client.player.getBlockPos();
        Map<String, BlockPos> nearest = new LinkedHashMap<>();
        Map<String, Double> distances = new LinkedHashMap<>();

        for (int dx = -HORIZONTAL_RADIUS; dx <= HORIZONTAL_RADIUS; dx++) {
            for (int dy = -VERTICAL_RADIUS; dy <= VERTICAL_RADIUS; dy++) {
                for (int dz = -HORIZONTAL_RADIUS; dz <= HORIZONTAL_RADIUS; dz++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    if (!client.world.isChunkLoaded(pos)) continue;
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

        client.player.sendMessage(Text.literal("§6HugoOreOverlay 0.2 §7| Erz-Scan (" +
                HORIZONTAL_RADIUS + " Blöcke):"), false);
        if (nearest.isEmpty()) {
            client.player.sendMessage(Text.literal("§7Keine passenden, clientseitig bekannten Erze im Scanbereich."), false);
        } else {
            nearest.forEach((name, pos) -> client.player.sendMessage(Text.literal(
                    "§e" + name + " §7bei §fX " + pos.getX() + " / Y " + pos.getY() + " / Z " + pos.getZ()
            ), false));
        }

        // This lists only other player entities currently tracked by the client.
        // No attempt is made to infer hidden, untracked, or server-suppressed players.
        int playerCount = 0;
        client.player.sendMessage(Text.literal("§bSpieler in den empfangenen Entity-Daten:"), false);
        for (PlayerEntity other : client.world.getPlayers()) {
            if (other == client.player || !other.isAlive()) continue;
            double distance = client.player.distanceTo(other);
            if (distance > 128.0) continue;
            playerCount++;
            BlockPos p = other.getBlockPos();
            client.player.sendMessage(Text.literal("§b" + other.getName().getString() +
                    " §7– " + Math.round(distance) + "m §8(X " + p.getX() +
                    ", Y " + p.getY() + ", Z " + p.getZ() + ")"), false);
        }
        if (playerCount == 0) {
            client.player.sendMessage(Text.literal("§7Keine anderen Spieler in den aktuell empfangenen Entity-Daten."), false);
        }
        client.player.sendMessage(Text.literal("§8Hinweis: Nur geladene Blockdaten und vom Server übermittelte Spieler. Kein Seed-Reverse-Engineering oder Anti-Xray-/Unsichtbarkeits-Bypass."), false);
    }
}
