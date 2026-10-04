package net.p3pp3rf1y.sophisticatedstoragecreateintegration.common;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.compat.create.ContraptionHelper;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.ClientLinkedStorageContents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointData;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageGroupManager;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageGroupsSavedData;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageLinkedStorageHostWrapper;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage.MountedSophisticatedStorage;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class MountedLinkedStorageMenuData {
	private MountedLinkedStorageMenuData() {
	}

	public static void write(FriendlyByteBuf buffer, Player player, MountedSophisticatedStorage storage) {
		if (!(player.level() instanceof ServerLevel level)) {
			buffer.writeBoolean(false);
			return;
		}
		LinkedStorageEndpointData endpoint = storage.getStorageStack().get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT);
		if (endpoint == null) {
			buffer.writeBoolean(false);
			return;
		}
		LinkedStorageGroupManager manager = LinkedStorageGroupsSavedData.get(level).manager();
		Optional<StorageLinkedStorageHostWrapper> host = manager.resolveVirtualHost(endpoint.groupId())
				.filter(StorageLinkedStorageHostWrapper.class::isInstance).map(StorageLinkedStorageHostWrapper.class::cast);
		if (!manager.isEndpointMember(endpoint.groupId(), endpoint.endpointId()) || host.isEmpty()) {
			buffer.writeBoolean(false);
			return;
		}
		buffer.writeBoolean(true);
		buffer.writeUUID(endpoint.groupId());
		buffer.writeVarLong(manager.getRevision(endpoint.groupId()));
		ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buffer, host.get().getDisplayName());
		FriendlyByteBuf.writeNbt(buffer, manager.resolveContents(endpoint.groupId()).orElseThrow().getContents());
		buffer.writeVarInt(host.get().getInventoryHandler().getSlots());
		buffer.writeVarInt(host.get().getUpgradeHandler().getSlots());
		buffer.writeVarInt(host.get().getColumnsTaken());
		FriendlyByteBuf.writeNbt(buffer, host.get().getVirtualCarrierSnapshot().orElseThrow());
	}

	public static Position read(FriendlyByteBuf buffer, Player player) {
		int entityId = buffer.readInt();
		BlockPos localPos = buffer.readBlockPos();
		if (buffer.readBoolean()) {
			UUID groupId = buffer.readUUID();
			long revision = buffer.readVarLong();
			Component name = ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buffer);
			CompoundTag contents = Objects.requireNonNull(buffer.readNbt());
			int inventorySlots = buffer.readVarInt();
			int upgradeSlots = buffer.readVarInt();
			int columnsTaken = buffer.readVarInt();
			CompoundTag carrier = Objects.requireNonNull(buffer.readNbt());
			ClientLinkedStorageContents.updateContents(groupId, revision, contents, name, inventorySlots, upgradeSlots, columnsTaken);
			StorageLinkedStorageHostWrapper.applyClientSnapshotProfile(carrier, name, inventorySlots, upgradeSlots);
			if (player.level().getEntity(entityId) instanceof AbstractContraptionEntity entity
					&& ContraptionHelper.getMountedStorage(entity, localPos) instanceof MountedSophisticatedStorage storage
					&& storage.getStorageStack().get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT) instanceof LinkedStorageEndpointData endpoint
					&& groupId.equals(endpoint.groupId())) {
				ClientLinkedStorageContents.getContents(groupId)
						.ifPresent(snapshot -> storage.getStorageHolder().bindClientLinkedStorage(StorageLinkedStorageHostWrapper.create(snapshot, carrier)));
				// The menu will bind its slots to this initial snapshot, so it must not trigger a later host reload.
				ClientLinkedStorageContents.removeUpdatedGroup(groupId);
			}
		}
		return new Position(entityId, localPos);
	}

	public record Position(int entityId, BlockPos localPos) {
	}
}
