package net.p3pp3rf1y.sophisticatedstoragecreateintegration.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.CraftingContainerRecipeTransferHandlerBase;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.SettingsGhostIngredientHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.StorageGhostIngredientHandler;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.SophisticatedStorageCreateIntegration;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.client.MountedStorageScreen;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.client.MountedStorageSettingsScreen;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedStorageContainerMenu;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@JeiPlugin
public class StorageCreatePlugin implements IModPlugin {

	@Override
	public ResourceLocation getPluginUid() {
		return ResourceLocation.fromNamespaceAndPath(SophisticatedStorageCreateIntegration.MOD_ID, "default");
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGuiContainerHandler(MountedStorageScreen.class, new IGuiContainerHandler<>() {
			@Override
			public List<Rect2i> getGuiExtraAreas(MountedStorageScreen gui) {
				List<Rect2i> ret = new ArrayList<>();
				gui.getUpgradeSlotsRectangle().ifPresent(ret::add);
				ret.addAll(gui.getUpgradeSettingsControl().getTabRectangles());
				gui.getSortButtonsRectangle().ifPresent(ret::add);
				return ret;
			}
		});

		registration.addGuiContainerHandler(MountedStorageSettingsScreen.class, new IGuiContainerHandler<>() {
			@Override
			public List<Rect2i> getGuiExtraAreas(MountedStorageSettingsScreen gui) {
				return new ArrayList<>(gui.getSettingsTabControl().getTabRectangles());
			}
		});

		registration.addGhostIngredientHandler(MountedStorageScreen.class, new StorageGhostIngredientHandler<>());
		registration.addGhostIngredientHandler(MountedStorageSettingsScreen.class, new SettingsGhostIngredientHandler<>());
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		IRecipeTransferHandlerHelper handlerHelper = registration.getTransferHelper();
		IStackHelper stackHelper = registration.getJeiHelpers().getStackHelper();
		registration.addRecipeTransferHandler(new CraftingContainerRecipeTransferHandlerBase<MountedStorageContainerMenu, RecipeHolder<CraftingRecipe>>(handlerHelper, stackHelper) {
			@Override
			public Class<MountedStorageContainerMenu> getContainerClass() {
				return MountedStorageContainerMenu.class;
			}

			@Override
			public mezz.jei.api.recipe.RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
				return RecipeTypes.CRAFTING;
			}
		}, RecipeTypes.CRAFTING);
	}
}
