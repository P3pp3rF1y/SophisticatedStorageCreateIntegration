package net.p3pp3rf1y.sophisticatedstoragecreateintegration.common;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.init.ModContent;

public class MountedLimitedBarrelSettingsContainerMenu extends MountedStorageSettingsContainerMenu {
	protected MountedLimitedBarrelSettingsContainerMenu(int windowId, Player player, int contraptionEntityId, BlockPos localPos) {
		super(ModContent.MOUNTED_LIMITED_BARREL_SETTINGS_CONTAINER_TYPE.get(), windowId, player, contraptionEntityId, localPos);
	}

	public static MountedLimitedBarrelSettingsContainerMenu fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		return new MountedLimitedBarrelSettingsContainerMenu(windowId, playerInventory.player, buffer.readInt(), buffer.readBlockPos());
	}
}
