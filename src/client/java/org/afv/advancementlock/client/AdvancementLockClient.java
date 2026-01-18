package org.afv.advancementlock.client;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.impl.client.screen.ScreenExtensions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.afv.advancementlock.AdvancementLock;
import org.afv.advancementlock.client.config.ModMenuIntegration;
import org.afv.advancementlock.config.ModConfig;
import org.afv.advancementlock.network.AdvancementCountResponsePayload;
import org.afv.advancementlock.network.AdvancementTotalPayload;

public class AdvancementLockClient implements ClientModInitializer {

    private static int advancementCount = 0;
    private static final Identifier CONFIG_ICON = Identifier.of("advancementlock", "textures/gui/lock_icon.png");
    private static final String MOD_CONFIG_BUTTON_ID = "mod_config_button";

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(AdvancementCountResponsePayload.ID,
                (payload, context) -> {
                    int count = payload.count();
                    // Store or use the count
                    MinecraftClient.getInstance().execute(() -> {
                        AdvancementLockClient.advancementCount = count;
                        AdvancementLock.LOGGER.debug("Packet received: {}", count);
                    });
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                AdvancementTotalPayload.ID,
                (payload, context) -> {
                    int total = payload.total();
                    // AdvancementLock.LOGGER.info("Received AdvancementTotalPayload: {}", total);

                    // Store it somewhere accessible
                    AdvancementLock.setServerAdvancementTotal(total);
                }
        );
    }

    public AdvancementLockClient getInstance() { return this; }
    public static int getAdvancementCount() { return advancementCount; }

    public static Screen getConfigScreen(net.minecraft.client.gui.screen.Screen parent) {
        return AutoConfig.getConfigScreen(ModConfig.class, parent).get();
    }
}
