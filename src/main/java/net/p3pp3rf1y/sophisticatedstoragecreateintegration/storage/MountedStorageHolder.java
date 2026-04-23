package net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageSavedData;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SophisticatedMenuProvider;
import net.p3pp3rf1y.sophisticatedcore.compat.create.ContraptionHelper;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageData;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageUpdatePayload;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.block.ChestBlock;
import net.p3pp3rf1y.sophisticatedstorage.block.ChestBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.entity.StorageHolderBase;
import net.p3pp3rf1y.sophisticatedstorage.init.ModDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.upgrades.hopper.HopperUpgradeItem;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedLimitedBarrelContainerMenu;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedStorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.network.MountedStorageOpennessPayload;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MountedStorageHolder extends StorageHolderBase {
	private final Consumer<ItemStack> storageStackSetter;
	private final Supplier<ItemStack> storageStackGetter;
	@Nullable
	private WeakReference<Entity> contraptionEntityRef = null;
	private BlockPos localPos = BlockPos.ZERO;
	private BlockPos chestOtherPartPos = BlockPos.ZERO;
	private Vec3 position = Vec3.ZERO;
	@Nullable
	private WeakReference<Level> level = null;
	private boolean clearedNbt = false;
	@Nullable
	private WeakReference<IStorageWrapper> otherHalfStorageWrapper = null;
	private boolean refreshClientBlockRender = false;
	private boolean dirty = false;
	private boolean refreshRendersOnNextTick = false;

	public MountedStorageHolder(Supplier<ItemStack> storageStackGetter, Consumer<ItemStack> storageStackSetter) {
		super(false);
		this.storageStackGetter = storageStackGetter;
		this.storageStackSetter = storageStackSetter;
		updateRenderAttributes = true;
	}

	public void setLocalPos(BlockPos localPos) {
		this.localPos = localPos;
	}

	@Override
	public void setStorageItem(ItemStack storageItem) {
		super.setStorageItem(storageItem);
		dirty = true;
	}

	@Override
	protected boolean isOwnContainer(Player player) {
		if (player.containerMenu instanceof MountedStorageContainerMenu mountedStorageContainerMenu) {
			return mountedStorageContainerMenu.getContraptionEntity().map(c -> c == getEntity() &&
					(mountedStorageContainerMenu.getLocalPos().equals(localPos) || mountedStorageContainerMenu.getLocalPos().equals(chestOtherPartPos))
			).orElse(false);
		}
		return false;
	}

	@Override
	protected IStorageSavedData getStorageData() {
		return MountedStorageData.get();
	}

	@Override
	protected void setSyncedStorageStack(ItemStack storageStack) {
		storageStackSetter.accept(storageStack);
	}

	@Override
	protected ItemStack getSyncedStorageStack() {
		return storageStackGetter.get();
	}

	@Override
	protected boolean isLocked(ItemStack storageItem) {
		return storageItem.getOrDefault(ModDataComponents.LOCKED, false);
	}

	@Nullable
	@Override
	protected Level getLevel() {
		return level == null ? null : level.get();
	}

	public void setLevel(Level level) {
		this.level = new WeakReference<>(level);
		if (!level.isClientSide()) {
			getStorageWrapper().getRenderDataHandler().setDisplayItemsChangeListener(ri -> updateClientBlockRenderAfterNextSync());
		}
	}

	public void updateClientBlockRenderAfterNextSync() {
		refreshClientBlockRender = true;
	}

	@Override
	public void tick(Entity entity) {
		Level level = getLevel();
		if (level instanceof ServerLevel) {
			sendStorageUpdatePayload();
		} else if (refreshRendersOnNextTick && level != null && level.isClientSide() && getRenderBlockEntity() != null && entity instanceof AbstractContraptionEntity contraptionEntity) {
			refreshRenders(contraptionEntity, true);
			refreshRendersOnNextTick = false;
		}
		super.tick(entity);
	}

	public void sendStorageUpdatePayload() {
		if (!dirty) {
			return;
		}
		dirty = false;

		Entity entity = getEntity();
		if (entity == null || entity.level().isClientSide()) {
			return;
		}
		PacketDistributor.sendToPlayersTrackingEntity(entity, new MountedStorageUpdatePayload(entity.getId(), localPos, getSyncedStorageStack(), refreshClientBlockRender && isBarrel()));
		refreshClientBlockRender = false;
	}

	@Override
	protected Vec3 getPosition() {
		return position;
	}

	public void setPosition(Vec3 position) {
		this.position = position;
	}

	@Nullable
	@Override
	protected Entity getEntity() {
		return contraptionEntityRef == null ? null : contraptionEntityRef.get();
	}

	public void setContraptionEntity(Entity entity) {
		this.contraptionEntityRef = new WeakReference<>(entity);
	}

	@Nullable
	@Override
	protected StorageBlockEntity retrieveRenderBlockEntity() {
		Entity e = getEntity();
		if (e instanceof AbstractContraptionEntity abstractContraptionEntity) {
			return (StorageBlockEntity) abstractContraptionEntity.getContraption().getBlockEntityClientSide(localPos);
		}

		return null;
	}

	@Override
	protected boolean isUpgradeRunnable(ItemStack upgrade) {
		return !(upgrade.getItem() instanceof HopperUpgradeItem);
	}

	@Override
	protected void refreshRenderBlockEntity() {
		StorageBlockEntity renderBlockEntity = retrieveRenderBlockEntity();
		if (renderBlockEntity != null) {
			updateRenderBlockEntityAttributes(getSyncedStorageStack(), renderBlockEntity);
		}
	}

	@Override
	protected void updateRenderBlockEntityAttributes(ItemStack storageItem, StorageBlockEntity renderBlockEntity) {
		if (updateRenderAttributes && getEntity() instanceof AbstractContraptionEntity contraptionEntity) {
			StructureTemplate.StructureBlockInfo blockInfo = contraptionEntity.getContraption().getBlocks().get(localPos);
			renderBlockEntity.setBlockState(blockInfo.state());
		}
		super.updateRenderBlockEntityAttributes(storageItem, renderBlockEntity);
	}

	@Override
	protected void setLocked(boolean locked) {
		getSyncedStorageStack().set(ModDataComponents.LOCKED, locked);
	}

	@Override
	protected AABB getPickupBoundingBox() {
		return new AABB(getPosition(), getPosition().add(1, 1, 1)).inflate(0.2);
	}

	@Override
	protected void openMenu(Player player) {
		@Nullable Entity e = getEntity();
		if (e == null) {
			return;
		}
		player.openMenu(new SophisticatedMenuProvider((w, p, pl) -> createMenu(w, pl, e.getId(), localPos), getSyncedStorageStack().getHoverName(), false),
				buffer -> {
					buffer.writeInt(e.getId());
					buffer.writeBlockPos(localPos);
				});
	}

	private MountedStorageContainerMenuBase createMenu(int id, Player pl, int contraptionEntityId, BlockPos localPos) {
		if (MovingStorageWrapper.isLimitedBarrel(getSyncedStorageStack())) {
			return new MountedLimitedBarrelContainerMenu(id, pl, contraptionEntityId, localPos);
		} else {
			return new MountedStorageContainerMenu(id, pl, contraptionEntityId, localPos);
		}
	}

	@Override
	protected void playSound(SoundEvent sound) {
		Level level = getLevel();
		if (level == null) {
			return;
		}

		Vec3 position = getPosition().add(0.5, 0.5, 0.5);

		level.playSound(null, position.x(), position.y(), position.z(), sound, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
	}

	void initEntityLevelAndPositions(MovementContext context) {
		if (getEntity() == null) {
			AbstractContraptionEntity entity = context.contraption.entity;
			BlockPos localPos = context.localPos;
			Vec3 position = context.position;
			Level level = context.world;
			initEntityLevelAndPositions(entity, localPos, level, position, context.state);
		}
	}

	public void initEntityLevelAndPositions(AbstractContraptionEntity abstractContraptionEntity, BlockPos localPos, Level level, Vec3 position, BlockState state) {
		setContraptionEntity(abstractContraptionEntity);
		setLocalPos(localPos);
		setLevel(level);
		setPosition(position);

		if (isChest() && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
			chestOtherPartPos = localPos.relative(ChestBlock.getConnectedDirection(state));
			isMainStorage = state.getValue(ChestBlock.TYPE) == ChestType.RIGHT;
		}
	}

	public void clearNbt() {
		if (!clearedNbt && getEntity() instanceof AbstractContraptionEntity abstractContraptionEntity) {
			abstractContraptionEntity.getContraption().getBlocks()
					.computeIfPresent(localPos, (p, blockInfo) -> new StructureTemplate.StructureBlockInfo(blockInfo.pos(), blockInfo.state(), null));
			clearedNbt = true;
		}
	}

	@Override
	public void startOpen(Player player, Entity entity) {
		super.startOpen(player, entity);
		if (isChest() && isMainStorage && chestOtherPartPos != BlockPos.ZERO) {
			getHolderOfOtherHalf(chestOtherPartPos).ifPresent(holder -> holder.startOpen(player, entity));
		}
	}

	@Nullable
	@Override
	protected CustomPacketPayload createOpennessPayload() {
		Entity entity = getEntity();
		if (entity == null) {
			return null;
		}

		return new MountedStorageOpennessPayload(getEntity().getId(), isDoubleChest() && !isMainStorage ? chestOtherPartPos : localPos, isOpen());
	}

	private Optional<StorageHolderBase> getHolderOfOtherHalf() {
		if (isChest() && chestOtherPartPos != BlockPos.ZERO) {
			return getHolderOfOtherHalf(chestOtherPartPos).map(MountedStorageHolder.class::cast);
		}
		return Optional.empty();
	}

	private Optional<MountedStorageHolder> getHolderOfOtherHalf(BlockPos otherHalfLocalPos) {
		Entity e = getEntity();
		if (e instanceof AbstractContraptionEntity abstractContraptionEntity
				&& ContraptionHelper.getMountedStorage(abstractContraptionEntity, otherHalfLocalPos) instanceof MountedSophisticatedStorage mountedSophisticatedStorage) {
			return Optional.of(mountedSophisticatedStorage.getStorageHolder());
		}
		return Optional.empty();
	}

	@Override
	public void stopOpen(Player player, Entity entity) {
		super.stopOpen(player, entity);
		if (isChest() && isMainStorage && chestOtherPartPos != BlockPos.ZERO) {
			getHolderOfOtherHalf(chestOtherPartPos).ifPresent(holder -> holder.stopOpen(player, entity));
		}
	}

	public IStorageWrapper getMainStorageWrapper() {
		if (isMainStorage) {
			return getStorageWrapper();
		}

		if (otherHalfStorageWrapper == null || otherHalfStorageWrapper.get() == null) {
			IStorageWrapper wrapper;
			if (getEntity() == null || getEntity().level().isClientSide()) {
				wrapper = NoopStorageWrapper.INSTANCE;
			} else {
				wrapper = getHolderOfOtherHalf(chestOtherPartPos).map(MountedStorageHolder::getStorageWrapper).orElse(NoopStorageWrapper.INSTANCE);
			}
			otherHalfStorageWrapper = new WeakReference<>(wrapper);
			return wrapper;
		}
		return otherHalfStorageWrapper.get();
	}

	public void updateState() {
		if (getEntity() instanceof AbstractContraptionEntity contraptionEntity && getSyncedStorageStack().getItem() instanceof StorageBlockItem storageBlockItem) {
			StructureTemplate.StructureBlockInfo blockInfo = contraptionEntity.getContraption().getBlocks().get(localPos);
			BlockState newBlockState = storageBlockItem.getBlock().defaultBlockState();
			for (var value : blockInfo.state().getValues().toList()) {
				newBlockState = setStateValue(newBlockState, value.property(), value.value());
			}
			contraptionEntity.setBlock(localPos, new StructureTemplate.StructureBlockInfo(blockInfo.pos(), newBlockState, blockInfo.nbt()));
		}
	}

	private <T extends Comparable<T>> BlockState setStateValue(BlockState state, Property<T> property, Object value) {
		return state.setValue(property, (T) value);
	}

	public void setDirty() {
		dirty = true;
	}

	@Override
	public Optional<StorageHolderBase> getAuxiliaryStorageHolder() {
		return isMainStorage ? getHolderOfOtherHalf() : Optional.of(this);
	}

	@Override
	public StorageHolderBase getMainStorageHolder() {
		return isMainStorage ? super.getMainStorageHolder() : getHolderOfOtherHalf().orElse(this);
	}

	public boolean isDoubleChest() {
		return isChest() && chestOtherPartPos != BlockPos.ZERO;
	}

	@Override
	public void setShouldBeOpen(boolean shouldBeOpen) {
		super.setShouldBeOpen(shouldBeOpen);
		if (isDoubleChest() && isMainStorage) {
			getHolderOfOtherHalf().ifPresent(holder -> holder.setShouldBeOpen(shouldBeOpen));
		}
	}

	public void refreshRenders(boolean refreshBlockRender) {
		if (getEntity() instanceof AbstractContraptionEntity contraptionEntity) {
			refreshRenders(contraptionEntity, refreshBlockRender);
		} else {
			refreshRendersOnNextTick = true;
		}
	}

	private void refreshRenders(AbstractContraptionEntity contraptionEntity, boolean refreshBlockRender) {
		refreshRenderBlockEntity();
		if (refreshBlockRender) {
			contraptionEntity.getContraption().invalidateClientContraptionStructure();
		}
	}
}
