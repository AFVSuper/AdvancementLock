package org.afv.advancementlock.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.afv.advancementlock.AdvancementLock;

public record AdvancementCountResponsePayload(int count) implements CustomPayload {
    public static final Id<AdvancementCountResponsePayload> ID = 
        new Id<>(Identifier.of(AdvancementLock.ModID, "advancement_count_response"));

    public static final PacketCodec<PacketByteBuf, AdvancementCountResponsePayload> CODEC =
            PacketCodec.of(
                    (payload, buf) -> buf.writeInt(payload.count()),
                    buf -> new AdvancementCountResponsePayload(buf.readInt())
            );
    
    public AdvancementCountResponsePayload(PacketByteBuf buf) {
        this(buf.readInt());
    }
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}