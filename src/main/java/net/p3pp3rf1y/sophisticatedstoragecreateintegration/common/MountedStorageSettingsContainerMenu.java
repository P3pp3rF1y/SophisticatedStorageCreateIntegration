package net.p3pp3rf1y.sophisticatedstoragecreateintegration.common;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageSettingsContainerMenuBase;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.init.ModContent;

public class MountedStorageSettingsContainerMenu extends MountedStorageSettingsContainerMenuBase {
	protected MountedStorageSettingsContainerMenu(int windowId, Player player, int contraptionEntityId, BlockPos localPos) {
		this(ModContent.MOUNTED_STORAGE_SETTINGS_CONTAINER_TYPE.get(), windowId, player, contraptionEntityId, localPos);
	}

	protected MountedStorageSettingsContainerMenu(MenuType<?> menuType, int windowId, Player player, int contraptionEntityId, BlockPos localPos) {
		super(menuType, windowId, player, contraptionEntityId, localPos);
	}

	@Override
	protected CompoundTag getSettingsTag(CompoundTag contents) {
		return contents.getCompound(MovingStorageWrapper.SETTINGS_TAG);
	}

	public static MountedStorageSettingsContainerMenu fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		return new MountedStorageSettingsContainerMenu(windowId, playerInventory.player, buffer.readInt(), buffer.readBlockPos());
	}
}
