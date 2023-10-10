package net.cdnbcn.vaultfixes.bounty

import iskallia.vault.bounty.task.Task
import iskallia.vault.world.data.BountyData
import net.cdnbcn.vaultfixes.mixin_interfaces.BountyDataMixinInterface
import net.cdnbcn.vaultfixes.mixin_interfaces.TaskMixinInterface
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import java.util.function.Consumer
import java.util.stream.Stream

object TaskHelpers {
    fun <E> processEvent(player: ServerPlayer?, taskType: ResourceLocation?, event: E) {
        processEvent(player, taskType, event) {
            task: Task<*> -> task.increment(1.0)
        }
    }

    fun <E> processEvent(
        player: ServerPlayer?,
        taskType: ResourceLocation?,
        event: E,
        doIncrement: Consumer<Task<*>>
    ) {
        val bountyData = BountyData.get() as BountyDataMixinInterface
        val iter = Stream.concat(
                bountyData.`vaultFixes$getAllLegendaryByIdAsStream`<Task<*>>(player, taskType),
                bountyData.`vaultFixes$getAllActiveByIdAsStream`(player, taskType)
            )
            .filter { t: Task<*> -> !t.isComplete }
            .iterator()

        for(task: Task<*> in iter) {
            if (!task.validate(player, event))
                continue

            doIncrement.accept(task)
            if (task.isComplete) (task as TaskMixinInterface).`vaultFixes$callComplete`(player)
            break
        }
    }
}