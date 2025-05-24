package net.p3pp3rf1y.sophisticatedstoragecreateintegration;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.init.ModContent;
import net.p3pp3rf1y.sophisticatedstoragecreateintegration.network.StorageCreatePacketHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SophisticatedStorageCreateIntegration.MOD_ID)
public class SophisticatedStorageCreateIntegration {
	public static final String MOD_ID = "sophisticatedstoragecreateintegration";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@SuppressWarnings("java:S1118") //needs to be public for mod to work
	public SophisticatedStorageCreateIntegration() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModContent.registerHandler(modBus);
		modBus.addListener(SophisticatedStorageCreateIntegration::setup);
	}

	private static void setup(FMLCommonSetupEvent event) {
		StorageCreatePacketHandler.INSTANCE.init();
	}

	public static ResourceLocation getRL(String regName) {
		return new ResourceLocation(getRegistryName(regName));
	}

	public static String getRegistryName(String regName) {
		return MOD_ID + ":" + regName;
	}
}
