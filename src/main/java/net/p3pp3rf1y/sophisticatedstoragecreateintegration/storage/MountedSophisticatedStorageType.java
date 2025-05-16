package net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage;

import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;

import javax.annotation.Nullable;

public class MountedSophisticatedStorageType extends MountedItemStorageType<MountedSophisticatedStorage> {
	public MountedSophisticatedStorageType() {
		super(MountedSophisticatedStorage.CODEC);
	}

	@Override
	public @Nullable MountedSophisticatedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		return be instanceof StorageBlockEntity storage ? MountedSophisticatedStorage.from(level, storage) : null;
	}
}
