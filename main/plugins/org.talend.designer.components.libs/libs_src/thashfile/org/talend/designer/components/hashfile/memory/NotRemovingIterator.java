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

import java.util.Iterator;

/**
 * Decorates MemoryCache iterator to disallow remove action
 */
class NotRemovingIterator<E> implements Iterator<E> {

    /**
     * Decorated iterator
     */
    private Iterator<E> iterator;
    
    /**
     * Constructor sets decorated iterator
     * 
     * @param iterator decorated iterator
     */
    NotRemovingIterator(Iterator<E> iterator) {
        this.iterator = iterator;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E next() {
        return iterator.next();
    }

    /**
     * Operation is not supported
     * 
     * @throws {@link UnsupportedOperationException}
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() is not supported");
    }

}
