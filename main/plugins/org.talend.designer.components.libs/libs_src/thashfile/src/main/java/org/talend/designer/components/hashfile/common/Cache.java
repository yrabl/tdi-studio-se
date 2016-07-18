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

import org.talend.designer.components.hashfile.file.AdvancedFileHashFile;

/**
 * Interface for caches which stores data.
 * It substitutes {@link AdvancedFileHashFile} class
 */
public interface Cache<T> extends Iterable<T> {

    /**
     * Put value in cache. Returns null if put operation has failed
     * 
     * @param value value to put
     * @return value itself or null if operation has failed
     */
    T put(T value);
}
