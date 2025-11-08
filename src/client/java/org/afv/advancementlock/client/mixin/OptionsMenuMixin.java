package org.afv.advancementlock.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.afv.advancementlock.AdvancementLock;
import org.afv.advancementlock.client.config.IconButton;
import org.afv.advancementlock.client.config.ModMenuIntegration;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Supplier;

@Mixin(OptionsScreen.class)
public abstract class OptionsMenuMixin {

    @Shadow
    @Final
    private ThreePartsLayoutWidget layout;

    @Shadow
    protected abstract void refreshWidgetPositions();

    @Unique
    private IconButton configButton;

//    @Inject(method = "init", at = @At("TAIL"))
//    private void onInit(CallbackInfo ci) {
//        if (configButton != null) return; // already added
//
//        configButton = new IconButton(...);
//        layout.addHeader(...).add(configButton, Positioner::alignRight);
//        refreshWidgetPositions();
//    }
}
