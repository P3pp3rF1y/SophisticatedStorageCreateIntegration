package net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkHooks;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageBase;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageData;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedcore.util.WorldHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.*;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.WoodStorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.entity.StorageHolderTierUpgradeHandler;
import net.p3pp3rf1y.sophisticatedstorage.entity.StorageHolderToolHandler;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.*;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedLimitedBarrelContainerMenu;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedStorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.init.ModContent;

import javax.annotation.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

@SuppressWarnings("PMD.UnnecessaryImport")
public class MountedSophisticatedStorage extends MountedStorageBase {
	public static final Codec<MountedSophisticatedStorage> CODEC = ItemStack.CODEC.xmap(MountedSophisticatedStorage::new,
			MountedSophisticatedStorage::getStorageStack);
	protected static final Multimap<Class<? extends Item>, String> NBT_TO_TRANSFER = LinkedListMultimap.create();

	public static void registerNbtToTransfer(Class<? extends Item> itemClass, String tagName) {
		NBT_TO_TRANSFER.put(itemClass, tagName);
	}

	static {
		registerNbtToTransfer(StorageBlockItem.class, "displayName");
		registerNbtToTransfer(StorageBlockItem.class, "locked");
		registerNbtToTransfer(StorageBlockItem.class, "showLock");
		registerNbtToTransfer(StorageBlockItem.class, "showTier");
		registerNbtToTransfer(StorageBlockItem.class, "showUpgrades");
		registerNbtToTransfer(BarrelBlockItem.class, "showCounts");
		registerNbtToTransfer(BarrelBlockItem.class, "showFillLevels");
		registerNbtToTransfer(BarrelBlockItem.class, "slotColors");
		registerNbtToTransfer(WoodStorageBlockItem.class, "woodType");
		registerNbtToTransfer(WoodStorageBlockItem.class, WoodStorageBlockEntity.PACKED_TAG);
		registerNbtToTransfer(BarrelBlockItem.class, BarrelBlockEntity.MATERIALS_TAG);
	}

	private final MountedStorageHolder storageHolder;

	public MountedSophisticatedStorage(ItemStack storageStack) {
		super(ModContent.SOPHISTICATED_MOUNTED_STORAGE_TYPE.get(), storageStack);
		storageHolder = new MountedStorageHolder(this::getStorageStack, this::setStorageStack);
	}

	@Override
	public void setStorageStack(ItemStack stack) {
		super.setStorageStack(stack);
		if (storageHolder.getEntity() != null && !storageHolder.getEntity().level().isClientSide()) {
			storageHolder.setDirty();
		}
	}

	public static MountedSophisticatedStorage from(Level level, StorageBlockEntity storage) {
		boolean rightChestPart = storage instanceof ChestBlockEntity chestBe && !chestBe.isMainChest();

		storage.removeFromController();

		StorageWrapper storageWrapper = storage.getStorageWrapper();
		ItemStack storageItem = storageWrapper.getWrappedStorageStack();
		if (storageItem.getItem() instanceof ITintableBlockItem tintableBlockItem) {
			if (storageWrapper.getMainColor() != -1) {
				tintableBlockItem.setMainColor(storageItem, storageWrapper.getMainColor());
			}
			if (storageWrapper.getAccentColor() != -1) {
				tintableBlockItem.setAccentColor(storageItem, storageWrapper.getAccentColor());
			}
		}

		storage.getStorageWrapper().getSettingsHandler().getTypeCategory(ItemDisplaySettingsCategory.class).itemsChanged(); // update slot counts and fill
																															// levels

		CompoundTag fullBeNbt = storage.saveWithoutMetadata();

		CompoundTag contentsNbt = new CompoundTag();
		CompoundTag storageWrapperNbt = fullBeNbt.getCompound(StorageBlockEntity.STORAGE_WRAPPER_TAG);
		contentsNbt.put(StorageWrapper.CONTENTS_TAG, storageWrapperNbt.getCompound(StorageWrapper.CONTENTS_TAG));
		contentsNbt.put(StorageWrapper.SETTINGS_TAG, storageWrapperNbt.getCompound(StorageWrapper.SETTINGS_TAG));

		transferNbtIfPresent(storageWrapperNbt, StorageWrapper.RENDER_INFO_TAG, storageItem::getOrCreateTag);
		transferNbtIfPresent(storageWrapperNbt, StorageWrapper.SORT_BY_TAG, storageItem::getOrCreateTag);
		transferNbtIfPresent(storageWrapperNbt, StorageWrapper.NUMBER_OF_INVENTORY_SLOTS_TAG, storageItem::getOrCreateTag);
		transferNbtIfPresent(storageWrapperNbt, StorageWrapper.NUMBER_OF_UPGRADE_SLOTS_TAG, storageItem::getOrCreateTag);

		if (!rightChestPart) {
			UUID id = UUID.randomUUID();
			storageItem.getOrCreateTag().putUUID(StorageWrapper.UUID_TAG, id);
			MountedStorageData.get(id).setContents(contentsNbt);
			StorageBlockItem.setNumberOfInventorySlots(storageItem, storageWrapper.getInventoryHandler().getSlots());
			StorageBlockItem.setNumberOfUpgradeSlots(storageItem, storageWrapper.getUpgradeHandler().getSlots());
		}

		for (var entry : NBT_TO_TRANSFER.entries()) {
			if (entry.getKey().isInstance(storageItem.getItem())) {
				String tagName = entry.getValue();
				if (fullBeNbt.contains(tagName)) {
					// noinspection DataFlowIssue - contains check above
					storageItem.getOrCreateTag().put(tagName, fullBeNbt.get(tagName));
				}
			}
		}

		if (storage instanceof ChestBlockEntity chestBlock) {
			BlockState blockState = chestBlock.getBlockState();
			if (blockState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
				ChestBlockItem.setDoubleChest(storageItem, true);
			}
		}

		MountedSophisticatedStorage mountedStorage = new MountedSophisticatedStorage(storageItem);
		if (!level.isClientSide()) {
			mountedStorage.getStorageHolder().setDirty();
		}
		return mountedStorage;
	}

	private static void transferNbtIfPresent(@Nullable CompoundTag sourceNbt, String tagName, Supplier<CompoundTag> getTargetTag) {
		if (sourceNbt != null && sourceNbt.contains(tagName)) {
			// noinspection DataFlowIssue - contains check makes sure tag actually exists
			getTargetTag.get().put(tagName, sourceNbt.get(tagName));
		}
	}

	@Override
	public void updateWithSyncedStorageStack(ItemStack storageStack, boolean refreshBlockRender) {
		storageHolder.setStorageItem(storageStack);
		storageHolder.refreshRenders(refreshBlockRender);
	}

	@Override
	public IStorageWrapper getStorageWrapper() {
		return storageHolder.getStorageWrapper();
	}

	@Override
	public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		NBTHelper.getUniqueId(getStorageStack(), StorageWrapper.UUID_TAG).ifPresent(storageUuid -> {
			if (be instanceof StorageBlockEntity storageBe) {
				MountedStorageData mountedStorageData = MountedStorageData.get(storageUuid);
				CompoundTag fullBeNbt = new CompoundTag();

				CompoundTag contentNbt = mountedStorageData.getContents();
				transferNbtIfPresent(getStorageStack().getTag(), StorageWrapper.RENDER_INFO_TAG, () -> contentNbt);
				transferNbtIfPresent(getStorageStack().getTag(), StorageWrapper.SORT_BY_TAG, () -> contentNbt);
				transferNbtIfPresent(getStorageStack().getTag(), StorageWrapper.NUMBER_OF_INVENTORY_SLOTS_TAG, () -> contentNbt);
				transferNbtIfPresent(getStorageStack().getTag(), StorageWrapper.NUMBER_OF_UPGRADE_SLOTS_TAG, () -> contentNbt);

				fullBeNbt.put(StorageBlockEntity.STORAGE_WRAPPER_TAG, contentNbt);

				for (var entry : NBT_TO_TRANSFER.entries()) {
					if (entry.getKey().isInstance(getStorageStack().getItem())) {
						transferNbtIfPresent(getStorageStack().getTag(), entry.getValue(), () -> fullBeNbt);
					}
				}

				if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) { // prevent double chest update shape
																														// changes dropping items before both
																														// parts loaded
					storageBe.setBeingUpgraded(true);
				}
				storageBe.load(fullBeNbt);
				if (getStorageStack().getItem() instanceof ITintableBlockItem tintableBlockItem) {
					storageBe.getStorageWrapper().setColors(tintableBlockItem.getMainColor(getStorageStack()).orElse(-1),
							tintableBlockItem.getAccentColor(getStorageStack()).orElse(-1));
				}
				mountedStorageData.removeStorageContents();

				if (storageBe instanceof ChestBlockEntity chestBe && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
					BlockState inWorldState = level.getBlockState(pos);
					ChestType chestType = state.getValue(ChestBlock.TYPE);
					level.setBlock(pos, inWorldState.setValue(ChestBlock.TYPE, chestType), 3);

					Direction facing = inWorldState.getValue(ChestBlock.FACING);
					BlockPos connectedPos = pos.relative(chestType == ChestType.LEFT ? facing.getClockWise() : facing.getCounterClockWise());

					WorldHelper.getBlockEntity(level, connectedPos, ChestBlockEntity.class).ifPresent(otherHalfBe -> {
						ChestBlockEntity mainBe = chestType == ChestType.LEFT ? otherHalfBe : chestBe;
						ChestBlockEntity attachedBe = chestType == ChestType.LEFT ? chestBe : otherHalfBe;
						mainBe.setBeingUpgraded(false);
						attachedBe.setBeingUpgraded(false);
						mainBe.getStorageWrapper().onInit(level);
						mainBe.tryToAddToController();
					});
				} else {
					storageBe.getStorageWrapper().onInit(level);
					storageBe.tryToAddToController();
				}

			}
		});
	}

	public MountedStorageContainerMenuBase createMenu(int id, Player pl, int contraptionEntityId, BlockPos localPos) {
		if (MovingStorageWrapper.isLimitedBarrel(getStorageStack())) {
			return new MountedLimitedBarrelContainerMenu(id, pl, contraptionEntityId, localPos);
		} else {
			return new MountedStorageContainerMenu(id, pl, contraptionEntityId, localPos);
		}
	}

	@Override
	public boolean handleInteraction(ServerPlayer player, Contraption contraption, StructureTemplate.StructureBlockInfo info) {
		if (WoodStorageBlockItem.isPacked(getStorageStack())) {
			return false;
		}

		ServerLevel level = player.serverLevel();

		BlockPos localPos = info.pos();
		if (info.state().getBlock() instanceof ChestBlock && info.state().getValue(ChestBlock.TYPE) == ChestType.LEFT) {
			localPos = info.pos().relative(ChestBlock.getConnectedDirection(info.state()));
		}

		int contraptionEntityId = contraption.entity.getId();
		ItemStack itemInHand = player.getMainHandItem();
		if (itemInHand.getItem() instanceof StorageTierUpgradeItem tierUpgradeItem) {
			InteractionResult result = tryStorageTierUpgrade(player, itemInHand, tierUpgradeItem);
			if (result != InteractionResult.PASS) {
				return result == InteractionResult.SUCCESS;
			}
		} else if (itemInHand.getItem() instanceof StorageToolItem && tryToolInteraction(itemInHand)) {
			return true;
		} else if (itemInHand.getItem() instanceof UpgradeItemBase<?> && tryAddStorageUpgrade(player, itemInHand)) {
			return true;
		} else if (itemInHand.getItem() == ModItems.PAINTBRUSH.get() && tryPaintStorage(player, itemInHand)) {
			return true;
		}

		Vec3 localPosVec = Vec3.atCenterOf(localPos);

		openMenu(player, contraptionEntityId, localPos);

		Vec3 globalPos = contraption.entity.toGlobalVector(localPosVec, 0);
		onOpen(level, globalPos);
		return true;
	}

	private boolean tryToolInteraction(ItemStack itemInHand) {
		boolean result = StorageHolderToolHandler.tryStorageToolInteract(itemInHand, getStorageHolder()) == InteractionResult.SUCCESS;
		if (result && StorageToolItem.getMode(itemInHand) == StorageToolItem.Mode.LOCK) {
			storageHolder.updateClientBlockRender();
			storageHolder.sendStorageUpdatePayload();
		}
		return result;
	}

	private boolean tryAddStorageUpgrade(Player player, ItemStack itemInHand) {
		return StorageBlockBase.tryAddSingleUpgrade(player, InteractionHand.MAIN_HAND, itemInHand, getStorageWrapper());
	}

	private boolean tryPaintStorage(Player player, ItemStack paintbrush) {
		if (!(getStorageStack().getItem() instanceof BlockItem blockItem)) {
			return false;
		}
		BlockState state = blockItem.getBlock().defaultBlockState();
		SoundEvent placeSound = state.getSoundType().getPlaceSound();
		boolean painted = PaintbrushItem.paint(player, paintbrush, getStorageHolder(), getStorageWrapper(), getStorageHolder().getPosition(), Direction.UP,
				placeSound);
		if (painted) {
			storageHolder.updateClientBlockRender();
			storageHolder.sendStorageUpdatePayload();
		}
		return painted;
	}

	private InteractionResult tryStorageTierUpgrade(ServerPlayer player, ItemStack itemInHand, StorageTierUpgradeItem tierUpgradeItem) {
		InteractionResult result = StorageHolderTierUpgradeHandler.upgrade(player, getStorageHolder(), itemInHand, tierUpgradeItem);

		if (result == InteractionResult.SUCCESS) {
			if (getStorageStack().getItem() instanceof ChestBlockItem && ChestBlockItem.isDoubleChest(getStorageStack())) {
				if (storageHolder.getMainStorageHolder() instanceof MountedStorageHolder mainStorageHolder) {
					mainStorageHolder.updateState();
				}
				storageHolder.getAuxiliaryStorageHolder().filter(MountedStorageHolder.class::isInstance).map(MountedStorageHolder.class::cast)
						.ifPresent(MountedStorageHolder::updateState);
			} else {
				storageHolder.updateState();
			}
			return InteractionResult.SUCCESS;
		}

		return result;
	}

	public MountedStorageHolder getStorageHolder() {
		return storageHolder;
	}

	void initEntityLevelAndPositions(MovementContext context) {
		getStorageHolder().initEntityLevelAndPositions(context);
	}

	public void tick(Entity entity) {
		getStorageHolder().tick(entity);
	}

	public void clearNbt() {
		getStorageHolder().clearNbt();
	}

	@Override
	public void onContraptionDestroyed() {
		NBTHelper.getUniqueId(getStorageStack(), StorageWrapper.UUID_TAG).ifPresent(storageUuid -> MountedStorageData.get(storageUuid).removeStorageContents());
	}

	@Override
	protected IItemHandlerModifiable getExternalItemHandler() {
		return getStorageHolder().getMainStorageWrapper().getInventoryForInputOutput();
	}

	public void openMenu(ServerPlayer player, int contraptionEntityId, BlockPos localPos) {
		NetworkHooks.openScreen(player,
				new SimpleMenuProvider((w, p, pl) -> createMenu(w, pl, contraptionEntityId, localPos), getStorageStack().getHoverName()), buffer -> {
					buffer.writeInt(contraptionEntityId);
					buffer.writeBlockPos(localPos);
				});
	}

	public void setShouldBeOpen(boolean b) {
		storageHolder.setShouldBeOpen(b);
	}
}
