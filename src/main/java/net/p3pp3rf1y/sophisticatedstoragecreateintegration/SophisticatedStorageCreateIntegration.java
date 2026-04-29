package net.p3pp3rf1y.sophisticatedstoragecreateintegration;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.init.ModCompat;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.init.ModContent;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.network.ModPayloads;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SophisticatedStorageCreateIntegration.MOD_ID)
public class SophisticatedStorageCreateIntegration {
	public static final String MOD_ID = "sophisticatedstoragecreateintegration";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	private static String networkProtocolVersion;

	@SuppressWarnings("java:S1118") //needs to be public for mod to work
	public SophisticatedStorageCreateIntegration(IEventBus modBus, Dist dist, ModContainer container) {
		networkProtocolVersion = container.getModInfo().getVersion().toString();
		ModContent.registerHandler(modBus);
		modBus.addListener(ModPayloads::registerPayloads);
		ModCompat.register();
	}

	public static ResourceLocation getRL(String regName) {
		return ResourceLocation.parse(getRegistryName(regName));
	}

	public static String getRegistryName(String regName) {
		return MOD_ID + ":" + regName;
	}

	public static String getNetworkProtocolVersion() {
		return networkProtocolVersion;
	}
}
