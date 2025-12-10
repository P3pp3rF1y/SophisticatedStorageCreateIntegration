package net.p3pp3rf1y.sophisticatedstoragecreateintegration.init;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.api.schematic.nbt.SafeNbtWriterRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.SophisticatedStorageCreateIntegration;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedLimitedBarrelContainerMenu;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedLimitedBarrelSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedStorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedStorageSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.schematic.SophisticatedStorageSafeNbtWriter;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage.MountedSophisticatedStorageType;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage.OpenMountedStorageInventoryPayload;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage.SophisticatedStorageMovementBehaviour;

import java.util.function.Supplier;

public class ModContent {
	private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, SophisticatedStorageCreateIntegration.MOD_ID);

	public static final DeferredRegister<MountedItemStorageType<?>> ITEM_STORAGE_TYPES = DeferredRegister.create(CreateBuiltInRegistries.MOUNTED_ITEM_STORAGE_TYPE, SophisticatedStorage.MOD_ID);

	public static final DeferredHolder<MountedItemStorageType<?>, MountedSophisticatedStorageType> SOPHISTICATED_MOUNTED_STORAGE_TYPE = ITEM_STORAGE_TYPES.register("sophisticated_storage", MountedSophisticatedStorageType::new);

	public static final Supplier<MenuType<MountedStorageContainerMenu>> MOUNTED_STORAGE_CONTAINER_TYPE = MENU_TYPES.register("mounted_storage",
			() -> IMenuTypeExtension.create(MountedStorageContainerMenu::fromBuffer));

	public static final Supplier<MenuType<MountedStorageSettingsContainerMenu>> MOUNTED_STORAGE_SETTINGS_CONTAINER_TYPE = MENU_TYPES.register("mounted_storage_settings",
			() -> IMenuTypeExtension.create(MountedStorageSettingsContainerMenu::fromBuffer));

	public static final Supplier<MenuType<MountedLimitedBarrelContainerMenu>> MOUNTED_LIMITED_BARREL_CONTAINER_TYPE = MENU_TYPES.register("mounted_limited_barrel",
			() -> IMenuTypeExtension.create(MountedLimitedBarrelContainerMenu::fromBuffer));

	public static final Supplier<MenuType<MountedLimitedBarrelSettingsContainerMenu>> MOUNTED_LIMITED_BARREL_SETTINGS_CONTAINER_TYPE = MENU_TYPES.register("mounted_limited_barrel_settings",
			() -> IMenuTypeExtension.create(MountedLimitedBarrelSettingsContainerMenu::fromBuffer));

	public static void registerHandler(IEventBus modBus) {
		ITEM_STORAGE_TYPES.register(modBus);
		MENU_TYPES.register(modBus);

		if (FMLEnvironment.getDist() == Dist.CLIENT) {
			ModContentClient.registerHandlers(modBus);
		}

		modBus.addListener(ModContent::onModSetup);
		modBus.addListener(ModContent::registerPayloads);
	}

	private static void onModSetup(FMLCommonSetupEvent event) {
		BuiltInRegistries.BLOCK.stream().filter(block -> block instanceof StorageBlockBase)
				.forEach(block -> {
					MountedItemStorageType.REGISTRY.register(block, SOPHISTICATED_MOUNTED_STORAGE_TYPE.get());
					MovementBehaviour.REGISTRY.register(block, SophisticatedStorageMovementBehaviour.INSTANCE);
				});
		SafeNbtWriterRegistry.REGISTRY.register(ModBlocks.CHEST_BLOCK_ENTITY_TYPE.get(), SophisticatedStorageSafeNbtWriter.Wooden.INSTANCE);
		SafeNbtWriterRegistry.REGISTRY.register(ModBlocks.BARREL_BLOCK_ENTITY_TYPE.get(), SophisticatedStorageSafeNbtWriter.Wooden.INSTANCE);
		SafeNbtWriterRegistry.REGISTRY.register(ModBlocks.LIMITED_BARREL_BLOCK_ENTITY_TYPE.get(), SophisticatedStorageSafeNbtWriter.Wooden.INSTANCE);
		SafeNbtWriterRegistry.REGISTRY.register(ModBlocks.SHULKER_BOX_BLOCK_ENTITY_TYPE.get(), SophisticatedStorageSafeNbtWriter.INSTANCE);
	}

	private static void registerPayloads(final RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar(SophisticatedStorageCreateIntegration.MOD_ID).versioned("1.0");
		registrar.playToServer(OpenMountedStorageInventoryPayload.TYPE, OpenMountedStorageInventoryPayload.STREAM_CODEC, OpenMountedStorageInventoryPayload::handlePayload);
	}
}
