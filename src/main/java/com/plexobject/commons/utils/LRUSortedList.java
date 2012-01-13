package com.plexobject.commons.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import com.plexobject.commons.Pair;

public class LRUSortedList<T> implements List<T> {
	private static final Logger LOGGER = Logger.getLogger(LRUSortedList.class);
	private final int max;
	private final Comparator<T> comparator;

	private final List<Pair<Long, T>> list = new ArrayList<Pair<Long, T>>();
	private final List<Pair<Long, Integer>> timestamps = new ArrayList<Pair<Long, Integer>>();

	// comparator to sort by timestamp
	private static final Comparator<Pair<Long, Integer>> CMP = new Comparator<Pair<Long, Integer>>() {
		@Override
		public int compare(Pair<Long, Integer> first, Pair<Long, Integer> second) {
			if (first.getFirst() < second.getFirst()) {
				return -1;
			} else if (first.getFirst() > second.getFirst()) {
				return 1;
			} else {
				return 0;
			}
		}
	};

	public LRUSortedList(int max, Comparator<T> comparator) {
		this.max = max;
		this.comparator = comparator;
	}

	@Override
	public boolean add(T e) {
		if (list.size() > max) {
			removeOldest();
		}
		// add object
		long timestamp = System.nanoTime();
		int insertionIdx = Collections.binarySearch(this, e, comparator);
		if (insertionIdx < 0) {// not found
			insertionIdx = (-insertionIdx) - 1;
			list.add(insertionIdx, new Pair<Long, T>(timestamp, e));
		} else {
			// found
			list.set(insertionIdx, new Pair<Long, T>(timestamp, e));
		}

		// as timestamps are sorted, we just remove the oldest (first)
		if (timestamps.size() > max) {
			timestamps.remove(0);
		}
		// update timestamp
		Pair<Long, Integer> t = new Pair<Long, Integer>(timestamp, insertionIdx);
		timestamps.add(t);
		return true;
	}

	@Override
	public void add(int index, T element) {
		throw new UnsupportedOperationException(
				"can't add element at arbitrary index, must use add to keep sorted order");
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		for (T e : c) {
			add(e);
		}
		return c.size() > 0;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException(
				"can't add element at arbitrary index, must use addAll to keep sorted order");
	}

	@Override
	public void clear() {
		list.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object e) {
		if (e == null) {
			return false;
		}
		try {
			return Collections.binarySearch(this, (T) e, comparator) >= 0;
		} catch (ClassCastException ex) {
			LOGGER.error("Unexpected type for contains "
					+ e.getClass().getName() + ": " + e);
			return false;
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object e : c) {
			if (!contains(e)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public T get(int index) {
		Pair<Long, T> e = list.get(index);
		return e != null ? e.getSecond() : null;
	}

	public T get(Object e) {
		int ndx = indexOf(e);
		if (ndx >= 0) {
			return get(ndx);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int indexOf(Object e) {
		try {
			return Collections.binarySearch(this, (T) e, comparator);
		} catch (ClassCastException ex) {
			LOGGER.error("Unexpected type for get " + e.getClass().getName()
					+ ": " + e);
			return -1;
		}
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		final Iterator<Pair<Long, T>> it = list.iterator();
		return new Iterator<T>() {

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public T next() {
				Pair<Long, T> e = it.next();
				return e.getSecond();
			}

			@Override
			public void remove() {
				it.remove();
			}
		};
	}

	@Override
	public int lastIndexOf(Object o) {
		for (int i = list.size() - 1; i >= 0; i--) {
			T e = get(i);
			if (e.equals(o)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public ListIterator<T> listIterator() {
		final ListIterator<Pair<Long, T>> it = list.listIterator();
		return buildListIterator(it);
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		final ListIterator<Pair<Long, T>> it = list.listIterator(index);
		return buildListIterator(it);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object e) {
		try {
			int ndx = Collections.binarySearch(this, (T) e, comparator);
			if (ndx >= 0) {
				remove(ndx);
				return true;
			} else {
				return false;
			}

		} catch (ClassCastException ex) {
			LOGGER.error("Unexpected type for remove " + e.getClass().getName()
					+ ": " + e);
			return false;
		}
	}

	@Override
	public T remove(int index) {
		Pair<Long, T> e = list.remove(index);
		Pair<Long, Integer> t = new Pair<Long, Integer>(e.getFirst(), 0);

		int insertionIdx = Collections.binarySearch(timestamps, t, CMP);
		if (insertionIdx >= 0) {
			timestamps.remove(insertionIdx);
		}
		return e != null ? e.getSecond() : null;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean all = true;
		for (Object e : c) {
			all = all && remove(e);
		}
		return all;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = false;
		Iterator<?> it = c.iterator();
		while (it.hasNext()) {
			Object e = it.next();
			if (!contains(e)) {
				it.remove();
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public T set(int index, T element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		List<T> tlist = new ArrayList<T>();
		List<Pair<Long, T>> plist = list.subList(fromIndex, toIndex);
		for (Pair<Long, T> e : plist) {
			tlist.add(e.getSecond());
		}
		return tlist;
	}

	@Override
	public Object[] toArray() {
		return subList(0, list.size()).toArray();
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] a) {
		return subList(0, list.size()).toArray(a);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Iterator<T> it = iterator();
		while (it.hasNext()) {
			sb.append(it.next() + ", ");
		}
		return sb.toString();
	}

	private void removeOldest() {
		timestamps.remove(timestamps.size() - 1);
	}

	private ListIterator<T> buildListIterator(
			final ListIterator<Pair<Long, T>> it) {
		return new ListIterator<T>() {

			@Override
			public void add(T e) {
				it.add(new Pair<Long, T>(System.nanoTime(), e));
			}

			@Override
			public boolean hasNext() {
				return it.hasNext();

			}

			@Override
			public boolean hasPrevious() {
				return it.hasPrevious();

			}

			@Override
			public T next() {
				Pair<Long, T> e = it.next();
				return e.getSecond();
			}

			@Override
			public int nextIndex() {
				return it.nextIndex();

			}

			@Override
			public T previous() {
				Pair<Long, T> e = it.previous();
				return e.getSecond();
			}

			@Override
			public int previousIndex() {
				return it.previousIndex();

			}

			@Override
			public void remove() {
				it.remove();

			}

			@Override
			public void set(T e) {
				it.set(new Pair<Long, T>(System.nanoTime(), e));

			}
		};
	}

}
