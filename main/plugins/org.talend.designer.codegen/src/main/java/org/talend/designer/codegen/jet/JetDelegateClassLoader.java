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
package org.talend.designer.codegen.jet;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * DOC ggu class global comment. Detailled comment
 */
class JetDelegateClassLoader extends ClassLoader {

    private ClassLoader delegate;

    public JetDelegateClassLoader(ClassLoader parent, ClassLoader delegate) {
        super(parent);
        this.delegate = delegate;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException e) {
            // try delegate
            try {
                Class<?> loadedClass = delegate.loadClass(name);
                if (resolve) {
                    resolveClass(loadedClass);
                }
                return loadedClass;
            } catch (ClassNotFoundException ce) {
                // Ignore because we'll re-throw the original exception eventually.
            }
            throw e;
        }
    }

    // @Override
    // protected Class<?> findClass(String name) throws ClassNotFoundException {
    // return delegate.loadClass(name);
    // }

    @Override
    protected URL findResource(String name) {
        return delegate.getResource(name);
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        return delegate.getResources(name);
    }

}
