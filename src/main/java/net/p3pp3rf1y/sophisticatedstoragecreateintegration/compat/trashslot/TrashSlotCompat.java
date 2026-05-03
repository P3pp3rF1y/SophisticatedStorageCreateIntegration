package net.p3pp3rf1y.sophisticatedstoragecreateintegration.compat.trashslot;

import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.init.ModContent;

public class TrashSlotCompat implements ICompat {
	@Override
	public void setup() {
		net.p3pp3rf1y.sophisticatedcore.compat.trashslot.TrashSlotCompat.registerMenuType(ModContent.MOUNTED_STORAGE_CONTAINER_TYPE.get());
	}
}
