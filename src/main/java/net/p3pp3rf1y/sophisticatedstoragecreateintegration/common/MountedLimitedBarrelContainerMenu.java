package net.p3pp3rf1y.sophisticatedstoragecreateintegration.common;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageSettingsContainerMenuBase;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.init.ModContent;

import java.util.List;

public class MountedLimitedBarrelContainerMenu extends MountedStorageContainerMenu {
	public MountedLimitedBarrelContainerMenu(int containerId, Player player, int contraptionEntityId, BlockPos localPos) {
		super(ModContent.MOUNTED_LIMITED_BARREL_CONTAINER_TYPE.get(), containerId, player, contraptionEntityId, localPos);
	}

	public static MountedLimitedBarrelContainerMenu fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		return new MountedLimitedBarrelContainerMenu(windowId, playerInventory.player, buffer.readInt(), buffer.readBlockPos());
	}

	@Override
	protected MountedStorageSettingsContainerMenuBase instantiateSettingsContainerMenu(int windowId, Player player, int entityId, BlockPos localPos) {
		return new MountedLimitedBarrelSettingsContainerMenu(windowId, player, entityId, localPos);
	}

	@Override
	public List<Integer> getSlotOverlayColors(int slot) {
		return List.of();
	}
}
