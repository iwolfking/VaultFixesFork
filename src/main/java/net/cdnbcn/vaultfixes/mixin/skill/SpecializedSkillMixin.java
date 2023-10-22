package net.cdnbcn.vaultfixes.mixin.skill;

import iskallia.vault.skill.base.*;
import net.cdnbcn.vaultfixes.data.NotifyArrayList;
import net.cdnbcn.vaultfixes.mixin_interfaces.SkillParentInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(SpecializedSkill.class)
public abstract class SpecializedSkillMixin extends LearnableSkill implements SkillParentInterface {
    @Shadow(remap = false)
    private List<LearnableSkill> specializations;

    @Unique
    private final NotifyArrayList<LearnableSkill> vaultFixes$notifyList = new NotifyArrayList<>();
    @Unique
    private final List<SkillParentInterface> vaultFixes$nestedBranches = new ArrayList<>();
    @Unique
    private final List<TickingSkill> vaultFixes$tickingSkills = new ArrayList<>();

    @Inject(method = "<init>(IIILjava/util/stream/Stream;)V", at = @At("RETURN"), remap = false)
    private void ctor(CallbackInfo ci)
    {
        vaultFixes$notifyList.getOnRemove().register(this::VaultFixes$skillsOnRemove);
        vaultFixes$notifyList.getOnAdd().register(this::VaultFixes$skillsOnAdded);
        vaultFixes$notifyList.addAll(specializations);
        specializations = vaultFixes$notifyList;
    }
    @Inject(method ="<init>()V", at = @At("RETURN"), remap = false)
    private void ctor2(CallbackInfo ci)
    {
        vaultFixes$notifyList.getOnRemove().register(this::VaultFixes$skillsOnRemove);
        vaultFixes$notifyList.getOnAdd().register(this::VaultFixes$skillsOnAdded);
        specializations = vaultFixes$notifyList;
    }

    @Unique
    private void VaultFixes$skillsOnRemove(Skill skill)
    {
        if(skill == this)
            return;

        if(skill instanceof SkillParentInterface ps)
            vaultFixes$nestedBranches.remove(ps);

        if(skill instanceof TickingSkill ts)
            vaultFixes$tickingSkills.remove(ts);
    }
    @Unique
    private void VaultFixes$skillsOnAdded(Skill skill)
    {
        if(skill == this)
            return;

        if(skill instanceof SkillParentInterface ps)
            vaultFixes$nestedBranches.add(ps);

        if(skill instanceof TickingSkill ts)
            vaultFixes$tickingSkills.add(ts);
    }
    @Unique
    void VaultFixes$overrideSkills() {
        if(!vaultFixes$tickingSkills.isEmpty()) vaultFixes$tickingSkills.clear();
        if(!vaultFixes$nestedBranches.isEmpty()) vaultFixes$nestedBranches.clear();

        vaultFixes$notifyList.getOnRemove().unregister(this::VaultFixes$skillsOnRemove);
        vaultFixes$notifyList.clear();
        vaultFixes$notifyList.getOnRemove().unregister(this::VaultFixes$skillsOnRemove);
        vaultFixes$notifyList.addAll(specializations);
        specializations = vaultFixes$notifyList;
    }
    @Override
    public void vaultFixes$tickChildren(SkillContext context){
        if (vaultFixes$notifyList != specializations)
            if(specializations == null)
                return;
            else
                VaultFixes$overrideSkills();

        vaultFixes$tickingSkills.forEach(skill -> skill.onTick(context));
        vaultFixes$nestedBranches.forEach(parent -> parent.vaultFixes$tickChildren(context));
    }
    @Override
    public void vaultFixes$iterateTickable(Consumer<TickingSkill> lambda){
        vaultFixes$tickingSkills.forEach(lambda);
        vaultFixes$nestedBranches.forEach(parent -> parent.vaultFixes$iterateTickable(lambda));
    }
}
