package net.p3pp3rf1y.sophisticatedstoragecreateintegration.network;

import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.SophisticatedStorageCreateIntegration;

public class StorageCreatePacketHandler extends PacketHandler {
	public static final StorageCreatePacketHandler INSTANCE = new StorageCreatePacketHandler(SophisticatedStorageCreateIntegration.MOD_ID);

	private StorageCreatePacketHandler(String modId) {
		super(modId);
	}

	@Override
	public void init() {
		registerMessage(MountedStorageOpennessMessage.class, MountedStorageOpennessMessage::encode, MountedStorageOpennessMessage::decode, MountedStorageOpennessMessage::onMessage);
	}
}
