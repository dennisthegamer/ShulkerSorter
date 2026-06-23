package de.dennisthegamer.shulkersort.network;

import de.dennisthegamer.shulkersort.ShulkerSort;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SortRequestPayload(Action action) implements CustomPayload {

    public enum Action { SORT, UNDO }

    public static final Id<SortRequestPayload> TYPE = new Id<>(
            Identifier.of(ShulkerSort.MOD_ID, "sort_request"));

    public static final PacketCodec<PacketByteBuf, SortRequestPayload> CODEC =
            CustomPayload.codecOf(SortRequestPayload::write, SortRequestPayload::read);

    private void write(PacketByteBuf buf) {
        buf.writeByte(action.ordinal());
    }

    private static SortRequestPayload read(PacketByteBuf buf) {
        return new SortRequestPayload(Action.values()[buf.readByte()]);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}
