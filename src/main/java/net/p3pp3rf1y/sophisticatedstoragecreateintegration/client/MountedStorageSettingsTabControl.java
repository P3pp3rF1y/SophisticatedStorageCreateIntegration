package net.p3pp3rf1y.sophisticatedstoragecreateintegration.client;

import net.minecraft.core.BlockPos;
import net.p3pp3rf1y.sophisticatedcore.client.gui.Tab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageSettingsTabControl;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedStorageSettingsContainerMenu;

public class MountedStorageSettingsTabControl extends StorageSettingsTabControl {
	protected MountedStorageSettingsTabControl(MountedStorageSettingsScreen screen, Position position) {
		super(screen, position);
	}

	@Override
	protected Tab instantiateReturnBackTab() {
		int contraptionEntityId = -1;
		BlockPos localPos = BlockPos.ZERO;
		if (screen.getMenu() instanceof MountedStorageSettingsContainerMenu menu) {
			contraptionEntityId = menu.getContraptionEntityId();
			localPos = menu.getLocalPos();
		}
		return new BackToMountedStorageTab(new Position(x, getTopY()), contraptionEntityId, localPos);
	}
}
