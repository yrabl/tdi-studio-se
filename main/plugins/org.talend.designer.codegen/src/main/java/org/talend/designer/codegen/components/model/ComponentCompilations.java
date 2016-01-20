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
package org.talend.designer.codegen.components.model;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.talend.designer.codegen.model.ICodegenConstants;

/**
 * DOC mhirt class global comment. Detailled comment
 */
public class ComponentCompilations {

    private static File f = null;

    public static void deleteMarkers() {
        initFile();
        if (fileExists()) {
            f.delete();
        }
    }

    public static void addMarkers() {
        initFile();
        if (fileNotExists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    public static boolean getMarkers() {
        initFile();
        return fileExists();
    }

    private static boolean fileExists() {
        return f != null && f.exists();
    }

    private static boolean fileNotExists() {
        return f != null && !f.exists();
    }

    private static void initFile() {
        try {
            if (f == null) {
                IPath filePath = ResourcesPlugin.getWorkspace().getRoot().getLocation()
                        .append(ICodegenConstants.PROJECT_NAME).append("FirstCompilationMarker"); //$NON-NLS-1$ //$NON-NLS-2$
                f = filePath.toFile();
            }
        } catch (Exception e) {
            // do nothing
        }
    }
}
