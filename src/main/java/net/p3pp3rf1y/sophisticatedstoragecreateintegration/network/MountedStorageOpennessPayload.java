package net.p3pp3rf1y.sophisticatedstoragecreateintegration.network;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedcore.compat.create.ContraptionHelper;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageBase;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.SophisticatedStorageCreateIntegration;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage.MountedSophisticatedStorage;
import org.jspecify.annotations.Nullable;

public record MountedStorageOpennessPayload(int contraptionEntityId, BlockPos localPos, boolean shouldBeOpen) implements CustomPacketPayload {
	public static final Type<MountedStorageOpennessPayload> TYPE = new Type<>(SophisticatedStorageCreateIntegration.getIdentifier("storage_openness"));
	public static final StreamCodec<RegistryFriendlyByteBuf, MountedStorageOpennessPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT,
			MountedStorageOpennessPayload::contraptionEntityId,
			BlockPos.STREAM_CODEC,
			MountedStorageOpennessPayload::localPos,
			ByteBufCodecs.BOOL,
			MountedStorageOpennessPayload::shouldBeOpen,
			MountedStorageOpennessPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(MountedStorageOpennessPayload payload, IPayloadContext context) {
		Player player = context.player();
		Entity entity = player.level().getEntity(payload.contraptionEntityId());
		if (entity instanceof AbstractContraptionEntity contraptionEntity) {
			@Nullable MountedStorageBase mountedStorage = ContraptionHelper.getMountedStorage(contraptionEntity, payload.localPos());
			if (!(mountedStorage instanceof MountedSophisticatedStorage mountedSophisticatedStorage)) {
				return;
			}
			mountedSophisticatedStorage.setShouldBeOpen(payload.shouldBeOpen());
		}
	}
}
