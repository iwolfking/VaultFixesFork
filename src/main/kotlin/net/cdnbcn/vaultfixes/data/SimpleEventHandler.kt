package net.cdnbcn.vaultfixes.data


class SimpleEventHandler {
    private val eventList: MutableList<Runnable> = mutableListOf()

    fun unregister(listener: Runnable): Boolean {
        return eventList.remove(listener)
    }
    fun register(listener: Runnable) {
        eventList.add(listener)
    }

    fun fire(){
        for(event in eventList)
        {
            try {
                event.run()
            } catch(_: Exception) {}
        }
    }
}