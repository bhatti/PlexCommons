package com.plexobject.commons.cache;

public interface CacheDisposer<T> {
    void dispose(T object);
}
