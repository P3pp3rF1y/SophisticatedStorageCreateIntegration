package net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageData;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.inventory.ContainerContents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.*;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageLinkedStorageHostWrapper;

import java.util.UUID;

public final class MountedLinkedStorageEndpointAdapter implements ILinkedStorageEndpointAdapter<ILinkedStorageBlockEndpoint> {
	public static final MountedLinkedStorageEndpointAdapter INSTANCE = new MountedLinkedStorageEndpointAdapter();

	private MountedLinkedStorageEndpointAdapter() {
	}

	@Override
	public Identifier factoryId() {
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
		return ResourceHandlerUtil.isEmpty(wrapper.getInventoryHandler()) && ResourceHandlerUtil.isEmpty(wrapper.getUpgradeHandler())
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
	public ContainerContents copyCanonicalContents(ServerLevel level, ILinkedStorageBlockEndpoint endpoint) {
		MountedStorageHolder holder = requireHolder(endpoint);
		UUID uuid = holder.getInstalledStorageItem().get(ModCoreDataComponents.STORAGE_UUID);
		return uuid == null ? new ContainerContents() : MountedStorageData.get().getContents(uuid).copy();
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
			MountedStorageData.get().removeStorageContents(previousUuid);
		}
	}

	private static MountedStorageHolder requireHolder(ILinkedStorageBlockEndpoint endpoint) {
		if (endpoint instanceof MountedStorageHolder holder) {
			return holder;
		}
		throw new IllegalArgumentException("Unsupported mounted storage endpoint");
	}
}
