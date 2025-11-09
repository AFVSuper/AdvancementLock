package org.afv.advancementlock.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelStorage;
import org.afv.advancementlock.AdvancementLock;
import org.afv.advancementlock.diff.LockDifficulty;
import org.afv.advancementlock.diff.LockDifficultyState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {
    @Unique
    private LockDifficulty selectedDifficulty = LockDifficulty.NORMAL;

    @Shadow
    @Final
    private TabManager tabManager;

    @Unique
    private CyclingButtonWidget<LockDifficulty> lockDiffButton;

    @Inject(method = "init", at = @At("TAIL"))
    private void addLockDifficultyButton(CallbackInfo ci) {
        CreateWorldScreen screen = (CreateWorldScreen) (Object) this;

        int width = 210;
        int height = 20;
        int x = screen.width / 2 - width / 2;
        int y = screen.height / 6 + 120; // adjust vertical position

        lockDiffButton = CyclingButtonWidget.<LockDifficulty>builder(
                        value -> Text.translatable(value.getTranslationKey()))
                .values(LockDifficulty.values())
                .initially(selectedDifficulty)
                .build(
                        x, y, width, height,
                        Text.translatable("advancementlock.world_gen.difficulty"),
                        (button, value) -> selectedDifficulty = value
                );

        // Add it to the screen initially, we'll control visibility in render
        ((ScreenAccessor) screen).callAddDrawableChild(lockDiffButton);
        lockDiffButton.visible = false; // hidden by default
    }

    @Inject(method = "createLevel", at = @At("TAIL"))
    private void afterCreateWorld(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance(); // safe here, running on client
        IntegratedServer server = client.getServer(); // now we have the server instance
        if (server != null) {
            LockDifficultyState state = LockDifficultyState.getServerState(server);
            state.setDifficulty(selectedDifficulty);
        }
        AdvancementLock.applyDifficulty(server);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Use title comparison since GameTab class is private
        lockDiffButton.visible = tabManager.getCurrentTab() != null &&
                tabManager.getCurrentTab().getTitle().equals(Text.translatable("createWorld.tab.game.title"));
    }

    @Inject(method = "refreshWidgetPositions", at = @At("TAIL"))
    private void onRefreshPositions(CallbackInfo ci) {
        CreateWorldScreen screen = (CreateWorldScreen) (Object) this;
        if (lockDiffButton != null) {
            lockDiffButton.setX(screen.width / 2 - 105);
            lockDiffButton.setY(screen.height / 6 + 120);
        }
    }

    @Unique
    public LockDifficulty getSelectedDifficulty() {
        return selectedDifficulty;
    }

    @Unique
    public void setSelectedDifficulty(LockDifficulty difficulty) {
        selectedDifficulty = difficulty;
    }
}