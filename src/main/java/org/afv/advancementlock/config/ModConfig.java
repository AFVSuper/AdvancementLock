package org.afv.advancementlock.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.EnumListEntry;
import net.minecraft.text.Text;
import org.afv.advancementlock.AdvancementLock;

@Config(name = AdvancementLock.ModID)
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Excluded
    public LockDifficulty difficulty = LockDifficulty.NORMAL;

    public ConfigBuilder buildScreen() {
        ConfigBuilder builder = ConfigBuilder.create().setTitle(Text.translatable("advancementlock.config.title"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        EnumListEntry<LockDifficulty> difficultyEntry = entryBuilder.startEnumSelector(
                        Text.translatable("advancementlock.config.difficulty"),
                        LockDifficulty.class,
                        difficulty
                )
                .setDefaultValue(LockDifficulty.NORMAL)
                .setEnumNameProvider(ld -> Text.translatable(((LockDifficulty) ld).getTranslationKey())) // <--- this sets custom labels
                .setSaveConsumer(val -> { this.difficulty = val; AutoConfig.getConfigHolder(ModConfig.class).save(); })
                .build();

        builder.getOrCreateCategory(Text.of("General")).addEntry(difficultyEntry);
        return builder;
    }
}