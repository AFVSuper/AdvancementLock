package org.afv.advancementlock.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import org.afv.advancementlock.AdvancementLock;
import org.afv.advancementlock.client.utils.SlotClientUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow
    @Nullable
    protected abstract PlayerEntity getCameraPlayer();

    @Shadow
    protected abstract void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter,
                                             PlayerEntity player, ItemStack stack, int seed);

    @Final
    @Shadow
    private MinecraftClient client;

    @Shadow @Final private static Identifier HOTBAR_SELECTION_TEXTURE;
    @Shadow @Final private static Identifier HOTBAR_TEXTURE;
    @Shadow @Final private static Identifier HOTBAR_ATTACK_INDICATOR_BACKGROUND_TEXTURE;
    @Shadow @Final private static Identifier HOTBAR_ATTACK_INDICATOR_PROGRESS_TEXTURE;
    @Shadow @Final private static Identifier HOTBAR_OFFHAND_LEFT_TEXTURE;
    @Shadow @Final private static Identifier HOTBAR_OFFHAND_RIGHT_TEXTURE;

    @Inject(
        method = "renderHotbarItem",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRenderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        PlayerInventory inv = player.getInventory();

        // Find which hotbar slot this stack belongs to
        for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
            if (inv.getStack(i) == stack) {
                if (SlotClientUtils.isSlotLocked(i)) {
                    ci.cancel(); // cancel rendering locked slot
                }
                break;
            }
        }
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void onRenderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        PlayerEntity player = this.getCameraPlayer();
        if (player == null) return;

        ItemStack itemStack = player.getOffHandStack();
        Arm arm = player.getMainArm().getOpposite();

        PlayerInventory inv = player.getInventory();
        int centerX = context.getScaledWindowWidth() / 2;
        int y = context.getScaledWindowHeight() - 22;

        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED,
                HOTBAR_TEXTURE,
                centerX - 91, y,
                182, 22);

        // Draw selection highlight only for unlocked slot
        int selected = inv.getSelectedSlot();

        // Draw items for unlocked slots
        int l = 1;
        for (int slot = 0; slot < 9; slot++) {
            int slotX = centerX - 90 + slot * 20 + 2;
            int itemY = y + 3;
            if (SlotClientUtils.isSlotLocked(slot)) {
                context.fill(slotX - 2, y + 3 - 2, slotX + 18, y + 3 + 18, 0x50FF0000); // semi-transparent red overlay

                context.drawItem(new ItemStack(net.minecraft.item.Items.BARRIER), slotX, itemY);
                continue;
            }

            int x = centerX - 90 + slot * 20 + 2; // vanilla item X position
            this.renderHotbarItem(context, x, itemY, tickCounter, player, inv.getStack(slot), l++);
        }
        if (!itemStack.isEmpty()) {
            if (arm == Arm.LEFT) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_TEXTURE, centerX - 91 - 29, context.getScaledWindowHeight() - 23, 29, 24);
            } else {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_RIGHT_TEXTURE, centerX + 91, context.getScaledWindowHeight() - 23, 29, 24);
            }
        }

        if (!SlotClientUtils.isSlotLocked(selected)) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION_TEXTURE,
                    centerX - 91 - 1 + selected * 20, y - 1, 24, 23);
        }

        if (!itemStack.isEmpty()) {
            int m = context.getScaledWindowHeight() - 16 - 3;
            if (arm == Arm.LEFT) {
                this.renderHotbarItem(context, centerX - 91 - 26, m, tickCounter, player, itemStack, l++);
            } else {
                this.renderHotbarItem(context, centerX + 91 + 10, m, tickCounter, player, itemStack, l++);
            }
        }

        if (this.client.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR) {
            assert this.client.player != null;
            float f = this.client.player.getAttackCooldownProgress(0.0F);
            if (f < 1.0F) {
                int n = context.getScaledWindowHeight() - 20;
                int o = centerX + 91 + 6;
                if (arm == Arm.RIGHT) {
                    o = centerX - 91 - 22;
                }

                int p = (int)(f * 19.0F);
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_ATTACK_INDICATOR_BACKGROUND_TEXTURE, o, n, 18, 18);
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_ATTACK_INDICATOR_PROGRESS_TEXTURE, 18, 18, 0, 18 - p, o, n + 18 - p, 18, p);
            }
        }

        ci.cancel(); // cancel vanilla rendering
    }
}
