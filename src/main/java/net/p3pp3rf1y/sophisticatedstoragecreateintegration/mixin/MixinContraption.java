package net.p3pp3rf1y.sophisticatedstoragecreateintegration.mixin;

import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage.MountedStorageReadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Contraption.class)
public class MixinContraption {
	@Inject(method = "readNBT", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/MountedStorageManager;read(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;ZLcom/simibubi/create/content/contraptions/Contraption;)V"))
	private void setMountedStorageReadLevel(Level level, CompoundTag nbt, boolean spawnData, CallbackInfo ci) {
		if (level instanceof ServerLevel serverLevel) {
			MountedStorageReadContext.setServerLevel(serverLevel);
		}
	}

	@Inject(method = "readNBT", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/MountedStorageManager;read(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;ZLcom/simibubi/create/content/contraptions/Contraption;)V", shift = At.Shift.AFTER))
	private void clearMountedStorageReadLevel(Level level, CompoundTag nbt, boolean spawnData, CallbackInfo ci) {
		if (level instanceof ServerLevel) {
			MountedStorageReadContext.clearServerLevel();
		}
	}
}
