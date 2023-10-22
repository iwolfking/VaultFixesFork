package net.cdnbcn.vaultfixes.mixin_interfaces;

import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.TickingSkill;

import java.util.function.Consumer;

public interface SkillParentInterface {
    void vaultFixes$tickChildren(SkillContext context);

    void vaultFixes$iterateTickable(Consumer<TickingSkill> lambda);
}
