package net.p3pp3rf1y.sophisticatedstoragecreateintegration.common;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.compat.create.ContraptionHelper;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.inventory.ContainerContents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.*;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageLinkedStorageHostWrapper;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage.MountedSophisticatedStorage;

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
		buffer.writeNbt((CompoundTag) ContainerContents.CODEC.encodeStart(player.registryAccess().createSerializationContext(NbtOps.INSTANCE),
				manager.resolveContents(endpoint.groupId()).orElseThrow().getContents(endpoint.groupId())).getOrThrow());
		buffer.writeVarInt(host.get().getInventoryHandler().size());
		buffer.writeVarInt(host.get().getUpgradeHandler().size());
		buffer.writeVarInt(host.get().getColumnsTaken());
		buffer.writeNbt(host.get().getVirtualCarrierSnapshot().orElseThrow());
	}

	public static Position read(FriendlyByteBuf buffer, Player player) {
		int entityId = buffer.readInt();
		BlockPos localPos = buffer.readBlockPos();
		if (!buffer.readBoolean()) {
			return new Position(entityId, localPos);
		}
		UUID groupId = buffer.readUUID();
		long revision = buffer.readVarLong();
		Component name = ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buffer);
		CompoundTag contentsTag = buffer.readNbt();
		if (contentsTag == null) {
			return new Position(entityId, localPos);
		}
		ContainerContents contents = ContainerContents.CODEC.parse(player.registryAccess().createSerializationContext(NbtOps.INSTANCE), contentsTag)
				.getOrThrow();
		int inventorySlots = buffer.readVarInt();
		int upgradeSlots = buffer.readVarInt();
		int columnsTaken = buffer.readVarInt();
		CompoundTag carrier = buffer.readNbt();
		if (carrier == null) {
			return new Position(entityId, localPos);
		}
		ClientLinkedStorageContents.updateContents(groupId, revision, contents, name, inventorySlots, upgradeSlots, columnsTaken);
		CompoundTag profiledCarrier = StorageLinkedStorageHostWrapper.applyClientSnapshotProfile(carrier, name, inventorySlots, upgradeSlots);
		if (player.level().getEntity(entityId) instanceof AbstractContraptionEntity entity
				&& ContraptionHelper.getMountedStorage(entity, localPos) instanceof MountedSophisticatedStorage storage
				&& storage.getStorageStack().get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT) instanceof LinkedStorageEndpointData endpoint
				&& groupId.equals(endpoint.groupId())) {
			ClientLinkedStorageContents.getContents(groupId).ifPresent(
					snapshot -> storage.getStorageHolder().bindClientLinkedStorage(StorageLinkedStorageHostWrapper.create(snapshot, profiledCarrier)));
			ClientLinkedStorageContents.removeUpdatedGroup(groupId);
		}
		return new Position(entityId, localPos);
	}

	public record Position(int entityId, BlockPos localPos) {
	}
}
