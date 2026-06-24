package net.p3pp3rf1y.sophisticatedstoragecreateintegration.network;

import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.SophisticatedStorageCreateIntegration;

public class StorageCreatePacketHandler extends PacketHandler {
	public static final StorageCreatePacketHandler INSTANCE = new StorageCreatePacketHandler(SophisticatedStorageCreateIntegration.MOD_ID,
			SophisticatedStorageCreateIntegration.getNetworkProtocolVersion());

	private StorageCreatePacketHandler(String modId, String protocol) {
		super(modId, protocol);
	}

	@Override
	public void registerMessages() {
		registerMessage(MountedStorageOpennessMessage.class, MountedStorageOpennessMessage::encode, MountedStorageOpennessMessage::decode,
				MountedStorageOpennessMessage::onMessage);
	}
}
