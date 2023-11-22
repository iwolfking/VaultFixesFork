package net.cdnbcn.vaultfixes.mixin.saving;


import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.stat.VaultSnapshot;
import iskallia.vault.nbt.VListNBT;
import iskallia.vault.world.data.VaultSnapshots;
import net.cdnbcn.vaultfixes.VaultFixes;
import net.cdnbcn.vaultfixes.data.TemporalMapCache;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.VListNBTMixinInterface;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.VaultSnapshotMixinInterface;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.VaultSnapshotsMixinInterface;
import net.cdnbcn.vaultfixes.saving.PlayerSaveManger;
import net.minecraft.nbt.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Mixin(VaultSnapshots.class)
public class VaultSnapshotsMixin implements VaultSnapshotsMixinInterface {

    // debug only function
    @Inject(method = "create", at=@At("RETURN"), remap = false)
    private static void create(CompoundTag tag, CallbackInfoReturnable<VaultSnapshots> cir) {
        VaultFixes.INSTANCE.getLogger().debug("Creating VaultSnapshots DataStorage");
    }


    @Unique
    private final TemporalMapCache<UUID, VaultSnapshotMixinInterface> vaultFixes$cache =
            new TemporalMapCache<>(120, (a)->{}, this::vaultFixes$saveSnapshot);
    @Final
    @Shadow(remap = false)
    private VListNBT<VaultSnapshot, LongArrayTag> snapshots;


    /**
     * @author KoromaruKoruko
     * @reason Modify Save and Load
     */
    @Overwrite(remap = false)
    public static VaultSnapshot get(UUID vaultId) {
        return VaultFixes.INSTANCE.getVaultSnapshots().vaultFixes$getSnapshot(vaultId);
    }
    /**
     * @author KoromaruKoruko
     * @reason Modify Save and Load, please don't call this anymore!
     */
    @Overwrite(remap = false)
    public static List<VaultSnapshot> getAll(){
        // I hate this function, It's so fucking stupid

        // we want to remove all access to this function, LOG ALL ACCESSES
        StringBuilder sb = new StringBuilder();
        sb.append("Bad Access to VaultSnapshots.getAll() Method!\n");
        int x = 0;
        for(var frame : Thread.currentThread().getStackTrace()) {
            if(++x > 10)
                break;
            sb.append("  at ");
            sb.append(frame.toString());
            sb.append('\n');
        }
        VaultFixes.INSTANCE.getLogger().error(sb.toString());

        return VaultFixes.INSTANCE.getVaultSnapshots().vaultFixes$getAllSnapshots().toList();
    }



    //region vault Creation&Completion Events
    /**
     * @author KoromaruKoruko
     * @reason Modify Save and Load, please don't call this anymore!
     */
    @Overwrite(remap = false)
    public static void onVaultStarted(Vault vault) {
        final var rawVaultSnapshots = VaultSnapshots.get(VaultFixes.INSTANCE.getServer());
        final var vaultSnapshots = ((VaultSnapshotsMixinInterface)rawVaultSnapshots);
        vaultSnapshots.vaultFixes$createSnapshot(vault);
        rawVaultSnapshots.setDirty();
    }
    /**
     * @author KoromaruKoruko
     * @reason Modify Save and Load, please don't call this anymore!
     */
    @Overwrite(remap = false)
    public static void onVaultEnded(Vault vault) {
        final var rawVaultSnapshots = VaultSnapshots.get(VaultFixes.INSTANCE.getServer());
        final var vaultSnapshots = ((VaultSnapshotsMixinInterface)rawVaultSnapshots);
        final var vaultId = vault.get(Vault.ID);

        vaultSnapshots.vaultFixes$getSnapshot(vaultId).setEnd(vault);

        vault.get(Vault.STATS).getMap().keySet().forEach(id -> vaultSnapshots.vaultFixes$addForPlayer(id, vaultId));

        rawVaultSnapshots.setDirty();
    }
    //endregion vault Creation&Completion Events

    @Override
    public void vaultFixes$createSnapshot(Vault vault) {
        final var rawSnapshot = new VaultSnapshot(vault.get(Vault.VERSION));
        final var snapshot = ((VaultSnapshotMixinInterface)rawSnapshot);
        rawSnapshot.setStart(vault);
        //noinspection OptionalGetWithoutIsPresent
        vaultFixes$cache.putIfAbsent(snapshot.vaultFixes$getVaultID().get(), snapshot);
    }
    @Override
    public Stream<VaultSnapshot> vaultFixes$getAllSnapshots() {
        // yes It's ugly, and yes I don't want it to be called AT ALL
        //noinspection DataFlowIssue
        return Stream.concat(
                snapshots.stream(),
                vaultFixes$readSnapshots(
                    Arrays.stream(vaultFixes$getSnapshotsSaveFolder().toFile().listFiles())
                        .parallel()
                        .map(file -> {
                            final var fileName = file.getName();
                            if(!fileName.startsWith("snapshot_") | !fileName.endsWith(".nbt"))
                                return null;

                            return UUID.fromString(fileName.substring("snapshot_".length(), fileName.length() - 4));
                        })
                        .filter(Objects::nonNull)
                )
        );
    }

    @Override
    public Stream<VaultSnapshot> vaultFixes$readSnapshots(Stream<UUID> snapshots) {
        @SuppressWarnings("unchecked")
        final var readFn = ((VListNBTMixinInterface<VaultSnapshot, LongArrayTag>)this.snapshots).vaultFixes$getRead();
        //noinspection DataFlowIssue
        return snapshots.parallel()
                .map((id) -> {
                    final var inCache = vaultFixes$cache.getOrDefault(id, null);
                    if (inCache != null)
                        return inCache;

                    final var file = vaultFixes$getFileFor(id);
                    if(file.exists())
                    {
                        try {
                            return NbtIo.read(file);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    for (VaultSnapshot snapshot : this.snapshots) {
                        final var sid = ((VaultSnapshotMixinInterface)snapshot).vaultFixes$getVaultID();
                        if (sid.isPresent() && sid.get() == id) {
                            return snapshot;
                        }
                    }

                    return null;
                })
                .toList()
                .stream()
                .map(obj -> {
                   if(obj instanceof CompoundTag tag) {
                       final var rawSnapshot = readFn.apply((LongArrayTag)tag.get("data"));
                       final var snapshot = (VaultSnapshotMixinInterface)rawSnapshot;
                       //noinspection OptionalGetWithoutIsPresent
                       vaultFixes$cache.put(snapshot.vaultFixes$getVaultID().get(), snapshot);
                       return rawSnapshot;
                   }
                   else
                       return (VaultSnapshot)obj;
                });
    }

    @Override
    public Stream<UUID> vaultFixes$getAllForPlayer(UUID playerId) { return PlayerSaveManger.getPlayerData(playerId).vaultFixes$getAllSnapshots(); }
    @Override
    public ArrayList<UUID> vaultFixes$compileAllForPlayer(UUID playerId) {
        return vaultFixes$getAllSnapshots()
                .filter(snapshot ->
                        snapshot.getEnd() != null
                        && snapshot.getEnd().get(Vault.STATS).getMap().containsKey(playerId)
                )
                .map(snapshot -> snapshot.getStart().get(Vault.ID))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @Override
    public void vaultFixes$addForPlayer(UUID player, UUID snapshotId) {
        PlayerSaveManger.getPlayerData(player).vaultFixes$addSnapshot(snapshotId);
    }

    @Override
    public VaultSnapshot vaultFixes$getSnapshot(UUID id) {
        final var inCache = vaultFixes$cache.getOrDefault(id, null);
        if (inCache != null)
            return (VaultSnapshot) inCache;


        final var fileLoc = vaultFixes$getFileFor(id);
        if (fileLoc.exists())
        {
            VaultSnapshot snapshot = vaultFixes$readFile(fileLoc);
            vaultFixes$cache.putIfAbsent(id, (VaultSnapshotMixinInterface)snapshot);
            return snapshot;
        }


        for (VaultSnapshot snapshot : snapshots) {
            final var sid = ((VaultSnapshotMixinInterface)snapshot).vaultFixes$getVaultID();
            if (sid.isPresent() && sid.get() == id) {
                return snapshot;
            }
        }

        VaultFixes.INSTANCE.getLogger().error("VaultSnapshots: failed to fetch snapshot for "+id);
        return null;
    }

    @Unique
    private void vaultFixes$saveSnapshot(VaultSnapshotMixinInterface snapshot) {
        //noinspection OptionalGetWithoutIsPresent
        vaultFixes$writeFile(vaultFixes$getFileFor(snapshot.vaultFixes$getVaultID().get()), (VaultSnapshot) snapshot);
    }

    //region Save&Load
    /**
     * @author KoromaruKoruko
     * @reason Modify Save and Load
     */
    @Overwrite
    public CompoundTag save(CompoundTag nbt) {
        // eject and save stale data
        vaultFixes$cache.doCleanUp();

        // io bound task, as such we parallelize it
        snapshots.parallelStream().forEach( (rawSnapshot) -> {
            final var snapshot = ((VaultSnapshotMixinInterface)rawSnapshot);
            snapshot.vaultFixes$getVaultID()
                    .ifPresent(uuid -> {
                        vaultFixes$cache.put(uuid, snapshot);
                        vaultFixes$writeFile(vaultFixes$getFileFor(uuid), rawSnapshot);
                    });
        });

        nbt.put("snapshots", snapshots.serializeNBT());
        return nbt;
    }
    /**
     * @author KoromaruKoruko
     * @reason Modify Save and Load
     */
    @Inject(method = "load", at = @At("RETURN"), remap = false)
    public void load(CompoundTag nbt, CallbackInfo ci) {
        // mark any snapshot with an ID as dirty, so that it gets pushed into its respective file
        snapshots.parallelStream().forEach(val -> {
            final var snapshot = ((VaultSnapshotMixinInterface)val);
            if (snapshot.vaultFixes$getVaultID().isPresent())
                snapshot.vaultFixes$MarkDirty();
        });
    }

    @Unique
    private VaultSnapshot vaultFixes$readFile(File file) {
        try {
            //noinspection unchecked
            return ((VListNBTMixinInterface<VaultSnapshot, LongArrayTag>)snapshots)
                    .vaultFixes$getRead()
                    .apply((LongArrayTag)(Objects.requireNonNull(NbtIo.read(file)).get("data")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Unique
    private void vaultFixes$writeFile(File file, VaultSnapshot snapshot) {
        try {
            CompoundTag tag = new CompoundTag();
            //noinspection unchecked
            tag.put("data", ((VListNBTMixinInterface<VaultSnapshot, LongArrayTag>)snapshots)
                    .vaultFixes$getWrite()
                    .apply(snapshot)
            );
            NbtIo.write(tag, file);
            ((VaultSnapshotMixinInterface)snapshot).vaultFixes$MarkClean();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Unique
    private File vaultFixes$getFileFor(UUID id) { return vaultFixes$getSnapshotsSaveFolder().resolve("snapshot_"+id+".nbt").toFile(); }

    @Unique
    private static Path vaultFixes$_snapshotsFolder = null;
    @Unique
    private static Path vaultFixes$getSnapshotsSaveFolder() {
        if(vaultFixes$_snapshotsFolder == null)
            try {
                vaultFixes$_snapshotsFolder = Files.createDirectories(VaultFixes.INSTANCE.getDataDir().resolve("snapshots"));
            } catch (IOException e) {
                throw new RuntimeException(e); // should never happen!
            }


        return vaultFixes$_snapshotsFolder;
    }
    //endregion Save&Load
}
