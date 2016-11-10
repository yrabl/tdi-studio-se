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
package org.talend.designer.runprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.model.process.IContainerEntry;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.core.model.process.Problem.ProblemType;
import org.talend.core.model.process.TalendProblem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.ui.views.problems.Problems;
import org.talend.designer.runprocess.ErrorDetailTreeBuilder.JobErrorEntry;
import org.talend.designer.runprocess.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;

/**
 * Check if there is error in jobs before running.
 */
public class JobErrorsChecker {

    public static List<IContainerEntry> getErrors() {
        List<IContainerEntry> input = new ArrayList<IContainerEntry>();
        try {
            Item item = null;
            IProxyRepositoryFactory proxyRepositoryFactory = CorePlugin.getDefault().getRepositoryService()
                    .getProxyRepositoryFactory();
            ITalendSynchronizer synchronizer = CorePlugin.getDefault().getCodeGeneratorService().createRoutineSynchronizer();

            Map<String, Boolean> jobIds = new HashMap<String, Boolean>();
            Set<JobInfo> toBeCheckedJobs;
            if (LastGenerationInfo.getInstance().getCurrentBuildJob() != null) {
                toBeCheckedJobs = LastGenerationInfo.getInstance().getHaveGeneratedJobs();
            } else {
                toBeCheckedJobs = LastGenerationInfo.getInstance().getLastGeneratedjobs();
            }
            for (JobInfo jobInfo : toBeCheckedJobs) {
                boolean checkedNoError = false;
                if (LastGenerationInfo.getInstance().getNoCompileErrorJobs().contains(jobInfo)) {
                    checkedNoError = true;               
                }
                // TDI-28198:get right process item no matter the job open or close
                List<IRepositoryViewObject> allVersions = proxyRepositoryFactory.getAllVersion(jobInfo.getJobId());
                for (IRepositoryViewObject repositoryObject2 : allVersions) {
                    Property property2 = repositoryObject2.getProperty();
                    if (jobInfo.getJobVersion().equals(property2.getVersion())) {
                        item = property2.getItem();
                        break;
                    }
                }
                if (item == null) {
                    continue;
                }
                // get source file
                IFile sourceFile = synchronizer.getFile(item);
                if (sourceFile == null) {
                    continue;
                }
                jobIds.put(item.getProperty().getId(), checkedNoError);

                // Property property = process.getProperty();
                Problems.addJobRoutineFile(sourceFile, ProblemType.JOB, item, true);
            }
            Problems.refreshProblemTreeView();

            // collect error
            List<Problem> errors = Problems.getProblemList().getProblemsBySeverity(ProblemStatus.ERROR);
            ErrorDetailTreeBuilder builder = new ErrorDetailTreeBuilder();
            input.addAll(builder.createTreeInput(errors, jobIds));
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        return input;
    }

    public static boolean hasErrors(Shell shell, List<IContainerEntry> input, boolean isTestCase) {
        try {
            if (input == null) {
                input = getErrors();
            }
            if (input.size() > 0) {
                ErrorDetailDialog dialog = new ErrorDetailDialog(shell, input);
                dialog.setTestcase(isTestCase);
                if (dialog.open() != IDialogConstants.OK_ID) {
                    // stop running
                    return true;
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return false;
    }

    public static boolean hasErrors(Shell shell) {
        return hasErrors(shell, null, false);
    }

    public static boolean checkExportErrors(IStructuredSelection selection, boolean isJob) {
        if (!selection.isEmpty()) {
            final ITalendSynchronizer synchronizer = CorePlugin.getDefault().getCodeGeneratorService()
                    .createRoutineSynchronizer();
            Map<String, Boolean> jobIds = new HashMap<String, Boolean>();

            List<RepositoryNode> nodes = selection.toList();
            if (nodes.size() > 1) {
                // in case it's a multiple export, only check the status of the latest job to export
                for (RepositoryNode node : nodes) {
                    Item item = node.getObject().getProperty().getItem();
                    try {
                        IFile sourceFile = synchronizer.getFile(item);
                        if (sourceFile == null) {
                            return false;
                        }
                        // check the item has compile error when export job
                        boolean ret = false;
                        String message = null;
                        IMarker[] markers = sourceFile.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ONE);
                        for (IMarker marker : markers) {
                            Integer lineNr = (Integer) marker.getAttribute(IMarker.LINE_NUMBER);
                            message = (String) marker.getAttribute(IMarker.MESSAGE);
                            Integer severity = (Integer) marker.getAttribute(IMarker.SEVERITY);
                            Integer start = (Integer) marker.getAttribute(IMarker.CHAR_START);
                            Integer end = (Integer) marker.getAttribute(IMarker.CHAR_END);
                            if (lineNr != null && message != null && severity != null && start != null && end != null) {
                                switch (severity) {
                                case IMarker.SEVERITY_ERROR:
                                    ret = true;
                                    break;
                                default:
                                    break;
                                }
                            }
                        }
                        if (ret) {
                            if (isJob) {
                                throw new ProcessorException(Messages.getString("JobErrorsChecker_compile_errors") + '\n' + //$NON-NLS-1$
                                        Messages.getString("JobErrorsChecker_compile_error_content", item.getProperty() //$NON-NLS-1$
                                                .getLabel()) + '\n' + message);
                            } else {
                                throw new ProcessorException(Messages.getString("CamelJobErrorsChecker_compile_errors") + '\n' + //$NON-NLS-1$
                                        Messages.getString("CamelJobErrorsChecker_compile_error_content", item.getProperty() //$NON-NLS-1$
                                                .getLabel()) + '\n' + message);
                            }
                        }
                        boolean checkedNoError = false;
                        String jobId = item.getProperty().getId();
                        for (JobInfo jobInfo : LastGenerationInfo.getInstance().getNoCompileErrorJobs()) {
                            if (jobInfo.getJobId().equals(jobId)) {
                                checkedNoError = true;
                                break;
                            }
                        }
                        jobIds.put(jobId, checkedNoError);

                        Problems.addRoutineFile(sourceFile, ProblemType.JOB, item.getProperty().getLabel(), item.getProperty()
                                .getVersion(), true);
                    } catch (Exception e) {
                        MessageBoxExceptionHandler.process(e);
                        return true;
                    }

                }
            } else {
                // if single export (normal case), check compilation status from latest generation.
                try {
                    checkLastGenerationHasCompilationError(true);
                } catch (Exception e) {
                    if (CommonsPlugin.isHeadless()) {
                        CommonExceptionHandler.process(e);
                        // [TESB-8953] avoid SWT invoked and also throw Exception let Command Executor to have detailed
                        // trace in command status.
                        throw new RuntimeException(e);
                    }
                    MessageBoxExceptionHandler.process(e);
                    return true;
                }
            }
            for (RepositoryNode node : nodes) {
                Item item = node.getObject().getProperty().getItem();
                IDesignerCoreService service = CorePlugin.getDefault().getDesignerCoreService();
                IProcess process = service.getProcessFromItem(item);
                if (process instanceof IProcess2) {
                    ((IProcess2) process).checkProcess();
                }
            }
            Problems.refreshProblemTreeView();

            List<Problem> errors = Problems.getProblemList().getProblemsBySeverity(ProblemStatus.ERROR);
            ErrorDetailTreeBuilder builder = new ErrorDetailTreeBuilder();
            List<JobErrorEntry> input = builder.createTreeInput(errors, jobIds);
            try {
                if (input.size() > 0) {
                    String label = input.get(0).getLabel();
                    if (isJob) {
                        throw new ProcessorException(Messages.getString("JobErrorsChecker_compile_errors") + '\n' + //$NON-NLS-1$
                                Messages.getString("JobErrorsChecker_compile_error_content", label)); //$NON-NLS-1$
                    } else {
                        throw new ProcessorException(Messages.getString("CamelJobErrorsChecker_compile_errors") + '\n' + //$NON-NLS-1$
                                Messages.getString("CamelJobErrorsChecker_compile_error_content", label)); //$NON-NLS-1$
                    }
                }
            } catch (Exception e) {
                MessageBoxExceptionHandler.process(e);
                return true;
            }

        }
        return false;
    }

    public static void checkLastGenerationHasCompilationError(boolean updateProblemsView) {
        if (updateProblemsView && CommonsPlugin.isHeadless()) {
            updateProblemsView = false;
        }
        JobInfo currentBuildJob = LastGenerationInfo.getInstance().getCurrentBuildJob();
        if (currentBuildJob != null) {
            if (currentBuildJob.isTestContainer()) {
                return;
            }
            if (!checkCompilationErrors(currentBuildJob, updateProblemsView)) {
                LastGenerationInfo.getInstance().getNoCompileErrorJobs().add(currentBuildJob);
            }
            checkDepedentSubJobError(currentBuildJob.getSubJobInfos(), updateProblemsView);
        } else {
            for (JobInfo jobInfo : LastGenerationInfo.getInstance().getLastGeneratedjobs()) {
                if (jobInfo.isTestContainer()) {
                    continue;
                }
                checkCompilationErrors(jobInfo, updateProblemsView);
            }
        }

    }
    
    private static void checkDepedentSubJobError(Set<JobInfo> jobInfos, boolean updateProblemsView) {
        for (JobInfo subJob : jobInfos) {
            if (!subJob.isIndependentOrDynamic()) {
                checkDepedentSubJobError(subJob.getSubJobInfos(), updateProblemsView);
                if (!checkCompilationErrors(subJob, updateProblemsView)) {
                    LastGenerationInfo.getInstance().getNoCompileErrorJobs().add(subJob);
                }
            }
        }
    }

    private static boolean checkCompilationErrors(JobInfo jobInfo, boolean updateProblemsView) {
        final IProxyRepositoryFactory proxyRepositoryFactory = CorePlugin.getDefault().getRepositoryService()
                .getProxyRepositoryFactory();
        final ITalendSynchronizer synchronizer = CorePlugin.getDefault().getCodeGeneratorService().createRoutineSynchronizer();
        boolean isJob = true;
        boolean hasError = false;
        Item item = null;
        Integer line = null;
        String errorMessage = null;
        try {
            List<IRepositoryViewObject> allVersions = proxyRepositoryFactory.getAllVersion(jobInfo.getJobId());
            for (IRepositoryViewObject repositoryObject2 : allVersions) {
                Property property2 = repositoryObject2.getProperty();
                if (jobInfo.getJobVersion().equals(property2.getVersion())) {
                    item = property2.getItem();
                    break;
                }
            }
            if (item == null) {
                return hasError;
            }
            
            IFile file = synchronizer.getFile(item);
            if (file == null) {
                return hasError;
            }
            // check other java files related to the job . example : spark job will generate several java file for
            // one job
            final IResource[] members = file.getParent().members();
            for (IResource member : members) {
                if (member instanceof IFile && "java".equals(member.getFileExtension())) {
                    IMarker[] markers = ((IFile) member).findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ONE);
                    for (IMarker marker : markers) {
                        Integer lineNr = (Integer) marker.getAttribute(IMarker.LINE_NUMBER);
                        String message = (String) marker.getAttribute(IMarker.MESSAGE);
                        Integer severity = (Integer) marker.getAttribute(IMarker.SEVERITY);
                        Integer start = (Integer) marker.getAttribute(IMarker.CHAR_START);
                        Integer end = (Integer) marker.getAttribute(IMarker.CHAR_END);
                        if (lineNr != null && message != null && severity != null && start != null && end != null) {
                            switch (severity) {
                            case IMarker.SEVERITY_ERROR:
                                line = lineNr;
                                errorMessage = message;
                                break;
                            default:
                                break;
                            }
                        }
                    }
                }
            }
            if (updateProblemsView) {
                Problems.addRoutineFile(file, ProblemType.JOB, item.getProperty().getLabel(),
                        item.getProperty().getVersion(), true);
            }
            hasError = errorMessage != null;
            if (hasError && item != null) {
                if (isJob) {
                    throw new ProcessorException(Messages.getString("JobErrorsChecker_compile_errors") + " " + '\n' + //$NON-NLS-1$
                            Messages.getString("JobErrorsChecker_compile_error_message", item.getProperty().getLabel()) + '\n' //$NON-NLS-1$
                            + Messages.getString("JobErrorsChecker_compile_error_line") + ':' + ' ' + line + '\n' //$NON-NLS-1$
                            + Messages.getString("JobErrorsChecker_compile_error_detailmessage") + ':' + ' ' + errorMessage + '\n' //$NON-NLS-1$
                            + Messages.getString("JobErrorsChecker_compile_error_jvmmessage")); //$NON-NLS-1$
                } else {
                    throw new ProcessorException(Messages.getString("CamelJobErrorsChecker_compile_errors") + " " + '\n' + //$NON-NLS-1$
                            Messages.getString("JobErrorsChecker_compile_error_message", item.getProperty().getLabel()) + '\n' //$NON-NLS-1$
                            + Messages.getString("JobErrorsChecker_compile_error_line") + ':' + ' ' + line + '\n' //$NON-NLS-1$
                            + Messages.getString("JobErrorsChecker_compile_error_detailmessage") + ':' + ' ' + errorMessage + '\n' //$NON-NLS-1$
                            + Messages.getString("JobErrorsChecker_compile_error_jvmmessage")); //$NON-NLS-1$
                }
            }

            // ignore the routine errors.
            // if no error for job, check codes.
            // checkRoutinesCompilationError();
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return hasError;
    }

    private static void checkRoutinesCompilationError() throws ProcessorException {
        // from Problems
        List<Problem> errors = Problems.getProblemList().getProblemsBySeverity(ProblemStatus.ERROR);
        for (Problem p : errors) {
            if (p instanceof TalendProblem) {
                TalendProblem talendProblem = (TalendProblem) p;
                if (talendProblem.getType() == ProblemType.ROUTINE) {
                    int line = talendProblem.getLineNumber();
                    String errorMessage = talendProblem.getDescription();
                    throw new ProcessorException(Messages.getString(
                            "JobErrorsChecker_routines_compile_errors", talendProblem.getJavaUnitName()) + '\n'//$NON-NLS-1$
                            + Messages.getString("JobErrorsChecker_compile_error_line") + ':' + ' ' + line + '\n' //$NON-NLS-1$
                            + Messages.getString("JobErrorsChecker_compile_error_detailmessage") + ':' + ' ' + errorMessage); //$NON-NLS-1$
                }
            }
        }

        // if can't find the routines problem, try to check the file directly(mainly for commandline)
        try {
            final ITalendSynchronizer synchronizer = CorePlugin.getDefault().getCodeGeneratorService()
                    .createRoutineSynchronizer();
            IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();
            List<IRepositoryViewObject> routinesObjects = factory.getAll(ERepositoryObjectType.ROUTINES, false);
            if (routinesObjects != null) {
                for (IRepositoryViewObject obj : routinesObjects) {
                    Property property = obj.getProperty();
                    Item routinesitem = property.getItem();
                    IFile routinesFile = synchronizer.getFile(routinesitem);
                    IMarker[] markers = routinesFile.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ONE);
                    for (IMarker marker : markers) {
                        Integer lineNr = (Integer) marker.getAttribute(IMarker.LINE_NUMBER);
                        String message = (String) marker.getAttribute(IMarker.MESSAGE);
                        Integer severity = (Integer) marker.getAttribute(IMarker.SEVERITY);
                        Integer start = (Integer) marker.getAttribute(IMarker.CHAR_START);
                        Integer end = (Integer) marker.getAttribute(IMarker.CHAR_END);
                        if (lineNr != null && message != null && severity != null && start != null && end != null) {
                            switch (severity) {
                            case IMarker.SEVERITY_ERROR:
                                throw new ProcessorException(
                                        Messages.getString("JobErrorsChecker_routines_compile_errors", property.getLabel()) + '\n'//$NON-NLS-1$
                                                + Messages.getString("JobErrorsChecker_compile_error_line") + ':' + ' ' + lineNr + '\n' //$NON-NLS-1$
                                                + Messages.getString("JobErrorsChecker_compile_error_detailmessage") + ':' + ' ' + message); //$NON-NLS-1$
                            default:
                                break;
                            }
                        }
                    }
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        } catch (SystemException e) {
            ExceptionHandler.process(e);
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }

    }

}
