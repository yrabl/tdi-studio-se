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
import org.talend.designer.codegen.config.BundleExtJetBean;
import org.talend.designer.codegen.config.BundleJetSkeletonBean;
import org.talend.designer.codegen.config.JetBean;
import org.talend.designer.codegen.config.TemplateUtil;
import org.talend.designer.codegen.model.JetSkeletonManager;

/**
 * DOC wyang class global comment. Detailled comment
 */
public abstract class AbstractJetFileProvider {

    // .javajet
    protected static final String EXT_JAVAJET = TemplateUtil.EXT_SEP + ECodeLanguage.JAVA.getExtension()
            + TemplateUtil.TEMPLATE_EXT;

    /* for extension point */
    private String id, bundleId;

    /* for extension point */
    private final Map<String, String> overrideElementsMap = new HashMap<String, String>();

    private File resourcesRootFolder;

    private List<JetBean> jetBeans;

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

    public Map<String, String> getOverrideElementsMap() {
        return this.overrideElementsMap;
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
     * retrieve the javajet from the base path.
     */
    public List<BundleExtJetBean> retrieveExtJetBeans() {
        retrieveJetBeans();

        List<BundleExtJetBean> extJetBeans = new ArrayList<BundleExtJetBean>();
        for (JetBean bean : jetBeans) {
            if (bean instanceof BundleExtJetBean) {
                extJetBeans.add((BundleExtJetBean) bean);
            }
        }
        return extJetBeans;
    }

    /**
     * 
     * retrieve the .skeleton and inc.javajet
     */
    public List<BundleJetSkeletonBean> retrieveJetSkeletonBeans() {
        retrieveJetBeans();

        List<BundleJetSkeletonBean> jetSkeletonBeans = new ArrayList<BundleJetSkeletonBean>();
        for (JetBean bean : jetBeans) {
            if (bean instanceof BundleJetSkeletonBean) {
                jetSkeletonBeans.add((BundleJetSkeletonBean) bean);
            }
        }
        return jetSkeletonBeans;

    }

    /**
     * 
     * find all jet beans.
     */
    private void retrieveJetBeans() {
        if (jetBeans == null) {
            jetBeans = new ArrayList<JetBean>();
            try {
                File resRootFolder = getResourcesRootFolder();
                if (resRootFolder != null && resRootFolder.exists()) {
                    retrieveJetBeansFromFolder(resRootFolder, jetBeans);
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
    }

    protected void retrieveJetBeansFromFolder(File resourcesFolder, List<JetBean> beans) {
        if (resourcesFolder.isDirectory()) {
            File[] childrenFiles = resourcesFolder.listFiles(new FileFilter() {

                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    return validJavaJetResource(f) || validJetSkeletonResource(f);
                }
            });
            if (childrenFiles != null) {
                for (File f : childrenFiles) {
                    if (f.isFile()) {
                        if (validJetSkeletonResource(f)) {
                            BundleJetSkeletonBean jetSkeletonBean = createJetSkeletonBean(f);
                            if (jetSkeletonBean != null) {
                                beans.add(jetSkeletonBean);
                            }
                        } else if (validJavaJetResource(f)) {
                            BundleExtJetBean extJetBean = createJetBean(f);
                            if (extJetBean != null) {
                                beans.add(extJetBean);
                            }
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
     * valid the file is jet bean .javajet or not.
     */
    protected boolean validJavaJetResource(File res) {
        if (res != null && res.isFile()) {
            if (res.getName().endsWith(EXT_JAVAJET)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * valid the file is jet bean .javajet or not.
     */
    protected boolean validJetSkeletonResource(File res) {
        return JetSkeletonManager.validJetSkeletonResource(res);
    }

    /**
     * 
     * according to the jet file to create the jet bean bundle.
     */
    protected BundleExtJetBean createJetBean(File file) {
        IPath relativePath = getFileRelativePath(file);
        // FIXME TUP-2233, the className is same file name?
        String className = relativePath.removeFileExtension().lastSegment();

        BundleExtJetBean jetBean = new BundleExtJetBean(getBundleId(), relativePath.toString(), className);

        jetBean.setClassPath(new HashMap<String, String>(getJetBeanDependences(file)));
        jetBean.setClassLoader(getJetBeanClassLoader());

        return jetBean;
    }

    /**
     * 
     * create JetSkeletonBean.
     */
    protected BundleJetSkeletonBean createJetSkeletonBean(File file) {
        IPath relativePath = getFileRelativePath(file);
        BundleJetSkeletonBean jetSkeletonBean = new BundleJetSkeletonBean(getBundleId(), relativePath.toString());
        return jetSkeletonBean;
    }

    protected IPath getFileRelativePath(File file) {
        File resRootFolder = getResourcesRootFolder();
        Path basePath = new Path(resRootFolder.getAbsolutePath());

        IPath relativePath = getBasePath().append(new Path(file.getAbsolutePath()).makeRelativeTo(basePath));
        return relativePath;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractJetFileProvider)) {
            return false;
        }
        AbstractJetFileProvider other = (AbstractJetFileProvider) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [bundleId=" + this.bundleId + ", id=" + this.id + "]";
    }

}
