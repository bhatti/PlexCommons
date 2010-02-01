package com.plexobject.commons.cache;

public interface CacheLoader<K, V> {
    V get(K key);
}
