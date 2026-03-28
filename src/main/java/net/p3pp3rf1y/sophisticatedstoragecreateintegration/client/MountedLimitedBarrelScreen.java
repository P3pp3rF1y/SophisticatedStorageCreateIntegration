package net.p3pp3rf1y.sophisticatedstoragecreateintegration.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.LimitedBarrelScreen;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.common.MountedStorageContainerMenu;

public class MountedLimitedBarrelScreen extends MountedStorageScreen {
	public static final int STORAGE_SLOTS_HEIGHT = 82;

	public MountedLimitedBarrelScreen(MountedStorageContainerMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}

	@Override
	protected void drawSlotBg(GuiGraphicsExtractor guiGraphics, int x, int y, int visibleSlotsCount) {
		LimitedBarrelScreen.drawSlotBg(this, guiGraphics, x, y, getMenu().getNumberOfStorageInventorySlots());
	}

	@Override
	protected void renderLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		super.renderLabels(guiGraphics, mouseX, mouseY);
		LimitedBarrelScreen.renderBars(font, imageWidth, getMenu(), guiGraphics, getMenu()::getSlotFillPercentage);
	}

	@Override
	protected int getStorageInventoryHeight(int displayableNumberOfRows) {
		return STORAGE_SLOTS_HEIGHT;
	}

	@Override
	protected void updateStorageSlotsPositions() {
		LimitedBarrelScreen.updateSlotPositions(getMenu(), getMenu().getNumberOfStorageInventorySlots(), imageWidth);
	}

	@Override
	protected boolean shouldShowSortButtons() {
		return false;
	}

	@Override
	protected void addSearchBox() {
		// No search box for limited barrels
	}
}
