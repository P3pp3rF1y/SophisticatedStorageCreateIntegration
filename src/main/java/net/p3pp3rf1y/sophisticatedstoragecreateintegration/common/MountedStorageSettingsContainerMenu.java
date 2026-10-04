package net.p3pp3rf1y.sophisticatedstoragecreateintegration.common;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.compat.create.*;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.ClientLinkedStorageContents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointData;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageSettingsPayload;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.init.ModContent;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage.MountedSophisticatedStorage;

import java.util.Objects;
import java.util.UUID;

public class MountedStorageSettingsContainerMenu extends MountedStorageSettingsContainerMenuBase {
	private final boolean doubleChest;
	private final LinkedStorageEndpointData openedEndpoint;
	private final Object openedItem;
	private final IStorageWrapper openedWrapper;
	private CompoundTag lastLinkedSettingsNbt;
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
		MountedSophisticatedStorage storage = getStorage(player.level(), contraptionEntityId, localPos);
		openedEndpoint = storage == null ? null : storage.getStorageStack().get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT);
		openedItem = storage == null ? null : storage.getStorageStack().getItem();
		openedWrapper = storageWrapper;
	}

	private static MountedSophisticatedStorage getStorage(Level level, int entityId, BlockPos localPos) {
		if (level.getEntity(entityId) instanceof AbstractContraptionEntity entity
				&& ContraptionHelper.getMountedStorage(entity, localPos) instanceof MountedSophisticatedStorage storage) {
			return storage;
		}
		return null;
	}

	@Override
	public boolean stillValid(Player player) {
		MountedSophisticatedStorage storage = getStorage(player.level(), getContraptionEntityId(), getLocalPos());
		return storage != null && player.level().getEntity(getContraptionEntityId()) instanceof AbstractContraptionEntity entity && entity.isAlive()
				&& player.canInteractWithEntity(entity, 4.0F) && openedItem == storage.getStorageStack().getItem()
				&& Objects.equals(openedEndpoint, storage.getStorageStack().get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT))
				&& openedWrapper == storage.getStorageWrapper();
	}

	@Override
	public void detectSettingsChangeAndReload() {
		if (openedEndpoint != null && player.level().isClientSide) {
			boolean snapshotChanged = ClientLinkedStorageContents.removeUpdatedGroup(openedEndpoint.groupId());
			boolean settingsChanged = ClientLinkedStorageContents.removeUpdatedSettings(openedEndpoint.groupId());
			if (snapshotChanged || settingsChanged) {
				ClientLinkedStorageContents.getContents(openedEndpoint.groupId()).ifPresent(contents -> {
					MountedSophisticatedStorage storage = getStorage(player.level(), getContraptionEntityId(), getLocalPos());
					if (snapshotChanged && storage != null) {
						storage.getStorageHolder().refreshClientLinkedStorage();
					}
					storageWrapper.getSettingsHandler().reloadFrom(contents.getContents().getCompound("settings"));
				});
			}
			return;
		}
		super.detectSettingsChangeAndReload();
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
	public ItemStack getStorageSettingsTabIcon() {
		MountedSophisticatedStorage storage = getStorage(player.level(), getContraptionEntityId(), getLocalPos());
		return openedEndpoint != null && storage != null ? storage.getStorageStack() : super.getStorageSettingsTabIcon();
	}

	@Override
	protected CompoundTag getSettingsTag(CompoundTag contents) {
		return contents.getCompound(MovingStorageWrapper.SETTINGS_TAG);
	}

	public static MountedStorageSettingsContainerMenu fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		MountedLinkedStorageMenuData.Position position = MountedLinkedStorageMenuData.read(buffer, playerInventory.player);
		return new MountedStorageSettingsContainerMenu(windowId, playerInventory.player, position.entityId(), position.localPos());
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
		MountedStorageData storage = MountedStorageData.get(uuid);
		if (storage.removeUpdatedStorageSettingsFlag(uuid)) {
			CompoundTag contents = storage.getContents();
			storageWrapper.getSettingsHandler().reloadFrom(getSettingsTag(contents));
		}
	}
}
