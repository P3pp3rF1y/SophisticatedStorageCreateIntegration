package net.p3pp3rf1y.sophisticatedstoragecreateintegration.common;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.compat.create.*;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.init.ModContent;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage.MountedSophisticatedStorage;

import java.util.UUID;

public class MountedStorageSettingsContainerMenu extends MountedStorageSettingsContainerMenuBase {
	private final boolean doubleChest;
	protected MountedStorageSettingsContainerMenu(int windowId, Player player, int contraptionEntityId, BlockPos localPos) {
		this(ModContent.MOUNTED_STORAGE_SETTINGS_CONTAINER_TYPE.get(), windowId, player, contraptionEntityId, localPos);
	}

	protected MountedStorageSettingsContainerMenu(MenuType<?> menuType, int windowId, Player player, int contraptionEntityId, BlockPos localPos) {
		super(menuType, windowId, player, getWrapper(player.level(), contraptionEntityId, localPos), contraptionEntityId, localPos);
		if (getPlayer().level().getEntity(getContraptionEntityId()) instanceof AbstractContraptionEntity cEntity) {
			doubleChest = ContraptionHelper.getMountedStorage(cEntity, getLocalPos()) instanceof MountedSophisticatedStorage mountedSophisticatedStorage
					&& mountedSophisticatedStorage.getStorageHolder().isDoubleChest();
		} else {
			doubleChest = false;
		}
	}

	private static IStorageWrapper getWrapper(Level level, int contraptionEntityId, BlockPos localPos) {
		if (!(level.getEntity(contraptionEntityId) instanceof AbstractContraptionEntity contraptionEntity)) {
			return NoopStorageWrapper.INSTANCE;
		}
		MountedStorageBase itemStorage = ContraptionHelper.getMountedStorage(contraptionEntity, localPos);
		if (itemStorage == null) {
			return NoopStorageWrapper.INSTANCE;
		}

		return itemStorage.getStorageWrapper();
	}

	@Override
	protected CompoundTag getSettingsTag(CompoundTag contents) {
		return contents.getCompoundOrEmpty(MovingStorageWrapper.SETTINGS_TAG);
	}

	public static MountedStorageSettingsContainerMenu fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		return new MountedStorageSettingsContainerMenu(windowId, playerInventory.player, buffer.readInt(), buffer.readBlockPos());
	}

	@Override
	public boolean supportsItemDisplaySideSelection() {
		return doubleChest;
	}

	@Override
	protected CustomPacketPayload instantiateSettingsPayload(UUID uuid, CompoundTag settingsContents) {
		return new MountedStorageContentsPayload(uuid, settingsContents);
	}

	@Override
	protected void updateFromContents(UUID uuid) {
		MountedStorageData storage = MountedStorageData.get();
		if (storage.removeUpdatedStorageSettingsFlag(uuid)) {
			CompoundTag contents = storage.getContents(uuid);
			storageWrapper.getSettingsHandler().reloadFrom(getSettingsTag(contents));
		}
	}
}
