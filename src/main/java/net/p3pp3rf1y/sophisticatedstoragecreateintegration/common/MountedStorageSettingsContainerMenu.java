package net.p3pp3rf1y.sophisticatedstoragecreateintegration.common;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
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
import net.p3pp3rf1y.sophisticatedcore.inventory.ContainerContents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.*;
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
	private ContainerContents.SettingsData lastLinkedSettingsData;

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
		return level.getEntity(entityId) instanceof AbstractContraptionEntity entity
				&& ContraptionHelper.getMountedStorage(entity, localPos) instanceof MountedSophisticatedStorage storage ? storage : null;
	}

	@Override
	public boolean stillValid(Player player) {
		MountedSophisticatedStorage storage = getStorage(player.level(), getContraptionEntityId(), getLocalPos());
		return storage != null && player.level().getEntity(getContraptionEntityId()) instanceof AbstractContraptionEntity entity && entity.isAlive()
				&& player.isWithinEntityInteractionRange(entity, 4.0F) && openedItem == storage.getStorageStack().getItem()
				&& Objects.equals(openedEndpoint, storage.getStorageStack().get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT))
				&& openedWrapper == storage.getStorageWrapper()
				&& (openedEndpoint == null || player.level().isClientSide() || player.level() instanceof ServerLevel level
						&& LinkedStorageGroupsSavedData.get(level).manager().isEndpointMember(openedEndpoint.groupId(), openedEndpoint.endpointId()));
	}

	@Override
	public void detectSettingsChangeAndReload() {
		if (openedEndpoint != null && player.level().isClientSide()) {
			boolean snapshotChanged = ClientLinkedStorageContents.removeUpdatedGroup(openedEndpoint.groupId());
			boolean settingsChanged = ClientLinkedStorageContents.removeUpdatedSettings(openedEndpoint.groupId());
			if (snapshotChanged || settingsChanged)
				ClientLinkedStorageContents.getContents(openedEndpoint.groupId()).ifPresent(contents -> {
					MountedSophisticatedStorage storage = getStorage(player.level(), getContraptionEntityId(), getLocalPos());
					if (snapshotChanged && storage != null)
						storage.getStorageHolder().refreshClientLinkedStorage();
					storageWrapper.getSettingsHandler().reloadFrom(contents.getContents(openedEndpoint.groupId()).settings());
				});
			return;
		}
		super.detectSettingsChangeAndReload();
	}

	@Override
	protected void sendStorageSettingsToClient() {
		if (openedEndpoint != null) {
			ContainerContents.SettingsData settingsData = storageWrapper.getSettingsHandler().getSettingsData();
			if (lastLinkedSettingsData == null || !lastLinkedSettingsData.equals(settingsData)) {
				lastLinkedSettingsData = settingsData.copy();
				if (player instanceof ServerPlayer serverPlayer && stillValid(player))
					PacketDistributor.sendToPlayer(serverPlayer, new LinkedStorageSettingsPayload(openedEndpoint.groupId(), lastLinkedSettingsData));
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
	protected CompoundTag getSettingsTag(CompoundTag contents) {
		return contents.getCompoundOrEmpty(MovingStorageWrapper.SETTINGS);
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
	public ItemStack getStorageSettingsTabIcon() {
		MountedSophisticatedStorage storage = getStorage(player.level(), getContraptionEntityId(), getLocalPos());
		return openedEndpoint != null && storage != null ? storage.getStorageStack() : super.getStorageSettingsTabIcon();
	}

	@Override
	protected CustomPacketPayload instantiateSettingsPayload(UUID uuid, ContainerContents.SettingsData settingsContents) {
		return new MountedStorageSettingsPayload(uuid, settingsContents);
	}

	@Override
	protected void updateFromContents(UUID uuid) {
		MountedStorageData storage = MountedStorageData.get();
		if (storage.removeUpdatedStorageSettingsFlag(uuid)) {
			ContainerContents contents = storage.getContents(uuid);
			storageWrapper.getSettingsHandler().reloadFrom(contents.settings());
		}
	}
}
