// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.codegen.additionaljet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * created by ggu on 20 Mar 2015 Detailled comment
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ProxyAdditionalClassLoader extends ClassLoader {

    private List<ClassLoader> additionalClassLoaders = new ArrayList<ClassLoader>();

    public ProxyAdditionalClassLoader(ClassLoader classLoader) {
        super(classLoader);
    }

    public void addAdditionalClassLoader(ClassLoader aClassLoader) {
        if (this.getParent() == aClassLoader) { // don't add the parent again.
            return;
        }
        if (aClassLoader != null && !this.additionalClassLoaders.contains(aClassLoader)) {
            this.additionalClassLoaders.add(aClassLoader);
        }
    }

    public void addAdditionalClassLoaders(ClassLoader[] classLoaders) {
        if (classLoaders != null) {
            for (ClassLoader cl : classLoaders) {
                addAdditionalClassLoader(cl);
            }
        }
    }

    protected Object invokeDeclaredMethod(String method, Object[] paramObjs, Class[] paramClasses, Class resultClass) {
        Object findObject = null;
        for (ClassLoader cl : this.additionalClassLoaders) {
            try {
                findObject = invokeDeclaredMethod(cl, cl.getClass(), method, paramObjs, paramClasses, resultClass, true);
                if (findObject != null) {
                    break;
                }
            } catch (Exception e) {
                // try other loaders.
            }
        }
        return findObject;
    }

    protected Object invokeDeclaredMethod(String method, Object[] paramObjs, Class[] paramClasses) {
        return invokeDeclaredMethod(method, paramObjs, paramClasses, null);
    }

    protected Object invokeDeclaredMethod(Object obj, Class clazz, String methodName, Object[] paramObjs, Class[] paramClasses,
            Class resultClass, boolean withParentClass) {
        Object findObject = null;
        try {
            Method method = clazz.getDeclaredMethod(methodName, paramClasses);
            method.setAccessible(true);
            findObject = method.invoke(obj, paramObjs);
        } catch (Exception e) {
            // try parent or not.
        }
        boolean hasResult = false;
        if (findObject != null) {
            hasResult = true;

            if (resultClass != null) { // for some special results.
                if (resultClass.equals(Enumeration.class)) {
                    hasResult = ((Enumeration) findObject).hasMoreElements();
                } else if (resultClass.equals(Package[].class)) {
                    hasResult = ((Package[]) findObject).length > 0;
                }
            }
        }
        if (!hasResult && withParentClass) {
            findObject = invokeDeclaredMethod(obj, clazz.getSuperclass(), methodName, paramObjs, paramClasses, resultClass,
                    withParentClass);
        }
        return findObject;
    }

    // ----------------------overrides-------------------- //

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> loadedClass = null;
        try {
            loadedClass = super.loadClass(name, resolve);
        } catch (ClassNotFoundException e) {
            // try other additional loader.
        }

        if (loadedClass == null) {
            loadedClass = (Class<?>) invokeDeclaredMethod("loadClass", new Object[] { name, resolve }, //$NON-NLS-1$
                    new Class[] { String.class, boolean.class });
        }
        if (loadedClass != null) {
            return loadedClass;
        }
        throw new ClassNotFoundException(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> findClass = null;
        try {
            findClass = super.findClass(name);
        } catch (ClassNotFoundException e) {
            // try other additional loader.
        }

        if (findClass == null) {
            findClass = (Class<?>) invokeDeclaredMethod("findClass", new Object[] { name }, //$NON-NLS-1$
                    new Class[] { String.class });
        }
        if (findClass != null) {
            return findClass;
        }
        throw new ClassNotFoundException(name);
    }

    @Override
    public URL getResource(String name) {
        URL url = super.getResource(name);

        if (url == null) {
            url = (URL) invokeDeclaredMethod("getResource", new Object[] { name }, //$NON-NLS-1$
                    new Class[] { String.class });
        }
        return url;
    }

    @Override
    protected URL findResource(String name) {
        URL findRes = null;
        findRes = super.findResource(name);

        if (findRes == null) {
            findRes = (URL) invokeDeclaredMethod("findResource", new Object[] { name }, //$NON-NLS-1$
                    new Class[] { String.class });
        }
        return findRes;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> urls = super.getResources(name);

        if (urls == null || !urls.hasMoreElements()) {
            urls = (Enumeration<URL>) invokeDeclaredMethod("getResources", new Object[] { name }, //$NON-NLS-1$
                    new Class[] { String.class }, Enumeration.class);
        }
        if (urls != null) {
            return urls;
        }
        return Collections.emptyEnumeration();
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {

        Enumeration<URL> findResources = null;
        findResources = super.findResources(name);

        if (findResources == null || !findResources.hasMoreElements()) {
            findResources = (Enumeration<URL>) invokeDeclaredMethod("findResources", new Object[] { name }, //$NON-NLS-1$
                    new Class[] { String.class }, Enumeration.class);
        }
        if (findResources != null) {
            return findResources;
        }
        return Collections.emptyEnumeration();

    }

    @Override
    public InputStream getResourceAsStream(String name) {
        InputStream is = super.getResourceAsStream(name);

        if (is == null) {
            is = (InputStream) invokeDeclaredMethod("getResourceAsStream", new Object[] { name }, //$NON-NLS-1$
                    new Class[] { String.class });
        }

        return is;
    }

    @Override
    protected Package getPackage(String name) {
        Package findPackage = null;
        findPackage = super.getPackage(name);

        if (findPackage == null) {
            findPackage = (Package) invokeDeclaredMethod("getPackage", new Object[] { name }, //$NON-NLS-1$
                    new Class[] { String.class });
        }
        return findPackage;

    }

    @Override
    protected Package[] getPackages() {
        Package[] findPackages = null;
        findPackages = super.getPackages();

        if (findPackages == null || findPackages.length == 0) {
            findPackages = (Package[]) invokeDeclaredMethod("getPackage", new Object[] {}, //$NON-NLS-1$
                    new Class[] {}, Package[].class);
        }
        if (findPackages != null) {
            return findPackages;
        }
        return new Package[0];

    }

    @Override
    protected String findLibrary(String libname) {
        String findLibrary = null;
        findLibrary = super.findLibrary(libname);

        if (findLibrary == null) {
            findLibrary = (String) invokeDeclaredMethod("findLibrary", new Object[] { libname }, //$NON-NLS-1$
                    new Class[] { String.class });
        }

        return findLibrary;

    }

    @Override
    public void setDefaultAssertionStatus(boolean enabled) {
        // TODO Auto-generated method stub
        super.setDefaultAssertionStatus(enabled);
    }

    @Override
    public void setPackageAssertionStatus(String packageName, boolean enabled) {
        // TODO Auto-generated method stub
        super.setPackageAssertionStatus(packageName, enabled);
    }

    @Override
    public void setClassAssertionStatus(String className, boolean enabled) {
        // TODO Auto-generated method stub
        super.setClassAssertionStatus(className, enabled);
    }

    @Override
    public void clearAssertionStatus() {
        // TODO Auto-generated method stub
        super.clearAssertionStatus();
    }

}
