package com.thizthizzydizzy.treefeller.platform.bukkit.connector;
import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
/** Primitive wire values shared by the native packet readers. */
public final class BukkitPacketData {
    private BukkitPacketData() {}
    public static int varInt(ByteBuf data) {
        int value = 0;
        for (int shift = 0; shift < 35; shift += 7) {
            int next = data.readUnsignedByte();
            value |= (next & 127) << shift;
            if ((next & 128) == 0)
                return value;
        }
        throw new IllegalArgumentException("Invalid VarInt");
    }
    public static String string(ByteBuf data) {
        int size = varInt(data);
        if (size < 0 || size > data.readableBytes())
            throw new IllegalArgumentException("Invalid packet string length");
        String text = data.toString(data.readerIndex(), size, StandardCharsets.UTF_8);
        data.skipBytes(size);
        return text;
    }
}
