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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.talend.designer.components.hashfile.common.Cache;

/**
 * Memory {@link Cache}, which keeps all records even in case of key duplication 
 */
public class KeepAllMemoryCache<V> implements Cache<V> {
    
    /**
     * Stores all records
     */
    private List<V> allRecords;
    
    /**
     * Constructor creates synchronized list to store all records
     */
    public KeepAllMemoryCache() {
        allRecords = Collections.synchronizedList(new ArrayList<V>());
    }

    /**
     * {@inheritDoc}
     * 
     * Iterator must be manually synched by user
     */
    @Override
    public Iterator<V> iterator() {
        return new NotRemovingIterator<V>(allRecords.iterator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V put(V value) {
        if (value != null) {
            allRecords.add(value);
            return value;
        }
        return null;
    }

}
