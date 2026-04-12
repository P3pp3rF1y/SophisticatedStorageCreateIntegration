package net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SophisticatedMenuProvider;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageBase;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageData;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedcore.util.ValueIOHelper;
import net.p3pp3rf1y.sophisticatedcore.util.WorldHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.*;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.entity.StorageHolderTierUpgradeHandler;
import net.p3pp3rf1y.sophisticatedstorage.entity.StorageHolderToolHandler;
import net.p3pp3rf1y.sophisticatedstorage.init.ModDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.*;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedLimitedBarrelContainerMenu;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedStorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.init.ModContent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Supplier;

public class MountedSophisticatedStorage extends MountedStorageBase {
	public static final MapCodec<MountedSophisticatedStorage> CODEC = ItemStack.OPTIONAL_CODEC.xmap(
			MountedSophisticatedStorage::new, MountedSophisticatedStorage::getStorageStack
	).fieldOf("value");
	protected static final Multimap<Class<? extends Item>, NbtToComponentMapper<?>> NBT_TO_COMPONENT_MAPPERS = LinkedListMultimap.create();

	public static void registerNbtToComponentMapper(Class<? extends Item> itemClass, NbtToComponentMapper<?> mapper) {
		NBT_TO_COMPONENT_MAPPERS.put(itemClass, mapper);
	}

	static {
		RegistryOps<JsonElement> jsonOps = RegistryAccess.EMPTY.createSerializationContext(JsonOps.INSTANCE);

		registerNbtToComponentMapper(StorageBlockItem.class, new NbtToComponentMapper<>("displayName", () -> DataComponents.CUSTOM_NAME,
				(tag, key, level) -> ComponentSerialization.CODEC.decode(jsonOps, JsonParser.parseString(tag.getStringOr(key, ""))).result().map(Pair::getFirst),
				(tag, key, value, level) -> tag.putString(key, ComponentSerialization.CODEC.encodeStart(jsonOps, value).getOrThrow().toString()))
		);
		registerNbtToComponentMapper(StorageBlockItem.class, new NbtToComponentMapper<>("locked", ModDataComponents.LOCKED, CompoundTag::getBoolean, CompoundTag::putBoolean));
		registerNbtToComponentMapper(StorageBlockItem.class, new NbtToComponentMapper<>("showLock", ModDataComponents.LOCK_VISIBLE, CompoundTag::getBoolean, CompoundTag::putBoolean));
		registerNbtToComponentMapper(StorageBlockItem.class, new NbtToComponentMapper<>("showTier", ModDataComponents.TIER_VISIBLE, CompoundTag::getBoolean, CompoundTag::putBoolean));
		registerNbtToComponentMapper(StorageBlockItem.class, new NbtToComponentMapper<>("showUpgrades", ModDataComponents.UPGRADES_VISIBLE, CompoundTag::getBoolean, CompoundTag::putBoolean));
		registerNbtToComponentMapper(BarrelBlockItem.class, new NbtToComponentMapper<>("showCounts", ModDataComponents.COUNTS_VISIBLE, CompoundTag::getBoolean, CompoundTag::putBoolean));
		registerNbtToComponentMapper(BarrelBlockItem.class, new NbtToComponentMapper<>("showFillLevels", ModDataComponents.FILL_LEVELS_VISIBLE, CompoundTag::getBoolean, CompoundTag::putBoolean));
		registerNbtToComponentMapper(BarrelBlockItem.class, new NbtToComponentMapper<>("slotColors", ModDataComponents.SLOT_COLORS,
				(tag, key) -> NBTHelper.getMap(tag, "slotColors", Integer::valueOf, (tagName, t) -> t.asInt().map(DyeColor::byId)),
				(tag, key, value) -> NBTHelper.putMap(tag, key, value, String::valueOf, color -> IntTag.valueOf(color.getId()))));
		registerNbtToComponentMapper(WoodStorageBlockItem.class, new NbtToComponentMapper<>("woodType", ModDataComponents.WOOD_TYPE,
				(tag, key) -> WoodType.values().filter(wt -> wt.name().equals(tag.getStringOr(key, ""))).findFirst(),
				(tag, key, value) -> tag.putString(key, value.name())));
		registerNbtToComponentMapper(WoodStorageBlockItem.class, new NbtToComponentMapper<>(WoodStorageBlockEntity.PACKED, ModDataComponents.PACKED, CompoundTag::getBoolean, CompoundTag::putBoolean));
		registerNbtToComponentMapper(BarrelBlockItem.class, new NbtToComponentMapper<>(BarrelBlockEntity.MATERIALS, ModDataComponents.BARREL_MATERIALS,
				(tag, key, level) -> NBTHelper.getMap(tag, key, BarrelMaterial::fromName, (bm, t) -> t.asString().map(ResourceLocation::parse)),
				(tag, key, value, level) -> NBTHelper.putMap(tag, key, value, BarrelMaterial::getSerializedName, resourceLocation -> StringTag.valueOf(resourceLocation.toString()))));
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

		storage.getStorageWrapper().getSettingsHandler().getTypeCategory(ItemDisplaySettingsCategory.class).itemsChanged(); //update slot counts and fill levels

		CompoundTag fullBeNbt = storage.saveWithoutMetadata(level.registryAccess());

		CompoundTag contentsNbt = new CompoundTag();
		CompoundTag storageWrapperNbt = fullBeNbt.getCompoundOrEmpty(StorageBlockEntity.STORAGE_WRAPPER);
		contentsNbt.put(StorageWrapper.CONTENTS_TAG, storageWrapperNbt.getCompoundOrEmpty(StorageWrapper.CONTENTS_TAG));
		contentsNbt.put(StorageWrapper.SETTINGS_TAG, storageWrapperNbt.getCompoundOrEmpty(StorageWrapper.SETTINGS_TAG));
		storageItem.set(ModCoreDataComponents.RENDER_INFO_TAG, storageWrapperNbt.getCompound(StorageWrapper.RENDER_INFO_TAG).map(CustomData::of).orElse(CustomData.EMPTY));
		storageItem.set(ModCoreDataComponents.SORT_BY, NBTHelper.getString(storageWrapperNbt, StorageWrapper.SORT_BY).map(SortBy::fromName).orElse(SortBy.NAME));
		storageItem.set(ModCoreDataComponents.NUMBER_OF_INVENTORY_SLOTS, storageWrapperNbt.getIntOr(StorageWrapper.NUMBER_OF_INVENTORY_SLOTS, 0));
		storageItem.set(ModCoreDataComponents.NUMBER_OF_UPGRADE_SLOTS, storageWrapperNbt.getIntOr(StorageWrapper.NUMBER_OF_UPGRADE_SLOTS, 0));

		if (!rightChestPart) {
			UUID id = UUID.randomUUID();
			storageItem.set(ModCoreDataComponents.STORAGE_UUID, id);
			MountedStorageData.get().setContents(id, contentsNbt);
			StorageBlockItem.setNumberOfInventorySlots(storageItem, storageWrapper.getInventoryHandler().getSlots());
			StorageBlockItem.setNumberOfUpgradeSlots(storageItem, storageWrapper.getUpgradeHandler().getSlots());
		}

		for (var entry : NBT_TO_COMPONENT_MAPPERS.entries()) {
			if (entry.getKey().isInstance(storageItem.getItem())) {
				NbtToComponentMapper<?> mapper = entry.getValue();
				if (fullBeNbt.contains(mapper.tagName)) {
					setComponentValue(level, mapper, storageItem, fullBeNbt);
				}
			}
		}

		if (storage instanceof ChestBlockEntity chestBlock) {
			BlockState blockState = chestBlock.getBlockState();
			if (blockState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
				ChestBlockItem.setDoubleChest(storageItem, true);
				chestBlock.removeDoubleMainPos();
			}
		}

		MountedSophisticatedStorage mountedStorage = new MountedSophisticatedStorage(storageItem);
		if (!level.isClientSide()) {
			mountedStorage.getStorageHolder().setDirty();
		}
		return mountedStorage;
	}

	private static <T> void setComponentValue(Level level, NbtToComponentMapper<T> mapper, ItemStack storageItem, CompoundTag fullBeNbt) {
		mapper.nbtValueGetter.get(fullBeNbt, mapper.tagName, level).ifPresent(value -> storageItem.set(mapper.type.get(), value));
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
		if (getStorageStack().has(ModCoreDataComponents.STORAGE_UUID) && be instanceof StorageBlockEntity storageBe) {
			UUID storageId = getStorageStack().get(ModCoreDataComponents.STORAGE_UUID);

			if (storageId == null) {
				return;
			}

			MountedStorageData mountedStorageData = MountedStorageData.get();
			CompoundTag fullBeNbt = new CompoundTag();

			CompoundTag contentNbt = mountedStorageData.getContents(storageId);
			contentNbt.put(StorageWrapper.RENDER_INFO_TAG, getStorageStack().getOrDefault(ModCoreDataComponents.RENDER_INFO_TAG, CustomData.EMPTY).copyTag());
			SortBy sortBy = getStorageStack().get(ModCoreDataComponents.SORT_BY);
			if (sortBy != null) {
				contentNbt.putString(StorageWrapper.SORT_BY, sortBy.getSerializedName());
			}
			Integer numberOfInventorySlots = getStorageStack().get(ModCoreDataComponents.NUMBER_OF_INVENTORY_SLOTS);
			if (numberOfInventorySlots != null) {
				contentNbt.putInt(StorageWrapper.NUMBER_OF_INVENTORY_SLOTS, numberOfInventorySlots);
			}
			Integer numberOfUpgradeSlots = getStorageStack().get(ModCoreDataComponents.NUMBER_OF_UPGRADE_SLOTS);
			if (numberOfUpgradeSlots != null) {
				contentNbt.putInt(StorageWrapper.NUMBER_OF_UPGRADE_SLOTS, numberOfUpgradeSlots);
			}
			fullBeNbt.put(StorageBlockEntity.STORAGE_WRAPPER, contentNbt);

			for (Map.Entry<Class<? extends Item>, NbtToComponentMapper<?>> entry : NBT_TO_COMPONENT_MAPPERS.entries()) {
				if (entry.getKey().isInstance(getStorageStack().getItem())) {
					setNbtValueFromComponent(fullBeNbt, entry.getValue(), getStorageStack(), level);
				}
			}

			if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) { //prevent double chest update shape changes dropping items before both parts loaded
				storageBe.setBeingUpgraded(true);
			}
			storageBe.loadAdditional(ValueIOHelper.inputFromCompoundTag(level.registryAccess(), fullBeNbt));
			if (getStorageStack().getItem() instanceof ITintableBlockItem tintableBlockItem) {
				storageBe.getStorageWrapper().setColors(tintableBlockItem.getMainColor(getStorageStack()).orElse(-1), tintableBlockItem.getAccentColor(getStorageStack()).orElse(-1));
			}
			mountedStorageData.removeStorageContents(storageId);

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
					attachedBe.setMainPos(mainBe.getBlockPos());

					mainBe.getStorageWrapper().onInit(level);
					mainBe.tryToAddToController();
				});
			} else {
				storageBe.getStorageWrapper().onInit(level);
				storageBe.tryToAddToController();
			}
		}
	}

	private <T> void setNbtValueFromComponent(CompoundTag fullBeNbt, NbtToComponentMapper<T> mapper, ItemStack storageItem, Level level) {
		if (storageItem.has(mapper.type.get())) {
			T value = storageItem.get(mapper.type.get());
			if (value != null) {
				mapper.nbtValueSetter.set(fullBeNbt, mapper.tagName, value, level);
			}
		} else {
			fullBeNbt.remove(mapper.tagName);
		}
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

		ServerLevel level = player.level();

		BlockPos localPos = info.pos();
		if (info.state().getBlock() instanceof ChestBlock && info.state().getValue(ChestBlock.TYPE) == ChestType.LEFT) {
			localPos = info.pos().relative(ChestBlock.getConnectedDirection(info.state()));
		}

		int contraptionEntityId = contraption.entity.getId();
		ItemStack itemInHand = player.getMainHandItem();
		if (itemInHand.getItem() instanceof StorageTierUpgradeItem tierUpgradeItem) {
			InteractionResult result = tryStorageTierUpgrade(player, itemInHand, tierUpgradeItem);
			if (result != InteractionResult.PASS) {
				return result.consumesAction();
			}
		} else if (itemInHand.getItem() instanceof StorageToolItem && tryToolInteraction(itemInHand)) {
			return true;
		} else if (itemInHand.getItem() instanceof UpgradeItemBase<?> && tryAddStorageUpgrade(player, itemInHand)) {
			return true;
		} else if (itemInHand.getItem() == ModItems.PAINTBRUSH.get() && tryPaintStorage(player, itemInHand)) {
			return true;
		}

		Vec3 localPosVec = Vec3.atCenterOf(localPos);

		OptionalInt id = openMenu(player, contraptionEntityId, localPos);
		if (id.isPresent()) {
			Vec3 globalPos = contraption.entity.toGlobalVector(localPosVec, 0);
			onOpen(level, globalPos);
			return true;
		} else {
			return false;
		}
	}

	private boolean tryToolInteraction(ItemStack itemInHand) {
		boolean result = StorageHolderToolHandler.tryStorageToolInteract(itemInHand, getStorageHolder()).consumesAction();
		if (result && StorageToolItem.getMode(itemInHand) == StorageToolItem.Mode.LOCK) {
			storageHolder.updateClientBlockRenderAfterNextSync();
			storageHolder.sendStorageUpdatePayload();
		}
		return result;
	}

	private boolean tryAddStorageUpgrade(Player player, ItemStack itemInHand) {
		return StorageBlockBase.tryAddSingleUpgrade(player, itemInHand, getStorageWrapper()).consumesAction();
	}

	private boolean tryPaintStorage(Player player, ItemStack paintbrush) {
		if (!(getStorageStack().getItem() instanceof BlockItem blockItem)) {
			return false;
		}
		BlockState state = blockItem.getBlock().defaultBlockState();
		SoundEvent placeSound = state.getSoundType().getPlaceSound();
		boolean painted = PaintbrushItem.paint(player, paintbrush, getStorageHolder(), getStorageWrapper(), getStorageHolder().getPosition(), Direction.UP, placeSound);
		if (painted) {
			storageHolder.updateClientBlockRenderAfterNextSync();
			storageHolder.sendStorageUpdatePayload();
		}
		return painted;
	}

	private InteractionResult tryStorageTierUpgrade(ServerPlayer player, ItemStack itemInHand, StorageTierUpgradeItem tierUpgradeItem) {
		InteractionResult result = StorageHolderTierUpgradeHandler.upgrade(player, getStorageHolder(), itemInHand, tierUpgradeItem);

		if (result.consumesAction()) {
			if (getStorageStack().getItem() instanceof ChestBlockItem && ChestBlockItem.isDoubleChest(getStorageStack())) {
				if (storageHolder.getMainStorageHolder() instanceof MountedStorageHolder mainStorageHolder) {
					mainStorageHolder.updateState();
				}
				storageHolder.getAuxiliaryStorageHolder().filter(MountedStorageHolder.class::isInstance)
						.map(MountedStorageHolder.class::cast)
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
		if (getStorageStack().has(ModCoreDataComponents.STORAGE_UUID)) {
			MountedStorageData.get().removeStorageContents(getStorageStack().get(ModCoreDataComponents.STORAGE_UUID));
		}
	}

	@Override
	protected IItemHandlerModifiable getExternalItemHandler() {
		return getStorageHolder().getMainStorageWrapper().getInventoryForInputOutput();
	}

	public OptionalInt openMenu(ServerPlayer player, int contraptionEntityId, BlockPos localPos) {
		return player.openMenu(new SophisticatedMenuProvider((w, p, pl) -> createMenu(w, pl, contraptionEntityId, localPos), getStorageStack().getHoverName(), false),
				buffer -> {
					buffer.writeInt(contraptionEntityId);
					buffer.writeBlockPos(localPos);
				});
	}

	public void setShouldBeOpen(boolean b) {
		storageHolder.setShouldBeOpen(b);
	}

	public record NbtToComponentMapper<T>(String tagName, Supplier<DataComponentType<T>> type,
										  NbtLevelAwareGetter<T> nbtValueGetter,
										  NbtLevelAwareSetter<T> nbtValueSetter) {
		public NbtToComponentMapper(String tagName, Supplier<DataComponentType<T>> type, NbtGetter<T> nbtValueGetter, NbtSetter<T> nbtValueSetter) {
			this(tagName, type, (tag, key, level) -> nbtValueGetter.get(tag, key), (tag, key, value, level) -> nbtValueSetter.set(tag, key, value));
		}

		public interface NbtGetter<T> {
			Optional<T> get(CompoundTag tag, String key);
		}

		public interface NbtLevelAwareGetter<T> {
			Optional<T> get(CompoundTag tag, String key, Level level);
		}

		public interface NbtSetter<T> {
			void set(CompoundTag tag, String key, T value);
		}

		public interface NbtLevelAwareSetter<T> {
			void set(CompoundTag tag, String key, T value, Level level);
		}
	}
}
