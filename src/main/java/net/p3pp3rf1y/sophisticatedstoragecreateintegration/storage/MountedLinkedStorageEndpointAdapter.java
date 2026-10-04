package net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageData;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.ILinkedStorageBlockEndpoint;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.ILinkedStorageEndpointAdapter;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointData;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageGroupsSavedData;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageHostDescriptor;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageLinkedStorageHostWrapper;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;

import java.util.UUID;

public final class MountedLinkedStorageEndpointAdapter implements ILinkedStorageEndpointAdapter<ILinkedStorageBlockEndpoint> {
	public static final MountedLinkedStorageEndpointAdapter INSTANCE = new MountedLinkedStorageEndpointAdapter();

	private MountedLinkedStorageEndpointAdapter() {
	}

	@Override
	public ResourceLocation factoryId() {
		return StorageLinkedStorageHostWrapper.FACTORY_ID;
	}

	@Override
	public Compatibility getCompatibility(ServerLevel level, ILinkedStorageBlockEndpoint endpoint, LinkedStorageHostDescriptor hostDescriptor) {
		MountedStorageHolder holder = requireHolder(endpoint);
		if (!holder.isLinkedStorageLinkCandidate() || !StorageLinkedStorageHostWrapper.getCompatibilityKey(holder.getInstalledStorageItem())
				.equals(StorageLinkedStorageHostWrapper.getCompatibilityKey(hostDescriptor.virtualCarrier()))) {
			return Compatibility.INCOMPATIBLE;
		}
		IStorageWrapper wrapper = holder.getStorageWrapper();
		return InventoryHelper.isEmpty(wrapper.getInventoryHandler()) && InventoryHelper.isEmpty(wrapper.getUpgradeHandler())
				? Compatibility.COMPATIBLE
				: Compatibility.HAS_CONTENTS;
	}

	@Override
	public LinkedStorageHostDescriptor createHostDescriptor(ServerLevel level, ILinkedStorageBlockEndpoint endpoint) {
		MountedStorageHolder holder = requireHolder(endpoint);
		return new LinkedStorageHostDescriptor(factoryId(),
				StorageLinkedStorageHostWrapper.createVirtualCarrier(holder.getInstalledStorageItem(), holder.getStorageWrapper()));
	}

	@Override
	public CompoundTag copyCanonicalContents(ServerLevel level, ILinkedStorageBlockEndpoint endpoint) {
		MountedStorageHolder holder = requireHolder(endpoint);
		IStorageWrapper wrapper = holder.getStorageWrapper();
		UUID uuid = holder.getInstalledStorageItem().get(ModCoreDataComponents.STORAGE_UUID);
		CompoundTag contents = uuid == null ? new CompoundTag() : MountedStorageData.get(uuid).getContents().copy();
		contents.putInt(StorageWrapper.NUMBER_OF_INVENTORY_SLOTS_TAG, wrapper.getInventoryHandler().getSlots());
		contents.putInt(StorageWrapper.NUMBER_OF_UPGRADE_SLOTS_TAG, wrapper.getUpgradeHandler().getSlots());
		contents.putString(StorageWrapper.SORT_BY_TAG, wrapper.getSortBy().getSerializedName());
		return contents;
	}

	@Override
	public void bindEndpoint(ServerLevel level, ILinkedStorageBlockEndpoint endpoint, LinkedStorageEndpointData endpointData) {
		MountedStorageHolder holder = requireHolder(endpoint);
		ItemStack item = holder.getInstalledStorageItem().copy();
		UUID previousUuid = item.get(ModCoreDataComponents.STORAGE_UUID);
		item.remove(ModCoreDataComponents.STORAGE_UUID);
		item.set(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT, endpointData);
		if (LinkedStorageGroupsSavedData.get(level).manager().isPrimaryEndpoint(endpointData.groupId(), endpointData.endpointId())) {
			item.set(ModCoreDataComponents.LINKED_STORAGE_PRIMARY_ENDPOINT, true);
		} else {
			item.remove(ModCoreDataComponents.LINKED_STORAGE_PRIMARY_ENDPOINT);
		}
		holder.setStorageItem(item);
		if (previousUuid != null) {
			MountedStorageData.get(previousUuid).removeStorageContents();
		}
	}

	private static MountedStorageHolder requireHolder(ILinkedStorageBlockEndpoint endpoint) {
		if (endpoint instanceof MountedStorageHolder holder) {
			return holder;
		}
		throw new IllegalArgumentException("Unsupported mounted storage endpoint");
	}
}
