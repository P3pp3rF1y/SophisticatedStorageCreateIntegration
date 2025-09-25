package net.p3pp3rf1y.sophisticatedstoragecreateintegration.init;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.p3pp3rf1y.sophisticatedcore.compat.trashslot.TrashSlotScreenRegistry;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.client.MountedLimitedBarrelScreen;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.client.MountedLimitedBarrelSettingsScreen;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.client.MountedStorageScreen;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.client.MountedStorageSettingsScreen;

public class ModContentClient {
	public static void registerHandlers(IEventBus modBus) {
		modBus.addListener(ModContentClient::onMenuScreenRegister);
	}

	private static void onMenuScreenRegister(RegisterEvent event) {
		if (!event.getRegistryKey().equals(ForgeRegistries.Keys.MENU_TYPES)) {
			return;
		}

		MenuScreens.register(ModContent.MOUNTED_STORAGE_CONTAINER_TYPE.get(), MountedStorageScreen::constructScreen);
		MenuScreens.register(ModContent.MOUNTED_STORAGE_SETTINGS_CONTAINER_TYPE.get(), MountedStorageSettingsScreen::constructScreen);
		MenuScreens.register(ModContent.MOUNTED_LIMITED_BARREL_CONTAINER_TYPE.get(), MountedLimitedBarrelScreen::new);
		MenuScreens.register(ModContent.MOUNTED_LIMITED_BARREL_SETTINGS_CONTAINER_TYPE.get(), MountedLimitedBarrelSettingsScreen::new);

		TrashSlotScreenRegistry.registerScreen(MountedStorageScreen.class);
	}
}
