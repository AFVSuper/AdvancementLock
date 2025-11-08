package org.afv.advancementlock.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.afv.advancementlock.AdvancementLock;

public record RequestAdvancementCountPayload() implements CustomPayload {
    public static final Id<RequestAdvancementCountPayload> ID = 
        new Id<>(Identifier.of(AdvancementLock.ModID, "request_advancement_count"));

    public static final PacketCodec<PacketByteBuf, RequestAdvancementCountPayload> CODEC =
            PacketCodec.of((buf, payload) -> {}, buf -> new RequestAdvancementCountPayload());
    
    public RequestAdvancementCountPayload(PacketByteBuf buf) {
        this();
    }
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}