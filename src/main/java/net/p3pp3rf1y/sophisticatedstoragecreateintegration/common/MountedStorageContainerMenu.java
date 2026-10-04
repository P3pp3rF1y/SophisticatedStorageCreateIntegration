package net.p3pp3rf1y.sophisticatedstoragecreateintegration.common;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.ISyncedContainer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageContentsPayload;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageSettingsContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.ClientLinkedStorageContents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointData;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointRole;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageSettingsPayload;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.init.ModContent;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage.MountedSophisticatedStorage;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class MountedStorageContainerMenu extends MountedStorageContainerMenuBase implements ISyncedContainer {
	public Optional<LinkedStorageEndpointRole> getInstalledLinkedStorageEndpointRole() {
		return getMountedStorage().flatMap(storage -> StorageBlockEntity.getLinkedStorageEndpointRole(storage.getStorageStack()));
	}

	private final LinkedStorageEndpointData openedEndpoint;
	private final Object openedItem;
	private final IStorageWrapper openedWrapper;
	private CompoundTag lastLinkedSettingsNbt;
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
		openedEndpoint = mountedStorage.getStorageStack().get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT);
		openedItem = mountedStorage.getStorageStack().getItem();
		openedWrapper = storageWrapper;
	}

	public static MountedStorageContainerMenu fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		MountedLinkedStorageMenuData.Position position = MountedLinkedStorageMenuData.read(buffer, playerInventory.player);
		return new MountedStorageContainerMenu(windowId, playerInventory.player, position.entityId(), position.localPos());
	}

	@Override
	public boolean stillValid(Player player) {
		if (!super.stillValid(player) || !(getMountedStorage().orElse(null) instanceof MountedSophisticatedStorage storage)) {
			return false;
		}
		if (storage.getStorageStack().getItem() != openedItem
				|| !Objects.equals(openedEndpoint, storage.getStorageStack().get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT))
				|| storageWrapper != storage.getStorageWrapper()) {
			return false;
		}
		return openedEndpoint == null || openedWrapper == storage.getStorageHolder().getStorageWrapper();
	}

	@Override
	public boolean detectSettingsChangeAndReload() {
		if (openedEndpoint != null && player.level().isClientSide) {
			boolean snapshotChanged = ClientLinkedStorageContents.removeUpdatedGroup(openedEndpoint.groupId());
			boolean settingsChanged = ClientLinkedStorageContents.removeUpdatedSettings(openedEndpoint.groupId());
			return (snapshotChanged || settingsChanged) && ClientLinkedStorageContents.getContents(openedEndpoint.groupId()).map(contents -> {
				if (snapshotChanged && mountedStorage instanceof MountedSophisticatedStorage storage) {
					storage.getStorageHolder().refreshClientLinkedStorage();
				}
				storageWrapper.getSettingsHandler().reloadFrom(contents.getContents().getCompound("settings"));
				if (snapshotChanged) {
					refreshUpgradeControls();
				}
				return true;
			}).orElse(false);
		}
		return super.detectSettingsChangeAndReload();
	}

	@Override
	protected void sendStorageSettingsToClient() {
		if (openedEndpoint != null) {
			CompoundTag settings = storageWrapper.getSettingsHandler().getNbt();
			if (lastLinkedSettingsNbt == null || !lastLinkedSettingsNbt.equals(settings)) {
				lastLinkedSettingsNbt = settings.copy();
				if (player instanceof ServerPlayer serverPlayer && stillValid(player)) {
					PacketDistributor.sendToPlayer(serverPlayer, new LinkedStorageSettingsPayload(openedEndpoint.groupId(), lastLinkedSettingsNbt));
				}
			}
			return;
		}
		super.sendStorageSettingsToClient();
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
	protected MountedStorageSettingsContainerMenuBase instantiateSettingsContainerMenu(int windowId, Player player, int contraptionEntityId,
			BlockPos localPos) {
		return new MountedStorageSettingsContainerMenu(windowId, player, contraptionEntityId, localPos);
	}

	@Override
	protected void writeSettingsContainerMenuExtraData(FriendlyByteBuf buffer) {
		buffer.writeInt(getEntity().map(Entity::getId).orElse(-1));
		buffer.writeBlockPos(localPos);
		MountedLinkedStorageMenuData.write(buffer, player, (MountedSophisticatedStorage) mountedStorage);
	}

	@Override
	protected CompoundTag getSettingsTag(CompoundTag contents) {
		return contents.getCompound(MovingStorageWrapper.SETTINGS_TAG);
	}

	public float getSlotFillPercentage(int slot) {
		List<Float> slotFillRatios = getMountedStorage().map(m -> m.getStorageWrapper().getRenderInfo().getItemDisplayRenderInfo().getSlotFillRatios())
				.orElse(Collections.emptyList());
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
