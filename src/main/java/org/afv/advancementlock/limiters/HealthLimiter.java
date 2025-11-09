package org.afv.advancementlock.limiters;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.afv.advancementlock.AdvancementLock;

import java.util.*;

public class HealthLimiter {
    private static final Identifier HEALTH_MODIFIER = Identifier.of(AdvancementLock.ModID,"fca47f5d-b030-474a-880f-af682755c7e3");

    public static void updatePlayerHealth(ServerPlayerEntity player, int unlocked) {
        // AdvancementLock.LOGGER.info("Health Update: {}", player.getName().getString());
        double baseHearts = 3; // starting
        int total = AdvancementLock.getAdvancementTotal();
        int gainedHearts = (int) ((AdvancementLock.getDiffModifier() * (12 * unlocked)) / total); // integer division
        double newMaxHearts = Math.min((baseHearts + gainedHearts) * 2, 20); // convert to HP

        var attr = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attr != null) {
            if (attr.getModifier(HEALTH_MODIFIER) != null) {
                attr.removeModifier(HEALTH_MODIFIER);
            }

            EntityAttributeModifier healthMod = new EntityAttributeModifier(
                    HEALTH_MODIFIER, newMaxHearts - 20, EntityAttributeModifier.Operation.ADD_VALUE
            );
            // AdvancementLock.LOGGER.info("Modif Value: {}", newMaxHearts - 20);
            attr.addPersistentModifier(healthMod);
            if (player.getHealth() > newMaxHearts) {
                player.setHealth((float)newMaxHearts);
            }
        }
    }

    private static void broadcastNewHeart(ServerPlayerEntity player, int newHearts) {
        Text msg = Text.literal("§aUnlocked a new heart! Now at " + newHearts + " hearts!");
        player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        player.sendMessage(msg);
    }

    public static void onAdvancementGranted(ServerPlayerEntity player) {
        int unlocked = AdvancementUtils.countUnlockedAdvancements(player);
        int total = AdvancementLock.getAdvancementTotal();
        int newHearts = 3 + (12 * unlocked) / total;
        int prev = 3 + (12 * (unlocked - 1)) / total;
        if (prev < 3) prev = 3;

        if (prev < newHearts) {
            broadcastNewHeart(player, newHearts);
        }

        updatePlayerHealth(player, unlocked);
    }
}
