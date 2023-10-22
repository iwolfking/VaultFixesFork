package net.cdnbcn.vaultfixes.data


class NotifyArrayList<T> : ArrayList<T>() {
    val onAdd : SimpleEventHandler2<T> = SimpleEventHandler2()
    val onRemove : SimpleEventHandler2<T> = SimpleEventHandler2()

    override fun clear() {
        super.forEach(onRemove::fire)
        super.clear()
    }
    override fun addAll(elements: Collection<T>): Boolean {
        return if (super.addAll(elements)){
            elements.forEach(onAdd::fire)
            true
        } else false
    }
    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        return if (super.addAll(index, elements)){
            elements.forEach(onAdd::fire)
            true
        } else false
    }
    override fun add(index: Int, element: T) {
        super.add(index, element)
        onAdd.fire(element)
    }
    override fun add(element: T): Boolean {
        return if(super.add(element)) {
            onAdd.fire(element)
            true
        } else false
    }
    override fun removeAt(index: Int): T {
        val ret = super.removeAt(index)
        onRemove.fire(ret)
        return ret
    }
    override fun set(index: Int, element: T): T {
        val ret = super.set(index, element)
        if(ret != null)
            onRemove.fire(ret)
        onAdd.fire(element)
        return ret
    }
    override fun retainAll(elements: Collection<T>): Boolean {
        val potentialRemoves = elements.stream().filter { !elements.contains(it) }.toList()
        val optimizedSet = if(elements is Set<T>) elements else elements.toSet()
        return if (super.retainAll(optimizedSet)) {
            potentialRemoves.forEach(onRemove::fire)
            true
        } else false
    }
    override fun removeAll(elements: Collection<T>): Boolean {
        val potentialRemoves = elements.stream().filter { !elements.contains(it) }.toList()
        val optimizedSet = if(elements is Set<T>) elements else elements.toSet()
        return if (super.removeAll(optimizedSet)) {
            potentialRemoves.forEach(onRemove::fire)
            true
        } else false
    }
    override fun remove(element: T): Boolean {
        return if (super.remove(element)) {
            onRemove.fire(element)
            true
        } else false
    }
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> { throw Exception("Not Supported!") }
}