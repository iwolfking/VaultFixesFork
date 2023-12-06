package net.cdnbcn.vaultfixes.mixin.fakeplayer;

import iskallia.vault.init.ModConfigs;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.research.type.Research;
import iskallia.vault.world.data.PlayerResearchesData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerResearchesData.class)
public class PlayerResearchesDataMixin {
    @Shadow(remap = false)
    private final Map<UUID, ResearchTree> playerMap = new HashMap<UUID, ResearchTree>();

    private boolean fakePlayerResearchInjected = false;
    private static UUID defaultFakePlayerUuid = UUID.fromString("41c82c87-7afb-4024-ba57-13d2c99cae77");

    @Inject(
            method = "getResearches(Ljava/util/UUID;)Liskallia/vault/research/ResearchTree;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void getResearches$return(UUID uuid, CallbackInfoReturnable<ResearchTree> ci) {
        if (!this.fakePlayerResearchInjected && uuid.equals(defaultFakePlayerUuid)) {
            this.playerMap.remove(defaultFakePlayerUuid);
            ResearchTree tree = ResearchTree.empty();
            for (Research research : ModConfigs.RESEARCHES.getAll())
                tree.research(research);
            this.playerMap.put(defaultFakePlayerUuid, tree);
            this.fakePlayerResearchInjected = true;
            ci.setReturnValue(tree);
        }
    }
}
