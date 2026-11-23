package net.p3pp3rf1y.sophisticatedstoragecreateintegration.storage;

import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

public final class MountedStorageReadContext {
	private static final ThreadLocal<ServerLevel> SERVER_LEVEL = new ThreadLocal<>();

	private MountedStorageReadContext() {
	}

	public static void setServerLevel(ServerLevel level) {
		SERVER_LEVEL.set(level);
	}

	public static Optional<ServerLevel> getServerLevel() {
		return Optional.ofNullable(SERVER_LEVEL.get());
	}

	public static void clearServerLevel() {
		SERVER_LEVEL.remove();
	}
}
