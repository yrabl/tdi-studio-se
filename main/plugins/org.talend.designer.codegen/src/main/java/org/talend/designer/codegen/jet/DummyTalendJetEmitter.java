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
package org.talend.designer.codegen.jet;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.codegen.jet.JETException;
import org.eclipse.emf.codegen.util.CodeGenUtil;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.osgi.framework.Bundle;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.designer.codegen.model.ICodegenConstants;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class DummyTalendJetEmitter extends TalendJetEmitter {

    protected ClassLoader classpathClassLoader;

    public DummyTalendJetEmitter(IProgressMonitor progressMonitor, HashMap<String, String> globalClasspath, boolean rebuild)
            throws JETException {
        super(null);

        for (String classKey : globalClasspath.keySet()) {
            this.addVariable(classKey, globalClasspath.get(classKey));
        }
        this.talendEclipseHelper = new TalendEclipseHelper(progressMonitor, this, rebuild);
    }

    public TalendEclipseHelper getTalendEclipseHelper() {
        return this.talendEclipseHelper;
    }

    /**
     * 
     * copied from EclipseHelper line 799
     */
    public ClassLoader getClassLoaderFromClasspath() {
        if (classpathClassLoader == null) {
            // by default, use current one
            this.classpathClassLoader = this.classLoader;
            try {
                List<URL> urls = new ArrayList<URL>();
                // Determine all the bundles that this project depends on.
                //
                final Set<Bundle> bundles = new HashSet<Bundle>();
                LOOP: for (IClasspathEntry jetEmitterClasspathEntry : getClasspathEntries()) {
                    IClasspathAttribute[] classpathAttributes = jetEmitterClasspathEntry.getExtraAttributes();
                    if (classpathAttributes != null) {
                        for (IClasspathAttribute classpathAttribute : classpathAttributes) {
                            if (classpathAttribute.getName().equals(CodeGenUtil.EclipseUtil.PLUGIN_ID_CLASSPATH_ATTRIBUTE_NAME)) {
                                Bundle bundle = Platform.getBundle(classpathAttribute.getValue());
                                if (bundle != null) {
                                    bundles.add(bundle);
                                    continue LOOP;
                                }
                            }
                        }
                    }
                    // For any entry that doesn't correspond to a plugin in the running JVM, compute a URL for the
                    // classes.
                    //
                    urls.add(new URL("platform:/resource" + jetEmitterClasspathEntry.getPath() + "/"));
                }

                //
                final IWorkspace workspace = ResourcesPlugin.getWorkspace();
                IProject project = workspace.getRoot().getProject(getProjectName());

                URL url = project.getLocation().append(ICodegenConstants.PATH_PROJECT_CLASSES).toFile().toURL(); //$NON-NLS-1$
                urls.add(url);

                // Define a class loader that looks up classes using the URLs or the parent class loader,
                // and failing those, tries to look up the class in each bundle in the running JVM.
                //
                URLClassLoader theClassLoader = new URLClassLoader(urls.toArray(new URL[0]), classLoader) {

                    @Override
                    public Class<?> loadClass(String className) throws ClassNotFoundException {
                        try {
                            return super.loadClass(className);
                        } catch (ClassNotFoundException exception) {
                            for (Bundle bundle : bundles) {
                                try {
                                    return bundle.loadClass(className);
                                } catch (ClassNotFoundException exception2) {
                                    // Ignore because we'll rethrow the original exception eventually.
                                }
                            }
                            throw exception;
                        }
                    }
                };
                this.classpathClassLoader = theClassLoader;
            } catch (MalformedURLException e) {
                ExceptionHandler.process(e);
            }
        }
        return this.classpathClassLoader;
    }

}
