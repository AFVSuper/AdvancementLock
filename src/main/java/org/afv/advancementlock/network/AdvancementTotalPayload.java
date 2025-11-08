package org.afv.advancementlock.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.afv.advancementlock.AdvancementLock;

public record AdvancementTotalPayload(int total) implements CustomPayload {
    public static final Id<AdvancementTotalPayload> ID =
            new Id<>(Identifier.of(AdvancementLock.ModID, "advancement_total"));

    public static final PacketCodec<PacketByteBuf, AdvancementTotalPayload> CODEC =
            PacketCodec.of(
                    (payload, buf) -> buf.writeInt(payload.total()),
                    buf -> new AdvancementTotalPayload(buf.readInt())
            );

    public AdvancementTotalPayload(PacketByteBuf buf) {
        this(buf.readInt());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}