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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.CorePlugin;
import org.talend.core.language.ECodeLanguage;
import org.talend.designer.codegen.CodeGeneratorActivator;
import org.talend.designer.codegen.config.TemplateUtil;
import org.talend.designer.codegen.model.template.BundleJetTemplate;

/**
 * DOC wyang class global comment. Detailled comment
 */
public abstract class AbstractJetFileProvider {

    private String id;

    private File resourcesRootFolder;

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

    // --------------------------------------------------------------------------------------//
    protected String getBundleId() {
        return null; // by default no bundle, need override
    }

    protected IPath getBasePath() {
        // by default
        return new Path("resources"); //$NON-NLS-1$
    }

    protected File getResourcesRootFolder() {
        if (resourcesRootFolder == null) {

            String bundleId = getBundleId();
            IPath basePath = getBasePath();

            if (bundleId != null && basePath != null) {
                try {
                    URL url = FileLocator.find(Platform.getBundle(bundleId), basePath, null);
                    // FIXME, need test in jar, path with space or special charactor(Chinese, etc).
                    URL fileUrl = FileLocator.toFileURL(url);
                    resourcesRootFolder = new File(fileUrl.getPath());
                } catch (IOException e) {
                    //
                }
            }
        }
        return resourcesRootFolder;
    }

    /**
     * retrieve the templates from the base path.
     */
    public List<BundleJetTemplate> retrieveTempaltes() {
        try {
            List<BundleJetTemplate> tempaltes = new ArrayList<BundleJetTemplate>();
            File resRootFolder = getResourcesRootFolder();

            retrieveTemplatesFromFolder(resRootFolder, tempaltes);

            return tempaltes;
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return Collections.emptyList();
    }

    protected void retrieveTemplatesFromFolder(File resourcesFolder, List<BundleJetTemplate> tempaltes) {
        if (resourcesFolder.isDirectory()) {
            File[] childrenFiles = resourcesFolder.listFiles(new FileFilter() {

                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    return validResource(f);
                }
            });
            if (childrenFiles != null) {
                for (File f : childrenFiles) {
                    if (f.isFile()) {
                        BundleJetTemplate template = createTemplate(f);
                        if (template != null) {
                            tempaltes.add(template);
                        }
                    } else if (f.isDirectory()) {
                        retrieveTemplatesFromFolder(f, tempaltes);
                    }
                }
            }
        }
    }

    /**
     * 
     * according to the template file to create the template bundle.
     */
    protected BundleJetTemplate createTemplate(File file) {
        File resRootFolder = getResourcesRootFolder();
        Path basePath = new Path(resRootFolder.getAbsolutePath());

        IPath relativePath = new Path(file.getAbsolutePath()).makeRelativeTo(basePath);
        // TODO, the relativePath is ok or not?
        BundleJetTemplate template = new BundleJetTemplate(getBundleId(), getBasePath().append(relativePath).toString());

        template.getJetTempalteDependences().putAll(getJetTempalteDependences(file));
        template.setJetTempalteClassLoader(getJetTempalteClassLoader());

        return template;
    }

    /**
     * 
     * valid the file is jet template or not.
     */
    protected boolean validResource(File res) {
        if (res != null && res.isFile()) {
            if (res.getName().endsWith(ECodeLanguage.JAVA.getExtension() + TemplateUtil.TEMPLATE_EXT)) {
                return true;
            }
        }
        return false;
    }

    protected Map<String, String> getJetTempalteDependences(File file) {
        Map<String, String> jetTempalteDependences = new LinkedHashMap<String, String>();

        // default.
        jetTempalteDependences.put("CORERUNTIME_LIBRARIES", "org.talend.core.runtime"); //$NON-NLS-1$ //$NON-NLS-2$
        jetTempalteDependences.put("MANAGEMENT_LIBRARIES", "org.talend.metadata.managment"); //$NON-NLS-1$ //$NON-NLS-2$
        jetTempalteDependences.put("CORE_LIBRARIES", CorePlugin.PLUGIN_ID); //$NON-NLS-1$
        jetTempalteDependences.put("CODEGEN_LIBRARIES", CodeGeneratorActivator.PLUGIN_ID); //$NON-NLS-1$
        jetTempalteDependences.put("COMMON_LIBRARIES", CommonsPlugin.PLUGIN_ID); //$NON-NLS-1$

        return jetTempalteDependences;
    }

    protected ClassLoader getJetTempalteClassLoader() {
        return null;
    }

}
