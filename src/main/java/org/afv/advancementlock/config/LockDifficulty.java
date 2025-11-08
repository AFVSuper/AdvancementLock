package org.afv.advancementlock.config;

import org.afv.advancementlock.util.TextUtils;

public enum LockDifficulty {
    EASY("Easy"),
    NORMAL("Normal"),
    HARD("Hard");

    private final String displayName;
    LockDifficulty(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
    public String getTranslationKey() { return "advancementlock.config.difficulty." + displayName.toLowerCase(); }
}
