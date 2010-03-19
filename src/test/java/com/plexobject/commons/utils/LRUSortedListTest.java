package com.plexobject.commons.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LRUSortedListTest {
    static final Logger LOGGER = Logger.getLogger(LRUSortedListTest.class);

    LRUSortedList<String> list;

    @Before
    public void setUp() throws Exception {
        list = new LRUSortedList<String>(100, String.CASE_INSENSITIVE_ORDER);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testAddT() {
        list.add("test");
        Assert.assertEquals(1, list.size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public final void testAddIntT() {
        list.add(1, "test");

    }

    @Test
    public final void testAddAllCollectionOfQextendsT() {
        list.addAll(Arrays.asList("one", "two", "three"));
        Assert.assertEquals(3, list.size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public final void testAddAllIntCollectionOfQextendsT() {
        list.addAll(0, Arrays.asList("one", "two", "three"));
    }

    @Test
    public final void testClear() {
        list.add("test");
        Assert.assertEquals(1, list.size());
        list.clear();
        Assert.assertEquals(0, list.size());
    }

    @Test
    public final void testContains() {
        list.add("test");
        Assert.assertTrue(list.contains("test"));
    }

    @Test
    public final void testContainsAll() {
        list.addAll(Arrays.asList("one", "two", "three"));
        Assert.assertTrue(list
                .containsAll(Arrays.asList("one", "two", "three")));
        Assert.assertFalse(list.containsAll(Arrays.asList("one", "two",
                "three", "four")));
    }

    @Test
    public final void testGetInt() {
        list.add("test");
        Assert.assertEquals("test", list.get(0));
    }

    @Test
    public final void testGetObject() {
        list.add("test");
        Assert.assertEquals("test", list.get("test"));
    }

    @Test
    public final void testIndexOf() {
        list.add("test");
        Assert.assertEquals(0, list.indexOf("test"));
    }

    @Test
    public final void testIsEmpty() {
        Assert.assertTrue(list.isEmpty());
        list.add("test");
        Assert.assertFalse(list.isEmpty());
    }

    @Test
    public final void testIterator() {
        boolean one = false, two = false, three = false;
        list.addAll(Arrays.asList("one", "two", "three"));
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String next = it.next();
            if (next.equals("one")) {
                one = true;
            } else if (next.equals("two")) {
                two = true;
            } else if (next.equals("three")) {
                three = true;
            }
        }
        Assert.assertTrue(one && two && three);
    }

    @Test
    public final void testLastIndexOf() {
        list.add("test");
        list.add("test");
        list.add("test");

        Assert.assertEquals(0, list.lastIndexOf("test"));
    }

    @Test
    public final void testListIterator() {
        boolean one = false, two = false, three = false;
        list.addAll(Arrays.asList("one", "two", "three"));
        ListIterator<String> it = list.listIterator();
        while (it.hasNext()) {
            String next = it.next();
            if (next.equals("one")) {
                one = true;
            } else if (next.equals("two")) {
                two = true;
            } else if (next.equals("three")) {
                three = true;
            }
        }
        Assert.assertTrue(one && two && three);
    }

    @Test
    public final void testListIteratorInt() {
        boolean one = false, two = false, three = false;
        list.addAll(Arrays.asList("one", "two", "three"));
        ListIterator<String> it = list.listIterator(0);
        while (it.hasNext()) {
            String next = it.next();
            if (next.equals("one")) {
                one = true;
            } else if (next.equals("two")) {
                two = true;
            } else if (next.equals("three")) {
                three = true;
            }
        }
        Assert.assertTrue(one && two && three);
    }

    @Test
    public final void testRemoveObject() {
        list.add("test");
        Assert.assertEquals(1, list.size());
        list.remove("test");
        Assert.assertEquals(0, list.size());
    }

    @Test
    public final void testRemoveInt() {
        list.add("test");
        Assert.assertEquals(1, list.size());
        list.remove(0);
        Assert.assertEquals(0, list.size());
    }

    @Test
    public final void testRemoveAll() {
        list.addAll(Arrays.asList("one", "two", "three"));
        Assert.assertEquals(3, list.size());
        list.removeAll(Arrays.asList("one", "two"));
        Assert.assertEquals(1, list.size());
    }

    @Test
    public final void testRetainAll() {
        list.addAll(Arrays.asList("one", "two", "three"));
        List<String> copy = new ArrayList<String>(list);
        copy.add("four");
        boolean changed = list.retainAll(copy);
        Assert.assertTrue(changed);
        Assert.assertEquals(3, copy.size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public final void testSet() {
        list.set(0, "test");
        Assert.assertEquals(1, list.size());
    }

    @Test
    public final void testSize() {
        list.add("test");
        Assert.assertEquals(1, list.size());
    }

    @Test
    public final void testSubList() {
        list.addAll(Arrays.asList("one", "two", "three"));
        List<String> sublist = list.subList(0, 2);
        Assert.assertEquals(2, sublist.size());
        Assert.assertEquals("one", sublist.get(0));
        Assert.assertEquals("three", sublist.get(1));
    }

    @Test
    public final void testToArray() {
        list.addAll(Arrays.asList("one", "two", "three"));

        Object[] arr = list.toArray();
        Assert.assertEquals(3, arr.length);
        Assert.assertEquals("one", arr[0]);
        Assert.assertEquals("three", arr[1]);
    }

    @Test
    public final void testToArrayTArray() {
        list.addAll(Arrays.asList("one", "two", "three"));

        Object[] arr = list.toArray(new Object[0]);
        Assert.assertEquals(3, arr.length);
        Assert.assertEquals("one", arr[0]);
        Assert.assertEquals("three", arr[1]);
    }

    @Test
    public final void testSort() throws InterruptedException {
        for (int i = 50; i >= 0; i--) {
            list.add(String.format("%05d", i));
            Thread.sleep(1);
        }
        for (int i = 0; i < 50; i++) {
            String v = list.get(String.format("%05d", i));
            Assert.assertNotNull("unexpected value for " + i + ", size "
                    + list.size() + ", list " + list, v);
            int num = Integer.parseInt(v);
            Assert.assertEquals(i, num);
        }
    }

    @Test
    public final void testAddGetTimings() throws InterruptedException {
        long started = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            list.add(String.valueOf(i));
            Thread.sleep(1);
        }
        long elapsed = System.currentTimeMillis() - started;
        LOGGER.info("Added 10000 in " + elapsed + " millis");
        started = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            list.get(String.valueOf(i));
        }
        elapsed = System.currentTimeMillis() - started;
        LOGGER.info("Retrieved 10000 in " + elapsed + " millis");

    }
}
