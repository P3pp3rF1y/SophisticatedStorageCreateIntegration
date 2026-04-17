package net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage;

import com.simibubi.create.content.contraptions.StructureTransform;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.p3pp3rf1y.sophisticatedstorage.block.ChestBlock;

public final class SophisticatedChestBlockTransformer {
	private SophisticatedChestBlockTransformer() {
	}

	public static BlockState transform(BlockState state, StructureTransform transform) {
		if (transform.mirror != null) {
			state = mirror(state, transform.mirror);
		}

		if (transform.rotationAxis == Direction.Axis.Y && transform.rotation != null) {
			state = rotate(state, transform.rotation);
		}

		return state;
	}

	private static BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(ChestBlock.FACING, rotation.rotate(state.getValue(ChestBlock.FACING)));
	}

	private static BlockState mirror(BlockState state, Mirror mirror) {
		BlockState mirroredState = rotate(state, mirror.getRotation(state.getValue(ChestBlock.FACING)));
		ChestType chestType = mirroredState.getValue(ChestBlock.TYPE);
		return chestType == ChestType.SINGLE ? mirroredState : mirroredState.setValue(ChestBlock.TYPE, chestType.getOpposite());
	}
}
