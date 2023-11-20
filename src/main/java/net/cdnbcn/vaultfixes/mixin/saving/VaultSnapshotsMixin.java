package net.cdnbcn.vaultfixes.mixin.saving;


import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.stat.VaultSnapshot;
import iskallia.vault.nbt.VListNBT;
import iskallia.vault.world.data.VaultSnapshots;
import net.cdnbcn.vaultfixes.VaultFixes;
import net.cdnbcn.vaultfixes.data.AccessTimedData;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.ServerPlayerMixinInterface;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.VListNBTMixinInterface;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.VaultSnapshotMixinInterface;
import net.cdnbcn.vaultfixes.mixin_interfaces.saving.VaultSnapshotsMixinInterface;
import net.minecraft.nbt.*;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

@Mixin(VaultSnapshots.class)
public class VaultSnapshotsMixin implements VaultSnapshotsMixinInterface {

    @Unique
    private static final HashMap<UUID, AccessTimedData<VaultSnapshot>> vaultFixes$cache = new HashMap<>();

    @Final
    @Shadow(remap = false)
    private VListNBT<VaultSnapshot, LongArrayTag> snapshots;

    /**
     * @author KoromaruKoruko
     * @reason Modify Save and Load
     */
    @Overwrite
    public CompoundTag save(CompoundTag nbt) {
        final ListIterator<VaultSnapshot> iter = snapshots.listIterator();
        while(iter.hasNext()) {
            final VaultSnapshot snapshot = iter.next();

            if(((VaultSnapshotMixinInterface)snapshot).vaultFixes$NeedsSave())
            {
                final var optid = ((VaultSnapshotMixinInterface)snapshot).vaultFixes$getVaultID();
                if(optid.isPresent()) {
                    final var id = optid.get();

                    vaultFixes$cache.put(id, new AccessTimedData<>(snapshot));
                    vaultFixes$writeFile(vaultFixes$getFileFor(id), snapshot);
                    iter.remove();
                }
            }
        }

        final Iterator<AccessTimedData<VaultSnapshot>> cacheIter = vaultFixes$cache.values().iterator();


        while(cacheIter.hasNext()){
            final AccessTimedData<VaultSnapshot> snapshot = cacheIter.next();

            if(Instant.now().isAfter(snapshot.getLastAccess().plusSeconds(60)))
                cacheIter.remove();

            final VaultSnapshot rawSnapshot = snapshot.getNoAccess();
            if(((VaultSnapshotMixinInterface)rawSnapshot).vaultFixes$NeedsSave())
                ((VaultSnapshotMixinInterface)rawSnapshot).vaultFixes$getVaultID()
                    .ifPresent(uuid -> vaultFixes$writeFile(vaultFixes$getFileFor(uuid), rawSnapshot));

        }

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
        for (VaultSnapshot val : snapshots) {
            if (((VaultSnapshotMixinInterface) val).vaultFixes$getVaultID().isPresent())
                ((VaultSnapshotMixinInterface) val).vaultFixes$MarkNeedsSaving();
        }
    }

    @Inject(method = "create", at=@At("RETURN"),remap = false)
    private static void create(CompoundTag tag, CallbackInfoReturnable<VaultSnapshots> cir) {
        VaultFixes.getLogger().info("Creating VaultSnapshots DataStorage");
    }

    /**
     * @author KoromaruKoruko
     * @reason Modify Save and Load
     */
    @Overwrite(remap = false)
    public static VaultSnapshot get(UUID vaultId) {
        return ((VaultSnapshotsMixinInterface)VaultSnapshots.get(ServerLifecycleHooks.getCurrentServer())).vaultFixes$getSnapshot(vaultId);
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
        sb.append("Bad Access to getAll Method!\n");
        int x = 0;
        for(var frame : Thread.currentThread().getStackTrace()) {
            if(++x > 10)
                break;
            sb.append("  at ");
            sb.append(frame.toString());
            sb.append('\n');
        }
        VaultFixes.getLogger().warn(sb.toString());

        return ((VaultSnapshotsMixinInterface)VaultSnapshots.get(ServerLifecycleHooks.getCurrentServer())).vaultFixes$getAllSnapshots().toList();
    }


    /**
     * @author KoromaruKoruko
     * @reason Modify Save and Load, please don't call this anymore!
     */
    @Overwrite(remap = false)
    public static void onVaultStarted(Vault vault) {
        final var rawVaultSnapshots = VaultSnapshots.get(VaultFixes.getServer());
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
        final var rawVaultSnapshots = VaultSnapshots.get(VaultFixes.getServer());
        final var vaultSnapshots = ((VaultSnapshotsMixinInterface)rawVaultSnapshots);
        final var vaultId = vault.get(Vault.ID);

        vaultSnapshots.vaultFixes$getSnapshot(vaultId).setEnd(vault);

        vault.get(Vault.STATS).getMap().keySet().forEach(id -> vaultSnapshots.vaultFixes$addForPlayer(id, vaultId));

        rawVaultSnapshots.setDirty();
    }

    @Override
    public VaultSnapshot vaultFixes$createSnapshot(Vault vault) {
        final var rawSnapshot = new VaultSnapshot(vault.get(Vault.VERSION));
        final var snapshot = ((VaultSnapshotMixinInterface)rawSnapshot);
        rawSnapshot.setStart(vault);
        //noinspection OptionalGetWithoutIsPresent
        vaultFixes$cache.putIfAbsent(snapshot.vaultFixes$getVaultID().get(), new AccessTimedData<>(rawSnapshot));
        return rawSnapshot;
    }

    @Unique
    public Stream<VaultSnapshot> vaultFixes$getAllSnapshots() {
        // yes It's ugly, and yes I don't want it to be called AT ALL
        return Stream.concat(
                snapshots.stream(),
                Arrays.stream(Objects.requireNonNull(vaultFixes$getSnapshotsSaveFolder().toFile().listFiles()))
                    .map(file -> {
                        final var fileName = file.getName();
                        if(!fileName.startsWith("snapshot_") | !fileName.endsWith(".nbt"))
                            return null;
                        try {
                             final var id = UUID.fromString(fileName.substring("snapshot_".length(), fileName.length() - 4));
                             final var inCache = vaultFixes$cache.getOrDefault(id, null);
                             if(inCache != null)
                                 return  inCache.get();
                             return vaultFixes$readFile(file);
                        }catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                );
    }

    @Override
    public Stream<UUID> vaultFixes$getAllForPlayer(UUID playerId) {
        final var serverPlayer = VaultFixes.getServer().getPlayerList().getPlayer(playerId);
        if (serverPlayer == null)
            return vaultFixes$loadAllForPlayer(playerId);

        return ((ServerPlayerMixinInterface)serverPlayer).vaultFixes$getAllSnapshots();
    }

    @Override
    public Stream<UUID> vaultFixes$loadAllForPlayer(UUID playerId) {
        final var file = vaultFixes$getPlayerSnapshotsFolder().resolve(playerId+".nbt").toFile();
        if (file.exists())
        {
            try {
                final var nbt = NbtIo.read(file);
                if(nbt != null)
                    return Objects.requireNonNull((ListTag)nbt.get("snapshots")).stream().map(NbtUtils::loadUUID);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // this dirty override exists to port any previous data to the new one!
        // and will execute only once whenever a player joins for the very first time!

        //noinspection OptionalGetWithoutIsPresent
        return vaultFixes$getAllSnapshots().filter(snapshot -> {
            final var end = snapshot.getEnd();
            if (end != null)
                return end.get(Vault.STATS).getMap().containsKey(playerId);

            return false;
        }).map(snapshot -> ((VaultSnapshotMixinInterface)snapshot).vaultFixes$getVaultID().get());
    }

    @Override
    public void vaultFixes$setAllForPlayer(UUID playerId, Stream<UUID> snapshotIds) {
        final var file = vaultFixes$getPlayerSnapshotsFolder().resolve(playerId+".nbt").toFile();
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        snapshotIds.forEach(id -> list.add(NbtUtils.createUUID(id)));
        tag.put("snapshots", list);


        try {
            NbtIo.write(tag, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void vaultFixes$addForPlayer(UUID player, UUID snapshotId)
    {
        final var onlinePlayer = VaultFixes.getServer().getPlayerList().getPlayer(player);
        if(onlinePlayer != null)
        {
            ((ServerPlayerMixinInterface)onlinePlayer).vaultFixes$addSnapshot(snapshotId); // will get saved on player leave
            return;
        }

        vaultFixes$setAllForPlayer(player, Stream.concat(vaultFixes$getAllForPlayer(player), Stream.of(snapshotId)));
    }

    @Override
    public VaultSnapshot vaultFixes$getSnapshot(UUID id) {
        final var inCache = vaultFixes$cache.getOrDefault(id, null);
        if (inCache != null)
            return inCache.get();


        final var fileLoc = vaultFixes$getFileFor(id);
        if (fileLoc.exists())
        {
            VaultSnapshot snapshot = vaultFixes$readFile(fileLoc);
            vaultFixes$cache.putIfAbsent(id, new AccessTimedData<>(snapshot));
            return snapshot;
        }


        for (VaultSnapshot snapshot : snapshots) {
            final var sid = ((VaultSnapshotMixinInterface)snapshot).vaultFixes$getVaultID();
            if (sid.isPresent() && sid.get() == id) {
                return snapshot;
            }
        }

        //VaultFixes.getLogger().warn("failed to fetch snapshot for "+id);
        return null;
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
            ((VaultSnapshotMixinInterface)snapshot).vaultFixes$MarkSaved();
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
        {
            vaultFixes$_snapshotsFolder = Path.of(
                    DedicatedServerProperties.fromFile(Path.of("server.properties")).levelName,
                    "data",
                    "vaultfixes",
                    "snapshots"
            );

            try {
                Files.createDirectories(vaultFixes$_snapshotsFolder);
            } catch (IOException e) {
                throw new RuntimeException(e); // should never happen!
            }
        }

        return vaultFixes$_snapshotsFolder;
    }

    @Unique
    private static Path vaultFixes$_playerSnapshotsFolder = null;
    @Unique
    private static Path vaultFixes$getPlayerSnapshotsFolder() {
        if(vaultFixes$_playerSnapshotsFolder == null)
        {
            vaultFixes$_playerSnapshotsFolder = Path.of(
                    DedicatedServerProperties.fromFile(Path.of("server.properties")).levelName,
                    "data",
                    "vaultfixes",
                    "player_snapshots"
            );

            try {
                Files.createDirectories(vaultFixes$_playerSnapshotsFolder);
            } catch (IOException e) {
                throw new RuntimeException(e); // should never happen!
            }
        }

        return vaultFixes$_playerSnapshotsFolder;
    }
}
