package net.cdnbcn.vaultfixes.mixin.saving;

import iskallia.vault.core.vault.stat.VaultSnapshot;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.ServerboundOpenHistoricMessage;
import iskallia.vault.network.message.VaultPlayerHistoricDataMessage;
import iskallia.vault.world.data.PlayerHistoricFavoritesData;
import net.cdnbcn.vaultfixes.VaultFixes;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.ServerPlayerMixinInterface;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;
import java.util.function.Supplier;

@Mixin(ServerboundOpenHistoricMessage.class)
public class ServerboundOpenHistoricMessageMixin {

    @Unique
    private static final int vaultFixes$loadAmount = 35;
    /**
     * @author KoromaruKoruko
     * @reason Modify to use new Snapshot Storage Method
     */
    @Overwrite(remap = false)
    public static void handle(ServerboundOpenHistoricMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(
                () -> {
                    ServerPlayer sender = context.getSender();
                    if (sender != null) {
                        final var vaultSnapshots = VaultFixes.getVaultSnapshots();
                        ArrayList<UUID> resultIds =
                                ((ServerPlayerMixinInterface)sender).vaultFixes$getLastSnapshots(vaultFixes$loadAmount)
                                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                        Collections.reverse(resultIds);

                        PlayerHistoricFavoritesData.get(sender.server)
                                .getHistoricFavorites(sender)
                                .getFavorites()
                                .stream()
                                .filter(id -> !resultIds.contains(id))
                                .forEach(resultIds::add);

                        ArrayList<VaultSnapshot> result =
                                vaultSnapshots.vaultFixes$readSnapshots(resultIds.stream())
                                .filter(snapshot -> snapshot != null && snapshot.getEnd() != null)
                                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

                        ModNetwork.CHANNEL.sendTo(new VaultPlayerHistoricDataMessage.S2C(result), sender.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                    }
                }
        );
        context.setPacketHandled(true);
    }
}
