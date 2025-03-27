package net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SophisticatedStorageMovementBehaviour implements MovementBehaviour {
	public static final SophisticatedStorageMovementBehaviour INSTANCE = new SophisticatedStorageMovementBehaviour();

	private SophisticatedStorageMovementBehaviour() {
	}

	@Override
	public void tick(MovementContext context) {
		@Nullable MountedSophisticatedStorage storage = getMountedSophisticatedStorage(context);
		if (storage != null) {
			storage.initEntityLevelAndPositions(context);
			storage.clearNbt();
			storage.tick(context.contraption.entity);
		}
	}

	//TODO replace with direct call to context.getItemStorage once this is fixed for when storage is synced and new instance is created on client
	@Nullable
	private MountedSophisticatedStorage getMountedSophisticatedStorage(MovementContext context) {
		if (context.contraption.getStorage().getAllItemStorages().get(context.localPos) instanceof MountedSophisticatedStorage mountedSophisticatedStorage) {
			return mountedSophisticatedStorage;
		}

		return null;
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		@Nullable MountedSophisticatedStorage storage = getMountedSophisticatedStorage(context);
		if (storage != null) {
			MountedStorageHolder storageHolder = storage.getStorageHolder();
			storageHolder.setPosition(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
		}
	}
}
