package net.p3pp3rf1y.sophisticatedstoragecreateintegration.common;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.ISyncedContainer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageContentsPayload;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageSettingsContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.init.ModContent;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage.MountedSophisticatedStorage;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MountedStorageContainerMenu extends MountedStorageContainerMenuBase implements ISyncedContainer {
	public MountedStorageContainerMenu(int containerId, Player player, int contraptionEntityId, BlockPos localPos) {
		this(ModContent.MOUNTED_STORAGE_CONTAINER_TYPE.get(), containerId, player, contraptionEntityId, localPos);
	}

	public MountedStorageContainerMenu(MenuType<?> menuType, int containerId, Player player, int contraptionEntityId, BlockPos localPos) {
		super(menuType, containerId, player, NoopStorageWrapper.INSTANCE, -1, false, contraptionEntityId, localPos);
		getContraptionEntity().ifPresent(contraptionEntity -> {
			if (mountedStorage instanceof MountedSophisticatedStorage mountedSophisticatedStorage) {
				mountedSophisticatedStorage.getStorageHolder().startOpen(player, contraptionEntity);
			}
		});
	}

	public static MountedStorageContainerMenu fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		return new MountedStorageContainerMenu(windowId, playerInventory.player, buffer.readInt(), buffer.readBlockPos());
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		getContraptionEntity().ifPresent(contraptionEntity -> {
			if (mountedStorage instanceof MountedSophisticatedStorage mountedSophisticatedStorage) {
				mountedSophisticatedStorage.getStorageHolder().stopOpen(player, contraptionEntity);
			}
		});
	}

	@Override
	protected StorageContainerMenuBase<IStorageWrapper>.StorageUpgradeSlot instantiateUpgradeSlot(UpgradeHandler upgradeHandler, int slotIndex) {
		return new StorageUpgradeSlot(upgradeHandler, slotIndex) {
			@Override
			protected void onUpgradeChanged() {
				if (player.level().isClientSide()) {
					return;
				}
				storageWrapper.getSettingsHandler().getTypeCategory(ItemDisplaySettingsCategory.class).itemsChanged();
			}
		};
	}

	@Override
	protected MountedStorageSettingsContainerMenuBase instantiateSettingsContainerMenu(int windowId, Player player, int contraptionEntityId, BlockPos localPos) {
		return new MountedStorageSettingsContainerMenu(windowId, player, contraptionEntityId, localPos);
	}

	@Override
	protected void writeSettingsContainerMenuExtraData(FriendlyByteBuf buffer) {
		buffer.writeInt(getEntity().map(Entity::getId).orElse(-1));
		buffer.writeBlockPos(localPos);
	}

	@Override
	protected CompoundTag getSettingsTag(CompoundTag contents) {
		return contents.getCompound(MovingStorageWrapper.SETTINGS_TAG);
	}

	public float getSlotFillPercentage(int slot) {
		List<Float> slotFillRatios = getMountedStorage().map(m -> m.getStorageWrapper().getRenderInfo().getItemDisplayRenderInfo().getSlotFillRatios()).orElse(Collections.emptyList());
		return slot > -1 && slot < slotFillRatios.size() ? slotFillRatios.get(slot) : 0;
	}

	@Override
	protected String getSettingsTitleKey() {
		return StorageTranslationHelper.INSTANCE.translGui("settings.title");
	}

	@Override
	protected CustomPacketPayload instantiateSettingsPayload(UUID uuid, CompoundTag settingsContents) {
		return new MountedStorageContentsPayload(uuid, settingsContents);
	}
}
