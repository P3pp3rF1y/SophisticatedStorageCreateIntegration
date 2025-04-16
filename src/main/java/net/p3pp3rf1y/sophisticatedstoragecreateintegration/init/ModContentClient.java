package net.p3pp3rf1y.sophisticatedstoragecreateintegration.init;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.p3pp3rf1y.sophisticatedcore.compat.trashslot.TrashSlotScreenRegistry;
import net.p3pp3rf1y.sophisticatedstorage.client.ClientEventHandler;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.client.MountedLimitedBarrelScreen;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.client.MountedLimitedBarrelSettingsScreen;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.client.MountedStorageScreen;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.client.MountedStorageSettingsScreen;

public class ModContentClient {
	public static void registerHandlers(IEventBus modBus) {
		ClientEventHandler.addSortScreenMatcher(screen -> screen instanceof MountedStorageScreen);

		modBus.addListener(ModContentClient::onMenuScreenRegister);
	}

	private static void onMenuScreenRegister(RegisterMenuScreensEvent event) {
		event.register(ModContent.MOUNTED_STORAGE_CONTAINER_TYPE.get(), MountedStorageScreen::constructScreen);
		event.register(ModContent.MOUNTED_STORAGE_SETTINGS_CONTAINER_TYPE.get(), MountedStorageSettingsScreen::constructScreen);
		event.register(ModContent.MOUNTED_LIMITED_BARREL_CONTAINER_TYPE.get(), MountedLimitedBarrelScreen::new);
		event.register(ModContent.MOUNTED_LIMITED_BARREL_SETTINGS_CONTAINER_TYPE.get(), MountedLimitedBarrelSettingsScreen::new);

		TrashSlotScreenRegistry.registerScreen(MountedStorageScreen.class);
	}
}
