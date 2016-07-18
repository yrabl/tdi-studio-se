// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.components.hashfile.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores and provides MemoreCaches by key. It also supports linked caches
 */
public class MemoryCaches {

    /**
     * Instance of this singleton
     */
    private static MemoryCaches instance;
    
    private Map<String, Cache> cacheMap;
    
    public static TalendMultiThreadLockMap resourceLockMap = new TalendMultiThreadLockMap();
    
    private MemoryCaches() {
        cacheMap = Collections.synchronizedMap(new HashMap<String, Cache>());
    }
    
    /**
     * Returns instance of this singleton
     * 
     * @return instance
     */
    public static MemoryCaches getInstance() {
        if (instance == null) {
            instance = new MemoryCaches();
        }
        return instance;
    }
    
    public <T> Cache<T> getMemoryCache(String key) {
        Cache<T> cache = cacheMap.get(key);
        return cache;
    }
    
    public <T> void addMemoryCache(String key, Cache<T> cache) {
        cacheMap.put(key, cache);
    }
    
    /**
     * Removes cache, which corresponds to specified key.
     * CacheMap can contain several keys, which points to the same cache (linked caches).
     * Method removes all pairs with value, to which points specified key
     * 
     * @param key cache key
     */
    public void clearCache(String key){
        Cache cache = cacheMap.get(key);
        // removes linked caches
        cacheMap.values().removeAll(Collections.singleton(cache));
    }
    
    public static class TalendMultiThreadLockMap {

        private Map<Object, Object> tMultiTheadLockMap = new HashMap<Object, Object>();

        public synchronized Object get(Object key) {
            if (tMultiTheadLockMap.get(key) == null) {
                tMultiTheadLockMap.put(key, new Object());
            }
            return tMultiTheadLockMap.get(key);
        }

        public synchronized void remove(Object key) {
            tMultiTheadLockMap.remove(key);
        }
    }

}
