package org.afv.advancementlock.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import org.afv.advancementlock.config.ModConfig;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ModConfig cfg = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
            return cfg.buildScreen().build(); // pass parent screen
        };
    }
}