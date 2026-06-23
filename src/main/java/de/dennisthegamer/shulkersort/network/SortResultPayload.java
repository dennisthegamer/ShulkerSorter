package de.dennisthegamer.shulkersort.network;

import de.dennisthegamer.shulkersort.ShulkerSort;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SortResultPayload(boolean success, int boxesSorted, int itemsMoved, String errorKey) implements CustomPayload {

    public static final Id<SortResultPayload> TYPE = new Id<>(
            Identifier.of(ShulkerSort.MOD_ID, "sort_result"));

    public static final PacketCodec<PacketByteBuf, SortResultPayload> CODEC =
            CustomPayload.codecOf(SortResultPayload::write, SortResultPayload::read);

    private void write(PacketByteBuf buf) {
        buf.writeBoolean(success);
        buf.writeVarInt(boxesSorted);
        buf.writeVarInt(itemsMoved);
        buf.writeString(errorKey != null ? errorKey : "");
    }

    private static SortResultPayload read(PacketByteBuf buf) {
        boolean success = buf.readBoolean();
        int boxesSorted = buf.readVarInt();
        int itemsMoved = buf.readVarInt();
        String errorKey = buf.readString();
        return new SortResultPayload(success, boxesSorted, itemsMoved, errorKey.isEmpty() ? null : errorKey);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}
