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
package org.talend.designer.codegen.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.talend.commons.runtime.utils.io.IOUtils;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentCompilations;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.codegen.CodeGeneratorActivator;
import org.talend.designer.codegen.config.BundleJetSkeletonBean;
import org.talend.designer.codegen.config.TalendJetEmitter;
import org.talend.designer.codegen.i18n.Messages;

/**
 * DOC xtan
 * <p>
 * the same as XsdValidationCacheManager.java, used for checking the Skeleton files whether change or not.
 * </p>
 * <p>
 * if there is one Skeleton file have changed, there will generate all jet--->java again.
 * </p>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
public final class JetSkeletonManager {

    private Map<String, Long> alreadyCheckedSkeleton;

    private static JetSkeletonManager instance;

    private final boolean forceSkeletonAlreadyChecked = ComponentCompilations.getMarkers();

    public static final String SKELETON_SUFFIX = ".skeleton"; //$NON-NLS-1$

    public static final String INCLUDEFILEINJET_SUFFIX = ".inc.javajet"; //$NON-NLS-1$

    private JetSkeletonManager() {
    }

    private static synchronized JetSkeletonManager getInstance() {
        if (instance == null) {
            instance = new JetSkeletonManager();
        }
        return instance;
    }

    private void load() {
        try {
            deserializeAlreadyChecked();
        } catch (Exception e) {
            IStatus status = new Status(IStatus.WARNING, CodeGeneratorActivator.PLUGIN_ID,
                    Messages.getString("JetSkeletonManager.unableLoad"), e); //$NON-NLS-1$
            CodeGeneratorActivator.getDefault().getLog().log(status);
        }
    }

    private void save() {
        try {
            serializeAlreadyChecked();
        } catch (Exception e) {
            IStatus status = new Status(IStatus.WARNING, CodeGeneratorActivator.PLUGIN_ID,
                    Messages.getString("JetSkeletonManager.unableSave"), e); //$NON-NLS-1$
            CodeGeneratorActivator.getDefault().getLog().log(status);
        }
    }

    private boolean checkAndUpdateCache(java.io.File file) {
        String path = file.getAbsolutePath();
        long currentCRC = 0;
        try {
            currentCRC = IOUtils.computeCRC(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            // ignore here, only print
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
        if (forceSkeletonAlreadyChecked) {

            alreadyCheckedSkeleton.put(path, currentCRC);
            return false;
        } else {
            Long lastCheckedCRC = alreadyCheckedSkeleton.get(path);

            boolean isChanged = lastCheckedCRC == null || currentCRC != lastCheckedCRC;
            if (isChanged) {
                alreadyCheckedSkeleton.put(path, currentCRC);
            }

            return isChanged;
        }
    }

    private File getSerializationFilePath() throws CoreException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(TalendJetEmitter.JET_PROJECT_NAME);
        if (!project.exists()) {
            project.create(new NullProgressMonitor());
        }
        project.open(new NullProgressMonitor());
        IFile file = project.getFile("SkeletonUpdateCache"); //$NON-NLS-1$
        if (!file.exists()) {
            file.create(null, true, new NullProgressMonitor());
        }
        return file.getLocation().toFile();
    }

    private void serializeAlreadyChecked() throws IOException, CoreException {
        BufferedOutputStream bufferedOutputStream = null;
        try {
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(getSerializationFilePath()));
            ObjectOutputStream objectOut = new ObjectOutputStream(bufferedOutputStream);
            objectOut.writeObject(alreadyCheckedSkeleton);
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                bufferedOutputStream.close();
            } catch (Exception e) {
                // ignore me even if i'm null
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void deserializeAlreadyChecked() throws Exception {
        alreadyCheckedSkeleton = new HashMap<String, Long>();

        File file = getSerializationFilePath();
        if (!file.exists()) {
            return;
        }

        BufferedInputStream bufferedInputStream = null;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            ObjectInputStream objectIn = new ObjectInputStream(bufferedInputStream);
            alreadyCheckedSkeleton = (Map<String, Long>) objectIn.readObject();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                bufferedInputStream.close();
            } catch (Exception e) {
                // ignore me even if i'm null
            }
        }
    }

    /**
     * DOC xtan
     * <p>
     * check the skeleton file whether changed or not, and save the SkeletonUpdateCache file again.
     * </p>
     * 
     * @return true if there is one skeleton file changed.
     */
    public static boolean updateSkeletonPersistenceData() {

        boolean doUpdate = false;

        JetSkeletonManager localInstance = JetSkeletonManager.getInstance();
        localInstance.load();

        IComponentsFactory componentsFactory = ComponentsFactoryProvider.getInstance();

        List<String> skeletons = new ArrayList<String>();
        List<String> systemSkeletons = getSystemSkeletons();
        List<String> componentSkeletons = componentsFactory.getSkeletons();

        if (systemSkeletons != null && !systemSkeletons.isEmpty()) {
            skeletons.addAll(systemSkeletons);
        }
        if (componentSkeletons != null && !componentSkeletons.isEmpty()) {
            skeletons.addAll(componentSkeletons);
        }

        for (String jetSkeleton : skeletons) {
            // System.out.println(jetSkeleton);
            try {

                File file = new File(jetSkeleton);
                if (localInstance.checkAndUpdateCache(file)) {
                    doUpdate = true;
                    // System.out.println("need check:" + jetSkeleton);
                }
            } catch (Exception e) {
                IStatus status = new Status(IStatus.WARNING, CodeGeneratorActivator.PLUGIN_ID,
                        Messages.getString("JetSkeletonManager.updateProblem"), e); //$NON-NLS-1$
                CodeGeneratorActivator.getDefault().getLog().log(status);
                localInstance.save();
                return true;
            }
        }

        localInstance.save();

        return doUpdate;
    }

    /**
     * DOC xtan get the skeleton file: subprocess_header_java.skeleton.
     * 
     * @return
     */
    private static List<String> getSystemSkeletons() {
        List<String> skeletons = new ArrayList<String>();

        List<BundleJetSkeletonBean> bundleJetSkeletonBeans = CodeGeneratorInternalTemplatesFactoryProvider.getInstance()
                .getBundleJetSkeletonBeans();
        for (BundleJetSkeletonBean bean : bundleJetSkeletonBeans) {
            URL url = bean.getResolvedURL();
            if (url != null) {
                File file = new File(url.getFile());
                if (file.exists()) {
                    skeletons.add(url.getFile());
                }
            }
        }
        return skeletons;

    }

    /**
     * 
     * valid the file is jet bean .javajet or not.
     */
    public static boolean validJetSkeletonResource(File res) {
        if (res != null //
                && res.isFile() //
                && res.getName().charAt(0) != '.' //
                && (res.getName().endsWith(JetSkeletonManager.SKELETON_SUFFIX) //
                || res.getName().endsWith(JetSkeletonManager.INCLUDEFILEINJET_SUFFIX))) {
            return true;
        }
        return false;
    }
}
