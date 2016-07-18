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

import org.talend.designer.components.hashfile.common.Cache;

/**
 * Creates appropriate MemoryCache according to keep mode
 */
public class MemoryCacheFactory {
    
    public static final String KEEP_ALL = "KEEP_ALL";
    
    public static final String KEEP_FIRST = "KEEP_FIRST";
    
    public static final String KEEP_LAST = "KEEP_LAST";

    /**
     * Creates appropriate MemoryCache according to keep mode
     * 
     * @param keepMode mode, could be "KEEP_ALL"
     * @return
     */
    public static <T> Cache<T> createMemoryCache(String keepMode) {
        Cache<T> memoryCache = null;
        switch (keepMode) {
        case KEEP_ALL: {
            memoryCache = new KeepAllMemoryCache<>();
            break;
        }
        case KEEP_FIRST: {
            memoryCache = new KeepFirstMemoryCache<>();
            break;
        }
        case KEEP_LAST: {
            memoryCache = new KeepLastMemoryCache<>();
            break;
        }
        default:
        {
            memoryCache = new KeepAllMemoryCache<>();
        }
        }
        return memoryCache;
    }
}
