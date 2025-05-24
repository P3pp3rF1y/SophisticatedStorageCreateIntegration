package net.p3pp3rf1y.sophisticatedstoragecreateintegration.network;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.p3pp3rf1y.sophisticatedcore.compat.create.ContraptionHelper;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageBase;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage.MountedSophisticatedStorage;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record MountedStorageOpennessMessage(int contraptionEntityId, BlockPos localPos, boolean shouldBeOpen) {
	public static void encode(MountedStorageOpennessMessage msg, FriendlyByteBuf packetBuffer) {
		packetBuffer.writeInt(msg.contraptionEntityId);
		packetBuffer.writeBlockPos(msg.localPos);
		packetBuffer.writeBoolean(msg.shouldBeOpen);
	}

	public static MountedStorageOpennessMessage decode(FriendlyByteBuf packetBuffer) {
		return new MountedStorageOpennessMessage(
				packetBuffer.readInt(),
				packetBuffer.readBlockPos(),
				packetBuffer.readBoolean()
		);
	}

	static void onMessage(MountedStorageOpennessMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> handleMessage(msg));
		context.setPacketHandled(true);
	}

	private static void handleMessage(MountedStorageOpennessMessage msg) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}

		Entity entity = player.level().getEntity(msg.contraptionEntityId());
		if (entity instanceof AbstractContraptionEntity contraptionEntity) {
			@Nullable MountedStorageBase mountedStorage = ContraptionHelper.getMountedStorage(contraptionEntity, msg.localPos());
			if (!(mountedStorage instanceof MountedSophisticatedStorage mountedSophisticatedStorage)) {
				return;
			}
			mountedSophisticatedStorage.setShouldBeOpen(msg.shouldBeOpen());
		}
	}
}
