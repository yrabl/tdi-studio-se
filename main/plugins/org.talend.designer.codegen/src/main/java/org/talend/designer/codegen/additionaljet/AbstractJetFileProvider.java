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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.designer.codegen.CodeGeneratorActivator;

/**
 * DOC wyang class global comment. Detailled comment
 */
public abstract class AbstractJetFileProvider {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void overwriteStubAdditionalFile() throws IOException {
        File installationFolder = getInstallationFolder();

        File externalFrameLocation = getExternalFrameLocation();
        if (externalFrameLocation != null) {
            if (externalFrameLocation.exists()) {
                try {
                    final FileFilter sourceFolderFilter = new FileFilter() {

                        @Override
                        public boolean accept(File pathname) {
                            return false;
                        }
                    };
                    FilesUtils.copyFolder(externalFrameLocation, installationFolder, false, sourceFolderFilter, null, true);
                } catch (Throwable e) {
                    ExceptionHandler.process(e);
                }
            }
        }
    }

    protected abstract File getExternalFrameLocation();

    public File getInstallationFolder() throws IOException {

        File installationFolder = null;
        URL url = FileLocator.find(Platform.getBundle(CodeGeneratorActivator.PLUGIN_ID), new Path("resources"), null); //$NON-NLS-1$
        URL fileUrl = FileLocator.toFileURL(url);
        installationFolder = new File(fileUrl.getPath());

        return installationFolder;
    }
}
