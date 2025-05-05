package net.p3pp3rf1y.sophisticatedstoragecreateintegration.compat.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.client.renderer.Rect2i;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.rei.ReiCraftingContainerTransferHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.rei.ReiSettingsGhostIngredientHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.rei.ReiStorageGhostIngredientHandler;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.client.MountedStorageScreen;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.client.MountedStorageSettingsScreen;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedStorageContainerMenu;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@REIPluginClient
public class StorageCreateReiClientPlugin implements REIClientPlugin {
	@Override
	public void registerExclusionZones(ExclusionZones zones) {
		zones.register(MountedStorageScreen.class, screen -> {
			List<Rect2i> ret = new ArrayList<>();
			screen.getUpgradeSlotsRectangle().ifPresent(ret::add);
			ret.addAll(screen.getUpgradeSettingsControl().getTabRectangles());
			screen.getSortButtonsRectangle().ifPresent(ret::add);
			return ret.stream().map(r -> new Rectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight())).toList();
		});

		zones.register(MountedStorageSettingsScreen.class, screen -> screen.getSettingsTabControl().getTabRectangles().stream().map(r -> new Rectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight())).toList());
	}

	@Override
	public void registerScreens(ScreenRegistry registry) {
		registry.registerDraggableStackVisitor(new ReiStorageGhostIngredientHandler<>(MountedStorageScreen.class));
		registry.registerDraggableStackVisitor(new ReiSettingsGhostIngredientHandler<>(MountedStorageSettingsScreen.class));
	}

	@Override
	public void registerTransferHandlers(TransferHandlerRegistry registry) {
		registry.register(ReiCraftingContainerTransferHandler.crafting(MountedStorageContainerMenu.class));
	}
}
