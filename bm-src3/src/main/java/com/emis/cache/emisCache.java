package com.emis.cache;

/**
 *  廣義的 Cache 的 Interface
 */

public interface emisCache
{
    String getName();
    boolean isExpired();
    Object getCache();
}