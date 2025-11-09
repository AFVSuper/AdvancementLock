package org.afv.advancementlock.diff;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.*;
import org.afv.advancementlock.AdvancementLock;

public class LockDifficultyState extends PersistentState {

    private LockDifficulty difficulty;

    public static final Codec<LockDifficultyState> CODEC = LockDifficulty.CODEC
            .xmap(LockDifficultyState::new, LockDifficultyState::getDifficulty);

    public static final PersistentStateType<LockDifficultyState> TYPE = new PersistentStateType<>(
            AdvancementLock.ModID + "_difficulty",
            LockDifficultyState::new,
            CODEC,
            null
    );

    public LockDifficultyState() {
        this(LockDifficulty.NORMAL);
    }

    public LockDifficultyState(LockDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public LockDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(LockDifficulty diff) {
        this.difficulty = diff;
        this.markDirty();
    }

    public static LockDifficultyState getServerState(MinecraftServer server) {
        ServerWorld world = server.getWorld(World.OVERWORLD);
        assert world != null;
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }
}