package net.p3pp3rf1y.sophisticatedstoragecreateintegration.schematic;

import com.simibubi.create.api.schematic.nbt.SafeNbtWriterRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.block.WoodStorageBlockEntity;

public class SophisticatedStorageSafeNbtWriter implements SafeNbtWriterRegistry.SafeNbtWriter {
	public static final SophisticatedStorageSafeNbtWriter INSTANCE = new SophisticatedStorageSafeNbtWriter();
	@Override
	public void writeSafe(BlockEntity be, CompoundTag tag, HolderLookup.Provider registries) {
		if (be instanceof StorageBlockEntity storageBlockEntity) {
			CompoundTag storageWrapperTag = new CompoundTag();
			storageWrapperTag.putInt(StorageWrapper.MAIN_COLOR, storageBlockEntity.getStorageWrapper().getMainColor());
			storageWrapperTag.putInt(StorageWrapper.ACCENT_COLOR, storageBlockEntity.getStorageWrapper().getAccentColor());
			tag.put(StorageBlockEntity.STORAGE_WRAPPER, storageWrapperTag);
		}
	}

	public static class Wooden extends SophisticatedStorageSafeNbtWriter {
		public static final Wooden INSTANCE = new Wooden();
		@Override
		public void writeSafe(BlockEntity be, CompoundTag tag, HolderLookup.Provider registries) {
			super.writeSafe(be, tag, registries);
			if (be instanceof WoodStorageBlockEntity woodStorageBlockEntity) {
				woodStorageBlockEntity.getWoodType().ifPresent(woodType -> {
					tag.putString("woodType", woodType.name());
				});
			}
		}
	}
}
