package net.cdnbcn.vaultfixes;

import com.mojang.logging.LogUtils;
import iskallia.vault.world.data.VaultSnapshots;
import net.cdnbcn.vaultfixes.config.ConfigManager;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.ServerPlayerMixinInterface;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.VaultSnapshotsMixinInterface;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

@Mod("vaultfixes")
public class VaultFixes {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * gets the current MinecraftServer Instance
     *
     * @return MinecraftServer Instance
     */
    public static MinecraftServer getServer() { return ServerLifecycleHooks.getCurrentServer(); }

    /**
     *  a publicly accessible method to get the logger (for use within a mixin).
     *
     * @return VaultFixes Logger
     */
    @SuppressWarnings("unused")
    public static Logger getLogger() { return LOGGER; }

    public VaultFixes() {
        MinecraftForge.EVENT_BUS.register(this);
        ConfigManager.INSTANCE.getConfig(); // invoke initializer to preload config
    }

    @SubscribeEvent
    private void playerJoin(PlayerEvent.PlayerLoggedInEvent event)
    {
        final var player = (ServerPlayer)event.getPlayer();
        // load player snapshots file
        ((ServerPlayerMixinInterface)player).vaultFixes$setAllSnapshots(((VaultSnapshotsMixinInterface)VaultSnapshots.get(getServer())).vaultFixes$loadAllForPlayer(player.getUUID()));
    }
    @SubscribeEvent
    private void playerLeave(PlayerEvent.PlayerLoggedOutEvent event)
    {
        final var player = (ServerPlayer)event.getPlayer();
        // save player snapshots file
        ((VaultSnapshotsMixinInterface)VaultSnapshots.get(getServer())).vaultFixes$setAllForPlayer(player.getUUID(), ((ServerPlayerMixinInterface)player).vaultFixes$getAllSnapshots());
    }
}
