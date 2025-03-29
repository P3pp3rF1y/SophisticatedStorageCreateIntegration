package net.p3pp3rf1y.sophisticatedstoragecreateintegration.client;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.compat.create.OpenMountedStorageInventoryMessage;
import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;
import net.p3pp3rf1y.sophisticatedcore.settings.StorageSettingsTabControlBase;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageSettingsScreen;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedStorageSettingsContainerMenu;

public class MountedStorageSettingsScreen extends StorageSettingsScreen {
	private final int entityId;
	private final BlockPos localPos;

	public MountedStorageSettingsScreen(SettingsContainerMenu<?> screenContainer, Inventory inv, Component title) {
		super(screenContainer, inv, title);
		entityId = screenContainer instanceof MountedStorageSettingsContainerMenu m ? m.getContraptionEntityId() : -1;
		localPos = screenContainer instanceof MountedStorageSettingsContainerMenu m ? m.getLocalPos() : BlockPos.ZERO;
	}

	@Override
	protected StorageSettingsTabControlBase initializeTabControl() {
		return new MountedStorageSettingsTabControl(this, new Position(leftPos + imageWidth, topPos + 4));
	}

	@Override
	protected void sendStorageInventoryScreenOpenMessage() {
		PacketHandler.INSTANCE.sendToServer(new OpenMountedStorageInventoryMessage(entityId, localPos));
	}

	public static MountedStorageSettingsScreen constructScreen(SettingsContainerMenu<?> screenContainer, Inventory inventory, Component title) {
		return new MountedStorageSettingsScreen(screenContainer, inventory, title);
	}
}
