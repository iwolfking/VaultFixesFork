package net.cdnbcn.vaultfixes.mixin.saving;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.stat.VaultSnapshot;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.ServerboundOpenHistoricMessage;
import iskallia.vault.network.message.VaultPlayerHistoricDataMessage;
import iskallia.vault.world.data.PlayerHistoricFavoritesData;
import iskallia.vault.world.data.VaultSnapshots;
import net.cdnbcn.vaultfixes.VaultFixes;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.VaultSnapshotsMixinInterface;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Mixin(ServerboundOpenHistoricMessage.class)
public class ServerboundOpenHistoricMessageMixin {

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
                        final var vaultSnapshots = (VaultSnapshotsMixinInterface)VaultSnapshots.get(VaultFixes.getServer());
                        Stream<UUID> playerSnapshots = vaultSnapshots.vaultFixes$getAllForPlayer(sender.getUUID());
                        AtomicInteger xpos = new AtomicInteger();
                        AtomicInteger inuse = new AtomicInteger();
                        UUID[] prev60 = new UUID[60];
                        playerSnapshots.forEachOrdered(uuid -> {
                            var pos = xpos.getAndIncrement();
                            if(pos == 60)
                            {
                                pos = 0;
                                xpos.set(1);
                            }

                            prev60[pos] = uuid;

                            if (inuse.get() < 60)
                                inuse.incrementAndGet();
                        }); // get last 60 (we grab extra in case there are uncompleted vaults)

                        ArrayList<VaultSnapshot> result = Stream.concat(
                                    xpos.get()==inuse.get() ? Stream.empty() : Arrays.stream(prev60, xpos.get(), inuse.get()),
                                    xpos.get()==0 ? Stream.empty() : Arrays.stream(prev60, 0, xpos.get())
                                )
                                .map(vaultSnapshots::vaultFixes$getSnapshot)
                                .filter(snapshot -> snapshot != null && snapshot.getEnd() != null)
                                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                        Collections.reverse(result);

                        PlayerHistoricFavoritesData.get(sender.server).getHistoricFavorites(sender).getFavorites()
                            .stream()
                            .filter(id -> result.stream().noneMatch(snapshot -> snapshot.getEnd().get(Vault.ID) == id))
                            .map(vaultSnapshots::vaultFixes$getSnapshot)
                            .filter(Objects::nonNull)
                            .forEachOrdered(result::add)
                        ;

                        ModNetwork.CHANNEL.sendTo(new VaultPlayerHistoricDataMessage.S2C(result), sender.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                    }
                }
        );
        context.setPacketHandled(true);
    }
}
