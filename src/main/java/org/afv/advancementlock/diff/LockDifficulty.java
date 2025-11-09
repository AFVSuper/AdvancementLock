package org.afv.advancementlock.diff;

import com.mojang.serialization.Codec;

public enum LockDifficulty {
    EASY("Easy"),
    NORMAL("Normal"),
    HARD("Hard");

    private final String displayName;
    LockDifficulty(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
    public String getTranslationKey() { return "advancementlock.config.difficulty." + displayName.toLowerCase(); }

    @Override
    public String toString() {
        return getDisplayName();
    }

    public LockDifficulty getFromName(String name) {
        switch (name) {
            case "Easy" -> {
                return EASY;
            }
            case "Normal" -> {
                return NORMAL;
            }
            case "Hard" -> {
                return HARD;
            }
            default -> { return null; }
        }
    }

    public static final Codec<LockDifficulty> CODEC =
            Codec.STRING.xmap(LockDifficulty::valueOf, Enum::name);
}
