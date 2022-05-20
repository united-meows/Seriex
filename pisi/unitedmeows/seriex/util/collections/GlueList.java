package pisi.unitedmeows.seriex.util.collections;

import static java.lang.Math.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

public class GlueList<T> extends AbstractList<T> implements Cloneable, Serializable {

	private static final long serialVersionUID = 4629641037522698945L;
	transient Node<T> first;
	transient Node<T> last;
	int size;
	int initialCapacity;
	private static final int DEFAULT_CAPACITY = 10;
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

	public GlueList() {
		final Node<T> initNode = new Node<>(null, null, 0, DEFAULT_CAPACITY);
		first = initNode;
		last = initNode;
	}

	public GlueList(final int initialCapacity) {
		this.initialCapacity = initialCapacity > MAX_ARRAY_SIZE ? MAX_ARRAY_SIZE : initialCapacity;
		final Node<T> initNode = new Node<>(null, null, 0, initialCapacity);
		first = initNode;
		last = initNode;
	}

	public GlueList(final Collection<? extends T> c) {
		Objects.requireNonNull(c);
		final Object[] arr = c.toArray();
		final int len = arr.length;
		if (len != 0) {
			final Node<T> initNode = new Node<>(null, null, 0, len);
			first = initNode;
			last = initNode;
			System.arraycopy(arr, 0, last.elementData, 0, len);
			last.elementDataPointer += len;
		} else {
			final Node<T> initNode = new Node<>(null, null, 0, DEFAULT_CAPACITY);
			first = initNode;
			last = initNode;
		}
		modCount++;
		size += len;
	}

	@Override
	public boolean add(final T element) {
		final Node<T> l = last;
		if (l.isAddable()) {
			l.add(element);
		} else {
			final Node<T> newNode = new Node<>(l, null, size);
			newNode.add(element);
			last = newNode;
			l.next = last;
		}
		modCount++;
		size++;
		return true;
	}

	@Override
	public void add(final int index, final T element) {
		rangeCheckForAdd(index);
		Node<T> node = getNodeForAdd(index);
		if (node == null) {
			final Node<T> l = last;
			final Node<T> newNode = new Node<>(l, null, size);
			last = newNode;
			l.next = last;
			node = newNode;
		}
		// if it is last and has extra space for element...
		if (node == last && node.elementData.length - node.elementDataPointer > 0) {
			final int nodeArrIndex = index - node.startingIndex;
			System.arraycopy(node.elementData, nodeArrIndex, node.elementData, nodeArrIndex + 1, node.elementDataPointer - nodeArrIndex);
			node.elementData[nodeArrIndex] = element;
			if (nodeArrIndex > 0) {
				System.arraycopy(node.elementData, 0, node.elementData, 0, nodeArrIndex);
			}
			node.elementDataPointer++;
		} else {
			final int newLen = node.elementData.length + 1;
			final T[] newElementData = (T[]) new Object[newLen];
			final int nodeArrIndex = index - node.startingIndex;
			System.arraycopy(node.elementData, nodeArrIndex, newElementData, nodeArrIndex + 1, node.elementDataPointer - nodeArrIndex);
			newElementData[nodeArrIndex] = element;
			if (nodeArrIndex > 0) {
				System.arraycopy(node.elementData, 0, newElementData, 0, nodeArrIndex);
			}
			node.elementData = newElementData;
			node.endingIndex++;
			node.elementDataPointer++;
		}
		updateNodesAfterAdd(node);
		modCount++;
		size++;
	}

	private void rangeCheckForAdd(final int index) {
		if (index > size || index < 0) throw new ArrayIndexOutOfBoundsException(index);
	}

	private void updateNodesAfterAdd(final Node<T> nodeFrom) {
		for (Node<T> node = nodeFrom.next; node != null; node = node.next) {
			node.startingIndex++;
			node.endingIndex++;
		}
	}

	@Override
	public boolean addAll(final Collection<? extends T> c) {
		Objects.requireNonNull(c);
		final Object[] collection = c.toArray();
		final int len = collection.length;
		if (len == 0) return false;
		if (size == 0) {
			if (initialCapacity >= len) {
				System.arraycopy(collection, 0, last.elementData, 0, len);
			} else {
				last.elementData = Arrays.copyOf((T[]) collection, len);
				last.endingIndex = len - 1;
			}
			last.elementDataPointer += len;
			modCount++;
			size += len;
			return true;
		}
		final int elementDataLen = last.elementData.length;
		final int elementSize = last.elementDataPointer;
		final int remainedStorage = elementDataLen - elementSize;
		if (remainedStorage == 0) {
			final Node<T> l = last;
			final int newLen = size >>> 1;
			final int initialLen = len > newLen ? len : newLen;
			final Node<T> newNode = new Node<>(l, null, size, initialLen);
			System.arraycopy(collection, 0, newNode.elementData, 0, len);
			newNode.elementDataPointer += len;
			last = newNode;
			l.next = last;
			modCount++;
			size += len;
			return true;
		}
		if (len <= remainedStorage) {
			System.arraycopy(collection, 0, last.elementData, elementSize, len);
			last.elementDataPointer += len;
			modCount++;
			size += len;
			return true;
		}
		if (len > remainedStorage) {
			System.arraycopy(collection, 0, last.elementData, elementSize, remainedStorage);
			last.elementDataPointer += remainedStorage;
			size += remainedStorage;
			final int newLen = size >>> 1;
			final int remainedDataLen = len - remainedStorage;
			final int initialLen = newLen > remainedDataLen ? newLen : remainedDataLen;
			final Node<T> l = last;
			final Node<T> newNode = new Node<>(l, null, size, initialLen);
			System.arraycopy(collection, remainedStorage, newNode.elementData, 0, remainedDataLen);
			newNode.elementDataPointer += remainedDataLen;
			last = newNode;
			l.next = last;
			modCount++;
			size += remainedDataLen;
			return true;
		}
		return false;
	}

	@Override
	public T set(final int index, final T element) {
		rangeCheck(index);
		final Node<T> node = getNode(index);
		final int nodeArrIndex = index - node.startingIndex;
		final T oldValue = node.elementData[nodeArrIndex];
		node.elementData[nodeArrIndex] = element;
		return oldValue;
	}

	@Override
	public T get(final int index) {
		rangeCheck(index);
		final Node<T> node = getNode(index);
		return node.elementData[index - node.startingIndex];
	}

	@Override
	public int indexOf(final Object o) {
		int index = 0;
		if (o == null) {
			for (Node<T> node = first; node != null; node = node.next) {
				for (int i = 0; i < node.elementDataPointer; i++) {
					if (node.elementData[i] == null) return index;
					index++;
				}
			}
		} else {
			for (Node<T> node = first; node != null; node = node.next) {
				for (int i = 0; i < node.elementDataPointer; i++) {
					if (o.equals(node.elementData[i])) return index;
					index++;
				}
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(final Object o) {
		int index = size - 1;
		if (o == null) {
			for (Node<T> node = last; node != null; node = node.pre) {
				for (int i = node.elementDataPointer - 1; i >= 0; i--) {
					if (node.elementData[i] == null) return index;
					index--;
				}
			}
		} else {
			for (Node<T> node = last; node != null; node = node.pre) {
				for (int i = node.elementDataPointer - 1; i >= 0; i--) {
					if (o.equals(node.elementData[i])) return index;
					index--;
				}
			}
		}
		return -1;
	}

	@Override
	public boolean contains(final Object o) {
		return indexOf(o) != -1;
	}

	@Override
	public T remove(final int index) {
		rangeCheck(index);
		Node<T> node;
		if (size == 2 && first != last) {
			final Node<T> newNode = new Node<>(null, null, 0, 2);
			newNode.add(first.elementData[0]);
			newNode.add(last.elementData[0]);
			node = first = last = newNode;
		} else {
			node = getNode(index);
		}
		final T[] elementData = node.elementData;
		final int elementSize = node.elementDataPointer;
		final int nodeArrIndex = index - node.startingIndex;
		final T oldValue = elementData[nodeArrIndex];
		final int numMoved = elementSize - nodeArrIndex - 1;
		if (numMoved > 0) {
			System.arraycopy(node.elementData, nodeArrIndex + 1, node.elementData, nodeArrIndex, numMoved);
		}
		if (first == last || node == last) {
			node.elementData[elementSize - 1] = null;
		} else {
			node.elementData = Arrays.copyOf(node.elementData, elementSize - 1);
			node.endingIndex = --node.endingIndex < 0 ? 0 : node.endingIndex;
		}
		node.elementDataPointer--;
		updateNodesAfterRemove(node);
		if (node.elementDataPointer == 0 && first != last) {
			final Node<T> next = node.next;
			final Node<T> prev = node.pre;
			if (prev == null) {
				first = next;
			} else {
				prev.next = next;
				node.pre = null;
			}
			if (next == null) {
				last = prev;
			} else {
				next.pre = prev;
				node.next = null;
			}
			node.elementData = null;
		}
		size--;
		modCount++;
		return oldValue;
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		Objects.requireNonNull(c);
		final Object[] arr = c.toArray();
		if (arr.length == 0) return false;
		boolean isModified = false;
		for (final Object o : arr) {
			isModified |= remove(o);
		}
		return isModified;
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		Objects.requireNonNull(c);
		final Object[] arr = c.toArray();
		if (arr.length == 0) return false;
		boolean isModified = false;
		final Object[] elements = toArray();
		for (final Object element : elements) if (!c.contains(element)) {
			isModified |= remove(element);
		}
		return isModified;
	}

	@Override
	public boolean remove(final Object o) {
		final int index = indexOf(o);
		if (index != -1) {
			remove(index);
			return true;
		}
		return false;
	}

	private void updateNodesAfterRemove(final Node<T> fromNode) {
		for (Node<T> node = fromNode.next; node != null; node = node.next) {
			node.startingIndex = --node.startingIndex < 0 ? 0 : node.startingIndex;
			node.endingIndex = --node.endingIndex < 0 ? 0 : node.endingIndex;
		}
	}

	private Node<T> getNode(final int index) {
		final int firstStartingIndex = first.startingIndex;
		final int firstEndingIndex = first.endingIndex;
		final int firstMinDistance = min(abs(index - firstStartingIndex), abs(index - firstEndingIndex));
		final int lastStartingIndex = last.startingIndex;
		final int lastEndingIndex = last.endingIndex;
		final int lastMinDistance = min(abs(index - lastStartingIndex), abs(index - lastEndingIndex));
		if (firstMinDistance <= lastMinDistance) {
			Node<T> node = first;
			do {
				if (node.startingIndex <= index && index <= node.endingIndex) return node;
				node = node.next;
			} while (true);
		} else {
			Node<T> node = last;
			do {
				if (node.startingIndex <= index && index <= node.endingIndex) return node;
				node = node.pre;
			} while (true);
		}
	}

	private Node<T> getNodeForAdd(final int index) {
		if (index == size && (last.startingIndex > index || index > last.endingIndex)) return null;
		return getNode(index);
	}

	private void rangeCheck(final int index) {
		if (index >= size || index < 0) throw new ArrayIndexOutOfBoundsException(index);
	}

	@Override
	public void clear() {
		for (Node<T> node = first; node != null;) {
			final Node<T> next = node.next;
			node.next = null;
			node.pre = null;
			node.elementData = null;
			node = next;
		}
		first = last = null;
		final int capacity = min(MAX_ARRAY_SIZE, max(size, max(initialCapacity, DEFAULT_CAPACITY)));
		final Node<T> initNode = new Node<>(null, null, 0, capacity);
		initialCapacity = capacity;
		first = initNode;
		last = initNode;
		modCount++;
		size = 0;
	}

	public void trimToSize() {
		final int pointer = last.elementDataPointer;
		final int arrLen = last.elementData.length;
		if (pointer < arrLen && arrLen > 2) {
			if (pointer < 2) {
				last.elementData = Arrays.copyOf(last.elementData, 2);
				last.endingIndex -= arrLen - 2;
			} else {
				last.elementData = Arrays.copyOf(last.elementData, pointer);
				last.endingIndex -= arrLen - pointer;
			}
		}
	}

	@Override
	public List<T> subList(final int fromIndex, final int toIndex) {
		return super.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		final Object[] objects = new Object[size];
		int i = 0;
		for (Node<T> node = first; node != null; node = node.next) {
			final int len = node.elementDataPointer;
			if (len > 0) {
				System.arraycopy(node.elementData, 0, objects, i, len);
			}
			i += len;
		}
		return objects;
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		return (T[]) Arrays.copyOf(toArray(), size, a.getClass());
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public Iterator<T> iterator() {
		return new Itr();
	}

	private class Itr implements Iterator<T> {

		Node<T> node = first;
		int i = 0;// inner-array index
		int j = 0;// total index -> cursor
		int lastReturn = -1;
		int expectedModCount = modCount;
		int elementDataPointer = node.elementDataPointer;

		@Override
		public boolean hasNext() {
			return j != size;
		}

		@Override
		public T next() {
			checkForComodification();
			if (j >= size) throw new NoSuchElementException();
			if (j >= last.endingIndex + 1) throw new ConcurrentModificationException();
			if (j == 0) {// it's for listIterator.when node becomes null.
				node = first;
				elementDataPointer = node.elementDataPointer;
				i = 0;
			}
			final T val = node.elementData[i++];
			if (i >= elementDataPointer) {
				node = node.next;
				i = 0;
				elementDataPointer = node != null ? node.elementDataPointer : 0;
			}
			lastReturn = j++;
			return val;
		}

		@Override
		public void remove() {
			if (lastReturn < 0) throw new IllegalStateException();
			checkForComodification();
			try {
				GlueList.this.remove(lastReturn);
				j = lastReturn;
				lastReturn = -1;
				i = --i < 0 ? 0 : i;
				elementDataPointer = node != null ? node.elementDataPointer : 0;
				expectedModCount = modCount;
			}
			catch (final IndexOutOfBoundsException e) {
				throw new ConcurrentModificationException();
			}
		}

		void checkForComodification() {
			if (modCount != expectedModCount) throw new ConcurrentModificationException();
		}
	}

	@Override
	public ListIterator<T> listIterator(final int index) {
		checkPositionIndex(index);
		return new ListItr(index);
	}

	private void checkPositionIndex(final int index) {
		if (index < 0 || index > size) throw new ArrayIndexOutOfBoundsException(index);
	}

	@Override
	public ListIterator<T> listIterator() {
		return new ListItr(0);
	}

	private class ListItr extends Itr implements ListIterator<T> {

		public ListItr(final int index) {
			node = index == size ? last : getNode(index);
			j = index;
			i = index - node.startingIndex;
			elementDataPointer = node.elementDataPointer;
		}

		@Override
		public boolean hasPrevious() {
			return j != 0;
		}

		@Override
		public T previous() {
			checkForComodification();
			final int temp = j - 1;
			if (temp < 0) throw new NoSuchElementException();
			if (temp >= last.endingIndex + 1) throw new ConcurrentModificationException();
			if (j == size) {
				node = last;
				elementDataPointer = node.elementDataPointer;
				i = elementDataPointer;
			}
			final int index = j - node.startingIndex;
			if (index == 0) {
				node = node.pre;
				elementDataPointer = node.elementDataPointer;
				i = elementDataPointer;
			}
			final T val = node.elementData[--i];
			if (i < 0) {
				node = node.pre;
				i = node != null ? node.elementDataPointer : 0;
			}
			j = temp;
			lastReturn = j;
			return val;
		}

		@Override
		public int nextIndex() {
			return j;
		}

		@Override
		public int previousIndex() {
			return j - 1;
		}

		@Override
		public void set(final T t) {
			if (lastReturn < 0) throw new IllegalStateException();
			checkForComodification();
			try {
				GlueList.this.set(lastReturn, t);
			}
			catch (final IndexOutOfBoundsException e) {
				throw new ConcurrentModificationException();
			}
		}

		@Override
		public void add(final T t) {
			checkForComodification();
			try {
				final int temp = j;
				GlueList.this.add(temp, t);
				j = temp + 1;
				lastReturn = -1;
				i++;
				elementDataPointer = node != null ? node.elementDataPointer : 0;
				expectedModCount = modCount;
			}
			catch (final IndexOutOfBoundsException e) {
				throw new ConcurrentModificationException();
			}
		}
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Object clone() {
		try {
			final GlueList<T> clone = (GlueList<T>) super.clone();
			clone.first = clone.last = null;
			final int capacity = min(MAX_ARRAY_SIZE, max(clone.size, max(clone.initialCapacity, DEFAULT_CAPACITY)));
			final Node<T> initNode = new Node<>(null, null, 0, capacity);
			clone.initialCapacity = capacity;
			clone.first = clone.last = initNode;
			clone.modCount = 0;
			clone.size = 0;
			for (Node<T> node = first; node != null; node = node.next) {
				for (int i = 0; i < node.elementDataPointer; i++) {
					clone.add(node.elementData[i]);
				}
			}
			return clone;
		}
		catch (final CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	// what the fuck where is modCount updated?
	private void writeObject(final ObjectOutputStream s) throws IOException {
		final int expectedModCount = modCount;
		s.defaultWriteObject();
		s.writeInt(size);
		for (Node<T> node = first; node != null; node = node.next) {
			for (int i = 0; i < node.elementDataPointer; i++) {
				s.writeObject(node.elementData[i]);
			}
		}
		if (modCount != expectedModCount) throw new ConcurrentModificationException();
	}

	private void readObject(final ObjectInputStream s) throws IOException,ClassNotFoundException {
		clear();
		s.defaultReadObject();
		for (int i = 0; i < s.readInt(); i++) {
			last.add((T) s.readObject());
		}
	}

	static class Node<T> {

		Node<T> pre;
		Node<T> next;
		int listSize;
		int startingIndex;
		int endingIndex;
		T[] elementData;
		int elementDataPointer;

		Node(final Node<T> pre, final Node<T> next, final int listSize) {
			this.pre = pre;
			this.next = next;
			this.listSize = listSize;
			this.elementData = (T[]) new Object[listSize >>> 1];
			this.startingIndex = listSize;
			this.endingIndex = listSize + elementData.length - 1;
		}

		Node(final Node<T> pre, final Node<T> next, final int listSize, final int initialCapacity) {
			this.pre = pre;
			this.next = next;
			this.listSize = listSize;
			this.elementData = createElementData(initialCapacity);
			this.startingIndex = listSize;
			this.endingIndex = listSize + elementData.length - 1;
		}

		T[] createElementData(final int capacity) {
			if (capacity == 0 || capacity == 1) return (T[]) new Object[DEFAULT_CAPACITY];
			if (capacity > 1) return (T[]) new Object[capacity];
			else throw new IllegalArgumentException("Illegal Capacity: " + capacity);
		}

		boolean isAddable() {
			return elementDataPointer < elementData.length;
		}

		void add(final T element) {
			elementData[elementDataPointer++] = element;
		}

		@Override
		public String toString() {
			return String.format("[sIndex: %d - eIndex: %d | elementDataPointer: %d | elementDataLength: %d]", startingIndex, endingIndex, elementDataPointer, elementData.length);
		}
	}
}
