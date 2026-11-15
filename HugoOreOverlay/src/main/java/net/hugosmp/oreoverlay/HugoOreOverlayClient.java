package net.hugosmp.oreoverlay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HugoOreOverlay 0.1: scans block states already sent to the client in nearby,
 * loaded chunks. It does not bypass server-side anti-xray or reveal unloaded chunks.
 */
public final class HugoOreOverlayClient implements ClientModInitializer {
    private static KeyBinding scanKey;
    private static final int HORIZONTAL_RADIUS = 12;
    private static final int VERTICAL_RADIUS = 8;
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
    }

    @Override
    public void onInitializeClient() {
        scanKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hugooreoverlay.scan",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                KeyBinding.Category.create(net.minecraft.util.Identifier.of("hugooreoverlay", "controls"))
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
                    Block block = client.world.getBlockState(pos).getBlock();
                    String name = ORES.get(block);
                    if (name == null) continue;
                    double distance = center.getSquaredDistance(pos);
                    if (distance < distances.getOrDefault(name, Double.MAX_VALUE)) {
                        distances.put(name, distance);
                        nearest.put(name, pos.toImmutable());
                    }
                }
            }
        }

        if (nearest.isEmpty()) {
            client.player.sendMessage(Text.literal("§6HugoOreOverlay: §7Keine geladenen Erze im Scanbereich gefunden."), false);
            return;
        }
        client.player.sendMessage(Text.literal("§6HugoOreOverlay §7| Nächste geladene Erzblöcke:"), false);
        nearest.forEach((name, pos) -> client.player.sendMessage(Text.literal(
                "§e" + name + " §7bei §fX " + pos.getX() + " / Y " + pos.getY() + " / Z " + pos.getZ()
        ), false));
        client.player.sendMessage(Text.literal("§8Hinweis: Nur bereits vom Server übermittelte Blockdaten; Anti-Xray wird nicht umgangen."), false);
    }
}
