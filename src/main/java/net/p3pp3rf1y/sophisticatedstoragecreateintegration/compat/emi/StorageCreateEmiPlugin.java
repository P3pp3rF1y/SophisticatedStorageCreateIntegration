package net.p3pp3rf1y.sophisticatedstoragecreateintegration.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.emi.EmiGridMenuInfo;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.emi.EmiSettingsGhostDragDropHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.emi.EmiStorageGhostDragDropHandler;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.client.MountedStorageScreen;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.client.MountedStorageSettingsScreen;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.init.ModContent;

@SuppressWarnings("unused")
@EmiEntrypoint
public class StorageCreateEmiPlugin implements EmiPlugin {
	@Override
	public void register(EmiRegistry registry) {
		registerGuiHandlers(registry);
		registerRecipeHandlers(registry);
	}

	private void registerGuiHandlers(EmiRegistry registry) {
		registry.addExclusionArea(MountedStorageScreen.class, (screen, consumer) -> {
			// noinspection ConstantValue
			if (screen == null || screen.getUpgradeSettingsControl() == null) {
				return;
			}
			screen.getUpgradeSlotsRectangle().ifPresent(r -> consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
			screen.getUpgradeSettingsControl().getTabRectangles().forEach(r -> consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
			screen.getSortButtonsRectangle().ifPresent(r -> consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
		});
		registry.addExclusionArea(MountedStorageSettingsScreen.class, (screen, consumer) -> {
			if (screen == null) { // Due to how Emi collects the exclusion area this can be null
				return;
			}
			screen.getExtendedControlsRectangles().forEach(r -> consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
		});

		registry.addDragDropHandler(MountedStorageScreen.class, new EmiStorageGhostDragDropHandler<>());
		registry.addDragDropHandler(MountedStorageSettingsScreen.class, new EmiSettingsGhostDragDropHandler<>());
	}

	private void registerRecipeHandlers(EmiRegistry registry) {
		registry.addRecipeHandler(ModContent.MOUNTED_STORAGE_CONTAINER_TYPE.get(), EmiGridMenuInfo.crafting());
	}
}
