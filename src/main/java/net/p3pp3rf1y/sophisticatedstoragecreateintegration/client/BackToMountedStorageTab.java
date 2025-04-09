package net.p3pp3rf1y.sophisticatedstoragecreateintegration.client;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.client.gui.Tab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ImageButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.*;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorage.network.StoragePacketHandler;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage.OpenMountedStorageInventoryMessage;

public class BackToMountedStorageTab extends Tab {
	private static final TextureBlitData ICON = new TextureBlitData(GuiHelper.ICONS, Dimension.SQUARE_256, new UV(64, 80), Dimension.SQUARE_16);

	private final int contraptionEntityId;
	private final BlockPos localPos;

	protected BackToMountedStorageTab(Position position, int contraptionEntityId, BlockPos localPos) {
		super(position, Component.translatable(StorageTranslationHelper.INSTANCE.translGui("back_to_storage.tooltip")),
				onTabIconClicked -> new ImageButton(new Position(position.x() + 1, position.y() + 4), Dimension.SQUARE_16, ICON, onTabIconClicked));
		this.contraptionEntityId = contraptionEntityId;
		this.localPos = localPos;
	}

	@Override
	protected void onTabIconClicked(int button) {
		StoragePacketHandler.INSTANCE.sendToServer(new OpenMountedStorageInventoryMessage(contraptionEntityId, localPos));
	}
}
