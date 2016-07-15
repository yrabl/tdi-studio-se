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
package org.talend.designer.components.hashfile.memory;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.talend.designer.components.hashfile.common.Cache;

/**
 * Memory {@link Cache}, which keeps only last record with specific key in case of key duplication 
 */
public class KeepLastMemoryCache<V> implements Cache<V> {

    /**
     * Stores last met records
     */
    private Set<V> lastMetCache;
    
    /**
     * Constructor creates synchronized map to store last records with certain key
     */
    public KeepLastMemoryCache() {
        lastMetCache = Collections.synchronizedSet(new LinkedHashSet<V>());
    }
    
    /**
     * {@inheritDoc}
     * 
     * Iterator must be manually synched by user
     */
    @Override
    public Iterator<V> iterator() {
        return new NotRemovingIterator<V>(lastMetCache.iterator());
    }   
        
    /**
     * {@inheritDoc}
     */
    @Override
    public V put(V value) {
        if (value != null) {
            // check if value is already in set
            if(!lastMetCache.add(value)) {
                // remove old value
                lastMetCache.remove(value);
                // add new value
                lastMetCache.add(value);
            }
        }
        return null;
    }

}
