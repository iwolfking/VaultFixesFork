package net.cdnbcn.vaultfixes;

import com.mojang.logging.LogUtils;
import iskallia.vault.world.data.VaultSnapshots;
import net.cdnbcn.vaultfixes.config.ConfigManager;
import net.cdnbcn.vaultfixes.data.TemporalMapCache;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.VaultSnapshotsMixinInterface;
import net.cdnbcn.vaultfixes.saving.PlayerSaveManger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod("vaultfixes")
public class VaultFixes {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path DataDirectory;
    static {
        try {
            DataDirectory = Files.createDirectories(Path.of(
                    DedicatedServerProperties.fromFile(Path.of("server.properties")).levelName,
                    "data",
                    "vaultfixes"
            )).toAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * gets the current MinecraftServer Instance
     *
     * @return MinecraftServer Instance
     */
    public static MinecraftServer getServer() { return ServerLifecycleHooks.getCurrentServer(); }

    /** gets the VaultSnapshots Manager as our own Interface Impl
     *
     * @return (VaultSnapshotsMixinInterface)VaultSnapshots.get(getServer())
     */
    public static VaultSnapshotsMixinInterface getVaultSnapshots() { return (VaultSnapshotsMixinInterface) VaultSnapshots.get(getServer()); }

    /**
     *  a publicly accessible method to get the logger (for use within a mixin).
     *
     * @return VaultFixes Logger
     */
    @SuppressWarnings("unused")
    public static Logger getLogger() { return LOGGER; }

    public static Path getDataDir() { return DataDirectory; }

    public VaultFixes() {
        if(!FMLEnvironment.dist.isDedicatedServer())
        {
            LOGGER.error("VaultFixes is only for DedicatedServers, REFUSING TO LOAD");
            throw new RuntimeException("VaultFixes is only for DedicatedServers, REFUSING TO LOAD");
        }
        ConfigManager.INSTANCE.getConfig(); // invoke initializer to preload config

        MinecraftForge.EVENT_BUS.addListener((ServerStoppingEvent event) -> {
            TemporalMapCache.Companion.doCleanUpAll$vaultfixes(); // eject all potentially unsaved data
        });

        PlayerSaveManger.initialize$vaultfixes();
    }
}
