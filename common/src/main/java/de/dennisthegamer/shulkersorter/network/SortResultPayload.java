package de.dennisthegamer.shulkersorter.network;

import de.dennisthegamer.shulkersorter.ShulkerSorter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SortResultPayload(boolean success, int boxesSorted, int itemsMoved, String errorKey) implements CustomPacketPayload {

    public static final Type<SortResultPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShulkerSorter.MOD_ID, "sort_result"));

    public static final StreamCodec<FriendlyByteBuf, SortResultPayload> CODEC =
            CustomPacketPayload.codec(SortResultPayload::write, SortResultPayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeBoolean(success);
        buf.writeVarInt(boxesSorted);
        buf.writeVarInt(itemsMoved);
        buf.writeUtf(errorKey != null ? errorKey : "");
    }

    private static SortResultPayload read(FriendlyByteBuf buf) {
        boolean success = buf.readBoolean();
        int boxesSorted = buf.readVarInt();
        int itemsMoved = buf.readVarInt();
        String errorKey = buf.readUtf();
        return new SortResultPayload(success, boxesSorted, itemsMoved, errorKey.isEmpty() ? null : errorKey);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
