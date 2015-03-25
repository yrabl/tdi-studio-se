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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.designer.codegen.CodeGeneratorActivator;
import org.talend.designer.codegen.config.BundleTemplateJetBean;
import org.talend.designer.codegen.config.TemplateUtil;

/**
 * DOC wyang class global comment. Detailled comment
 */
public abstract class AbstractJetFileProvider {

    private String id, bundleId;

    private File resourcesRootFolder;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public String getBundleId() {
        return bundleId;
    }

    protected abstract IPath getBasePath();

    protected File getResourcesRootFolder() {
        if (resourcesRootFolder == null) {
            IPath basePath = getBasePath();
            if (basePath != null) {
                try {
                    URL url = FileLocator.find(Platform.getBundle(getBundleId()), basePath, null);
                    // FIXME TUP-2233, need test in jar, path with space or special charactor(Chinese, etc).
                    // also for JetBean.getUri
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
     * retrieve the jet from the base path.
     */
    public List<BundleTemplateJetBean> retrieveJetBeans() {
        try {
            List<BundleTemplateJetBean> jetBeans = new ArrayList<BundleTemplateJetBean>();

            File resRootFolder = getResourcesRootFolder();
            if (resRootFolder != null && resRootFolder.exists()) {
                retrieveJetBeansFromFolder(resRootFolder, jetBeans);
            }

            return jetBeans;
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return Collections.emptyList();
    }

    protected void retrieveJetBeansFromFolder(File resourcesFolder, List<BundleTemplateJetBean> beans) {
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
                        BundleTemplateJetBean jetBean = createJetBean(f);
                        if (jetBean != null) {
                            beans.add(jetBean);
                        }
                    } else if (enableRetrievingSubFolders() && f.isDirectory()) {
                        retrieveJetBeansFromFolder(f, beans);
                    }
                }
            }
        }
    }

    protected boolean enableRetrievingSubFolders() {
        return false; // by default, don't process the sub folders.
    }

    /**
     * 
     * according to the jet file to create the jet bean bundle.
     */
    protected BundleTemplateJetBean createJetBean(File file) {
        File resRootFolder = getResourcesRootFolder();
        Path basePath = new Path(resRootFolder.getAbsolutePath());

        IPath relativePath = getBasePath().append(new Path(file.getAbsolutePath()).makeRelativeTo(basePath));
        // FIXME TUP-2233, the className is same file name?
        String className = relativePath.removeFileExtension().lastSegment();

        BundleTemplateJetBean jetBean = new BundleTemplateJetBean(getBundleId(), relativePath.toString(), className);

        jetBean.setClassPath(new HashMap<String, String>(getJetBeanDependences(file)));
        jetBean.setClassLoader(getJetBeanClassLoader());

        return jetBean;
    }

    /**
     * 
     * valid the file is jet bean or not.
     */
    protected boolean validResource(File res) {
        if (res != null && res.isFile()) {
            if (res.getName().endsWith('.' + ECodeLanguage.JAVA.getExtension() + TemplateUtil.TEMPLATE_EXT)) {
                return true;
            }
        }
        return false;
    }

    protected Map<String, String> getJetBeanDependences(File file) {
        Map<String, String> jetTempalteDependences = new LinkedHashMap<String, String>();

        // default for all files.
        jetTempalteDependences.put("CORERUNTIME_LIBRARIES", CoreRuntimePlugin.PLUGIN_ID); //$NON-NLS-1$ 
        jetTempalteDependences.put("MANAGEMENT_LIBRARIES", "org.talend.metadata.managment"); //$NON-NLS-1$ //$NON-NLS-2$
        jetTempalteDependences.put("CORE_LIBRARIES", CorePlugin.PLUGIN_ID); //$NON-NLS-1$
        jetTempalteDependences.put("CODEGEN_LIBRARIES", CodeGeneratorActivator.PLUGIN_ID); //$NON-NLS-1$
        jetTempalteDependences.put("COMMON_LIBRARIES", CommonsPlugin.PLUGIN_ID); //$NON-NLS-1$

        return jetTempalteDependences;
    }

    protected ClassLoader getJetBeanClassLoader() {
        // return this.getClass().getClassLoader(); // FIXME TUP-2233, need check or not?
        ProxyAdditionalClassLoader proxyClassLoader = new ProxyAdditionalClassLoader(this.getClass().getClassLoader());
        // add codegen class loader
        proxyClassLoader.addAdditionalClassLoader(CodeGeneratorActivator.class.getClassLoader());
        return proxyClassLoader;
    }

}
