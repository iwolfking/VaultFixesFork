package net.cdnbcn.vaultfixes.mixin;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class VaultFixesMixinPlugin implements IMixinConfigPlugin {
    private static final ArrayList<String> allowedMixins = new ArrayList<>();

    Map<String, List<String>> mixinEffectList = Map.ofEntries(
            new AbstractMap.SimpleEntry<>("disable_vault_autosave", List.of("VaultInitMixin")),
            new AbstractMap.SimpleEntry<>("disable_ispawner_mobai", List.of("SpawnerActionMixin", "MobMixin", "MobMixinInterface")),
            new AbstractMap.SimpleEntry<>("allow_chromatic_in_overworld_like", List.of("ChromaticIronMixin")),
            new AbstractMap.SimpleEntry<>("optimize_animal_pens", List.of("AnimalPenMixin")),
            new AbstractMap.SimpleEntry<>("optimize_bounties", List.of("MixinBountyData", "MixinCompletionTask", "MixinDamageTask", "MixinItemDiscoveryTask", "MixinItemSubmissionTask", "MixinKillEntityTask", "MixinMiningTask", "MixinTask", "TaskMixinInterface", "BountyDataMixinInterface")),
            new AbstractMap.SimpleEntry<>("optimize_vault_gear", List.of("VaultGearData_TypeMixin", "VaultGearDataMixin", "VaultGearData_TypeMixinInterface", "VaultGearDataMixinInterface")),
            new AbstractMap.SimpleEntry<>("optimize_player_skills", List.of("GroupSkillMixin", "SkillTreeMixin", "SpecializedSkillMixin", "TieredSkillMixin", "SkillParentInterface")),
            new AbstractMap.SimpleEntry<>("fake_players_have_research", List.of("PlayerResearchesDataMixin", "StageManagerMixin")),
            new AbstractMap.SimpleEntry<>("flan_images_are_claimed", List.of("ImageEntityMixin")),
            new AbstractMap.SimpleEntry<>("flan_create_is_claimed", List.of("LinkHandlerMixin", "RedstoneLinkBlockMixin")),
            new AbstractMap.SimpleEntry<>("flan_villagers_are_claimed", List.of("VillagerEventsMixin")),
            new AbstractMap.SimpleEntry<>("limit_activator_module_speed", List.of("ModularRouterBlockEntity")),
            new AbstractMap.SimpleEntry<>("allow_cata_frags_in_catalyzed", List.of("CrystalDataMixin")),
            new AbstractMap.SimpleEntry<>("optimize_vault_data_storage", List.of("PlayerStatisticsCollector$VaultRunsSnapshotMixin", "ServerboundOpenHistoricMessageMixin", "ServerPlayerMixin", "StatTotalsMixin", "VaultSnapshotMixin", "VaultSnapshotsMixin", "VListNBTMixin"))
    );

    @Override
    public void onLoad(String mixinPackage) {
        File configPath = FMLPaths.CONFIGDIR.get().resolve("vault-fixes-mixins.toml").toFile();
        CommentedFileConfig config;
        try {
            if(!configPath.exists()) {
                BufferedWriter bw = new BufferedWriter(new FileWriter(configPath));
                bw.write("""
                    # Disable Autosaving in Vaults - default: true\s
                    disable_vault_autosave = true
                    # Disable Mob AI from ISpawners - default: true\s
                    disable_ispawner_mobai = true
                    # Allow chromatic iron to generate in all Overworld like dimensions (i.e. resource worlds - default: true\s
                    allow_chromatic_in_overworld_like = true
                    # Greatly reduce the performance impact of animal pens - default: true\s
                    optimize_animal_pens = true
                    # Greatly reduce the performance impact of bounties - default: true\s
                    optimize_bounties = true
                    # Greatly reduce the performance impact of vault gear - default: true\s
                    optimize_vault_gear = true
                    # Greatly reduce the performance impact of player skills - default: true\s
                    optimize_player_skills = true
                    # Allow fake players to all mods (for autocrafting - default: true\s
                    fake_players_have_research = true
                    # Options below this point will either change game balance or functionality\s
                    # Disallow players to modify images in others claims - default: true\s
                    flan_images_are_claimed = true
                    # Disallow players to modify Create Links in others claims - default: true\s
                    flan_create_is_claimed = true
                    # Disallow the theft of villagers through shift+rclick in others claims- default: true\s
                    flan_villagers_are_claimed = true
                    # Limit speed of activator modules from modular routers - default: false\s
                    limit_activator_module_speed = false
                    # Allow Catalyst Frags to generate in all Vaults (incl. catalyzed - default: false\s
                    allow_cata_frags_in_catalyzed = false
                                            
                    # WARNING! This option will convert your players vault data into a new format irreversibly.\s
                    # This change is permanent but will retain all existing data\s
                    # If your server resets between updates, use this option.\s
                    # In the event this mod is not updated, your player data will be stuck on an old version of vault hunters\s
                    # default: (obviously) false\s
                    optimize_vault_data_storage = false""");
                bw.close();
            }
            config = CommentedFileConfig.of(configPath);
            config.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load vault-fixes-mixins.toml. Check your config.", e);
        }
        mixinEffectList.forEach((enabled, mixins) -> {
            if((boolean) config.get(enabled)) {
                allowedMixins.addAll(mixins);
            }
        });
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String[] packageTree = mixinClassName.split("\\.");
        return allowedMixins.contains(packageTree[packageTree.length-1]);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
