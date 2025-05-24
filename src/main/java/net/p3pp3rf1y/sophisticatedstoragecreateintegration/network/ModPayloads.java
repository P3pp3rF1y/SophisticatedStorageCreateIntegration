package net.p3pp3rf1y.sophisticatedstoragecreateintegration.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.SophisticatedStorageCreateIntegration;

public class ModPayloads {
	private ModPayloads() {}

	public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar(SophisticatedStorageCreateIntegration.MOD_ID).versioned("1.0");
		registrar.playToClient(MountedStorageOpennessPayload.TYPE, MountedStorageOpennessPayload.STREAM_CODEC, MountedStorageOpennessPayload::handlePayload);
	}
}
