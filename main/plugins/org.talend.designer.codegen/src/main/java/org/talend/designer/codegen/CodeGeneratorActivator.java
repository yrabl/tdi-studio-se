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
package org.talend.designer.codegen;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * Activator for Code Generator.
 * 
 * $Id$
 * 
 */
public class CodeGeneratorActivator extends Plugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.talend.designer.codegen"; //$NON-NLS-1$

    // The shared instance
    private static CodeGeneratorActivator plugin;

    /**
     * Default Constructor.
     */
    public CodeGeneratorActivator() {
        plugin = this;
    }

    public static CodeGeneratorActivator getDefault() {
        return plugin;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }
}
