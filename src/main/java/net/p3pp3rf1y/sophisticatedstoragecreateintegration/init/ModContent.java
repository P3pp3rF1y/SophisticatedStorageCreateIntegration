package net.p3pp3rf1y.sophisticatedstoragecreateintegration.init;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.transformable.MovedBlockTransformerRegistries;
import com.simibubi.create.api.schematic.nbt.SafeNbtWriterRegistry;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.network.StoragePacketHandler;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.SophisticatedStorageCreateIntegration;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedLimitedBarrelContainerMenu;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedLimitedBarrelSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedStorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedStorageSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.schematic.SophisticatedStorageSafeNbtWriter;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage.MountedSophisticatedStorageType;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage.OpenMountedStorageInventoryMessage;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage.SophisticatedChestBlockTransformer;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage.SophisticatedStorageMovementBehaviour;

import java.util.function.Supplier;

public class ModContent {
	private static final CreateRegistrate REGISTRATE = CreateRegistrate.create(SophisticatedStorageCreateIntegration.MOD_ID)
			.defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
			.setTooltipModifierFactory(item ->
					new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
							.andThen(TooltipModifier.mapNull(KineticStats.create(item)))
			);

	private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, SophisticatedStorageCreateIntegration.MOD_ID);

	public static final RegistryEntry<MountedSophisticatedStorageType> SOPHISTICATED_MOUNTED_STORAGE_TYPE = REGISTRATE.mountedItemStorage("sophisticated_storage", MountedSophisticatedStorageType::new).register();

	public static final Supplier<MenuType<MountedStorageContainerMenu>> MOUNTED_STORAGE_CONTAINER_TYPE = MENU_TYPES.register("mounted_storage",
			() -> IForgeMenuType.create(MountedStorageContainerMenu::fromBuffer));

	public static final Supplier<MenuType<MountedStorageSettingsContainerMenu>> MOUNTED_STORAGE_SETTINGS_CONTAINER_TYPE = MENU_TYPES.register("mounted_storage_settings",
			() -> IForgeMenuType.create(MountedStorageSettingsContainerMenu::fromBuffer));

	public static final Supplier<MenuType<MountedLimitedBarrelContainerMenu>> MOUNTED_LIMITED_BARREL_CONTAINER_TYPE = MENU_TYPES.register("mounted_limited_barrel",
			() -> IForgeMenuType.create(MountedLimitedBarrelContainerMenu::fromBuffer));

	public static final Supplier<MenuType<MountedLimitedBarrelSettingsContainerMenu>> MOUNTED_LIMITED_BARREL_SETTINGS_CONTAINER_TYPE = MENU_TYPES.register("mounted_limited_barrel_settings",
			() -> IForgeMenuType.create(MountedLimitedBarrelSettingsContainerMenu::fromBuffer));

	public static void registerHandler(IEventBus modBus) {
		REGISTRATE.registerEventListeners(modBus);
		MENU_TYPES.register(modBus);

		if (FMLEnvironment.dist == Dist.CLIENT) {
			ModContentClient.registerHandlers(modBus);
		}

		modBus.addListener(ModContent::onModSetup);
	}

	private static void onModSetup(FMLCommonSetupEvent event) {
		BuiltInRegistries.BLOCK.stream().filter(block -> block instanceof StorageBlockBase)
				.forEach(block -> {
					MountedItemStorageType.REGISTRY.register(block, SOPHISTICATED_MOUNTED_STORAGE_TYPE.get());
					MovementBehaviour.REGISTRY.register(block, SophisticatedStorageMovementBehaviour.INSTANCE);
				});
		MovedBlockTransformerRegistries.BLOCK_TRANSFORMERS.register(ModBlocks.CHEST.get(), SophisticatedChestBlockTransformer::transform);
		MovedBlockTransformerRegistries.BLOCK_TRANSFORMERS.register(ModBlocks.COPPER_CHEST.get(), SophisticatedChestBlockTransformer::transform);
		MovedBlockTransformerRegistries.BLOCK_TRANSFORMERS.register(ModBlocks.IRON_CHEST.get(), SophisticatedChestBlockTransformer::transform);
		MovedBlockTransformerRegistries.BLOCK_TRANSFORMERS.register(ModBlocks.GOLD_CHEST.get(), SophisticatedChestBlockTransformer::transform);
		MovedBlockTransformerRegistries.BLOCK_TRANSFORMERS.register(ModBlocks.DIAMOND_CHEST.get(), SophisticatedChestBlockTransformer::transform);
		MovedBlockTransformerRegistries.BLOCK_TRANSFORMERS.register(ModBlocks.NETHERITE_CHEST.get(), SophisticatedChestBlockTransformer::transform);
		SafeNbtWriterRegistry.REGISTRY.register(ModBlocks.CHEST_BLOCK_ENTITY_TYPE.get(), SophisticatedStorageSafeNbtWriter.Wooden.INSTANCE);
		SafeNbtWriterRegistry.REGISTRY.register(ModBlocks.BARREL_BLOCK_ENTITY_TYPE.get(), SophisticatedStorageSafeNbtWriter.Wooden.INSTANCE);
		SafeNbtWriterRegistry.REGISTRY.register(ModBlocks.LIMITED_BARREL_BLOCK_ENTITY_TYPE.get(), SophisticatedStorageSafeNbtWriter.Wooden.INSTANCE);
		SafeNbtWriterRegistry.REGISTRY.register(ModBlocks.SHULKER_BOX_BLOCK_ENTITY_TYPE.get(), SophisticatedStorageSafeNbtWriter.INSTANCE);
		event.enqueueWork(() -> {
			StoragePacketHandler.INSTANCE.registerMessage(OpenMountedStorageInventoryMessage.class, OpenMountedStorageInventoryMessage::encode, OpenMountedStorageInventoryMessage::decode, OpenMountedStorageInventoryMessage::onMessage);
		});
	}
}
