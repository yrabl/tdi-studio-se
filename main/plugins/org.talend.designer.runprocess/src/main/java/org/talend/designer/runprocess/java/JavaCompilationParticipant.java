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
package org.talend.designer.runprocess.java;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.SystemException;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ICoreService;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.designer.codegen.model.CodeGeneratorEmittersPoolFactory;
import org.talend.designer.core.ui.views.problems.Problems;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC xhuang class global comment. Detailled comment <br/>
 * 
 */
public class JavaCompilationParticipant extends CompilationParticipant {

    /**
     * DOC xhuang CompilationParticipant constructor comment.
     */
    public JavaCompilationParticipant() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jdt.core.compiler.CompilationParticipant#processAnnotations(org.eclipse.jdt.core.compiler.BuildContext
     * [])
     */
    @Override
    public void processAnnotations(BuildContext[] files) {

        boolean routineToUpdate = false;
        super.processAnnotations(files);

        List<IRepositoryViewObject> routineObjectList = null;
        for (BuildContext context : files) {

            String filePath = (context.getFile().getProjectRelativePath()).toString();

            if (isRoutineFile(filePath)) {
                if (!routineToUpdate) {
                    IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();

                    try {

                        routineObjectList = factory.getAll(ERepositoryObjectType.ROUTINES, false);
                    } catch (PersistenceException e) {
                        ExceptionHandler.process(e);
                    }
                }
                updateProblems(routineObjectList, filePath);
                routineToUpdate = true;
            }
        }

        if (routineToUpdate && !CommonsPlugin.isHeadless()) {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    try {
                        Problems.refreshProblemTreeView();
                    } catch (Exception e) {
                        // ignore any exception here, as there is no impact if refresh or not.
                        // but if don't ignore, exception could be thrown if refresh is done too early.
                    }
                }
            });
        }

    }

    /**
     * yzhang Comment method "updateProblems".
     */
    private void updateProblems(List<IRepositoryViewObject> routineObjectList, String filePath) {

        IRunProcessService runProcessService = CorePlugin.getDefault().getRunProcessService();
        try {
            ITalendProcessJavaProject talendProcessJavaProject = runProcessService.getTalendProcessJavaProject();
            if (talendProcessJavaProject == null) {
                return;
            }
            if (!GlobalServiceRegister.getDefault().isServiceRegistered(ICoreService.class)) {
                return;
            }
            ICoreService coreService = (ICoreService) GlobalServiceRegister.getDefault().getService(ICoreService.class);
            ITalendSynchronizer synchronizer = coreService.createCodesSynchronizer();
            if (synchronizer == null) {
                return;
            }
            IProject javaProject = talendProcessJavaProject.getProject();
            IFile file = javaProject.getFile(filePath);
            String fileName = file.getName();

            for (IRepositoryViewObject repositoryObject : routineObjectList) {
                Property property = repositoryObject.getProperty();
                Item item = property.getItem();
                IFile currentFile = synchronizer.getFile(item);
                if (currentFile != null && fileName.equals(currentFile.getName()) && currentFile.exists()) {
                    Problems.addRoutineFile(currentFile, property);
                    break;
                }

            }
        } catch (SystemException e) {
            ExceptionHandler.process(e);
        }
    }

    private boolean isRoutineFile(String filePath) {
        int endIndex = filePath.lastIndexOf("/"); //$NON-NLS-1$
        String javaFileCatalog = filePath.substring(0, endIndex);
        if (javaFileCatalog.contains(JavaUtils.JAVA_ROUTINES_DIRECTORY)) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.core.compiler.CompilationParticipant#isActive(org.eclipse.jdt.core.IJavaProject)
     */
    @Override
    public boolean isActive(IJavaProject project) {
        // if commandline or no GUI yeet, don't do anything
        if (CommonsPlugin.isHeadless() || !PlatformUI.isWorkbenchRunning()
                || CodeGeneratorEmittersPoolFactory.isInitializeStart()) {
            return false;
        }
        ITalendProcessJavaProject talendJavaProject = JavaProcessorUtilities.getTalendJavaProject();
        if (talendJavaProject != null) {
            return talendJavaProject.getJavaProject().equals(project);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.core.compiler.CompilationParticipant#isAnnotationProcessor()
     */
    @Override
    public boolean isAnnotationProcessor() {
        return true;
    }

}
