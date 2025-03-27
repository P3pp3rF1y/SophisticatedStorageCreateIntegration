package net.p3pp3rf1y.sophisticatedstoragecreateintegration.client;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedStorageContainerMenu;

public class MountedStorageScreen extends StorageScreenBase<MountedStorageContainerMenu> {
	public static MountedStorageScreen constructScreen(MountedStorageContainerMenu screenContainer, Inventory inv, Component title) {
		return new MountedStorageScreen(screenContainer, inv, title);
	}

	protected MountedStorageScreen(MountedStorageContainerMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}

	@Override
	protected String getStorageSettingsTabTooltip() {
		return StorageTranslationHelper.INSTANCE.translGui("settings.tooltip");
	}
}
