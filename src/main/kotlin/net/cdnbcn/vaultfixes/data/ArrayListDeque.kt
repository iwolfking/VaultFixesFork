package net.cdnbcn.vaultfixes.data

import java.util.*
import kotlin.collections.ArrayList

class ArrayListDeque<T>(private val inner: ArrayList<T>) : List<T>, Deque<T> {

    override val size: Int
        get() = inner.size

    constructor(size: Int) : this(ArrayList(size)) {}
    constructor() : this(ArrayList()) {}

    override fun iterator(): MutableIterator<T> { return inner.iterator() }
    override fun listIterator(): ListIterator<T> { return inner.listIterator() }
    override fun listIterator(index: Int): ListIterator<T> { return inner.listIterator(index) }
    override fun descendingIterator(): MutableIterator<T> { return ListDescendingMutableIterator(inner) }
    override fun contains(element: T): Boolean { return inner.contains(element) }
    override fun containsAll(elements: Collection<T>): Boolean { return inner.containsAll(elements) }
    override fun get(index: Int): T  { return inner[index] }
    override fun add(element: T): Boolean { return inner.add(element) }
    override fun addAll(elements: Collection<T>): Boolean { return inner.addAll(elements)  }
    override fun clear() { inner.clear() }
    override fun remove(): T {
        val elem: T = inner[0]
        inner.removeAt(0)
        return elem
    }
    override fun retainAll(elements: Collection<T>): Boolean { return inner.retainAll(elements.toSet()) }
    override fun removeAll(elements: Collection<T>): Boolean { return inner.removeAll(elements.toSet()) }
    override fun remove(element: T): Boolean { return inner.remove(element) }
    override fun subList(fromIndex: Int, toIndex: Int): List<T> { return inner.subList(fromIndex, toIndex) }
    override fun lastIndexOf(element: T): Int { return inner.lastIndexOf(element) }
    override fun isEmpty(): Boolean { return inner.isEmpty() }
    override fun poll(): T? { return if (this.isEmpty()) { null } else { remove() } }
    override fun element(): T { return inner[0] }
    override fun peek(): T? { return if (this.isEmpty()) { null } else { element() }}
    override fun removeFirst(): T { return remove() }
    override fun removeLast(): T {
        val elem: T = inner[inner.size-1]
        inner.removeAt(inner.size-1)
        return elem
    }
    override fun pollFirst(): T? { return poll() }
    override fun pollLast(): T? { return if (this.isEmpty()) { null } else { removeLast() } }
    override fun getFirst(): T { return element() }
    override fun getLast(): T { return inner[inner.size-1] }
    override fun peekFirst(): T? { return peek() }
    override fun peekLast(): T? { return if (this.isEmpty()) { null } else { last } }
    override fun removeFirstOccurrence(o: Any?): Boolean { return remove(o) }
    override fun removeLastOccurrence(o: Any?): Boolean {
        var x: Int = inner.size-1
        while(x > -1)
        {
            if(inner[x] == o)
            {
                inner.removeAt(x)
                return true
            }
            ++x
        }
        return false
    }
    override fun pop(): T { return removeFirst() }
    override fun push(e: T) { return inner.add(0, e) }
    override fun offerLast(e: T): Boolean { inner.add(e); return true }
    override fun offerFirst(e: T): Boolean { inner.add(0, e); return true }
    override fun addLast(e: T) { offerLast(e) }
    override fun addFirst(e: T) { push(e) }
    override fun offer(e: T): Boolean { offerLast(e); return true }
    override fun indexOf(element: T): Int { return inner.indexOf(element) }

}