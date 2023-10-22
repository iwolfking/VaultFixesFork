package net.cdnbcn.vaultfixes.data

import java.util.function.Consumer


class SimpleEventHandler2<T> {
    private val eventList: MutableList<Consumer<T>> = mutableListOf()

    fun unregister(listener: Consumer<T>): Boolean {
        return eventList.remove(listener)
    }
    fun register(listener: Consumer<T>) {
        eventList.add(listener)
    }

    fun fire(args: T){
        for(event in eventList)
        {
            try {
                event.accept(args)
            } catch(_: Exception) {}
        }
    }
}