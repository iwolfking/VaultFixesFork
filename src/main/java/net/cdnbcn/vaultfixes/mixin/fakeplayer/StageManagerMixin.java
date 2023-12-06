package net.cdnbcn.vaultfixes.mixin.fakeplayer;

import iskallia.vault.research.ResearchTree;
import iskallia.vault.research.StageManager;
import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StageManager.class)
public class StageManagerMixin {
    @Redirect(method = "getResearchTree", at=@At(value = "INVOKE", target = "Liskallia/vault/research/ResearchTree;empty()Liskallia/vault/research/ResearchTree;", remap = true), remap = false)
    private static ResearchTree getResearchTree(Player player) {
        return PlayerResearchesData.get((ServerLevel) player.level).getResearches(player);
    }
}
