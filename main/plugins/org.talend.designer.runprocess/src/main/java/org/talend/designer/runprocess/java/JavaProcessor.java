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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.JavaFormattingStrategy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.formatter.FormattingContext;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.formatter.MultiPassContentFormatter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.RuntimeExceptionHandler;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.commons.utils.resource.FileExtensions;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.ITargetExecutionConfig;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.runprocess.IJavaProcessorStates;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.core.runtime.process.TalendProcessOptionConstants;
import org.talend.core.ui.services.IRulesProviderService;
import org.talend.core.utils.BitwiseOptionUtils;
import org.talend.designer.codegen.ICodeGenerator;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ISyntaxCheckableEditor;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.ui.editor.CodeEditorFactory;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.designer.runprocess.ProcessorConstants;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.designer.runprocess.RunProcessContext;
import org.talend.designer.runprocess.RunProcessPlugin;
import org.talend.designer.runprocess.i18n.Messages;
import org.talend.designer.runprocess.prefs.RunProcessPrefsConstants;
import org.talend.repository.ProjectManager;
import org.talend.repository.utils.EsbConfigUtils;
import org.talend.utils.io.FilesUtils;

/**
 * Creat the package folder for the java file, and put the generated file to the correct folder.
 * 
 * The creation for the java package should follow the pattern below:
 * 
 * 1)The name for the first grade folder should keep same with the T.O.S project name. 2)The folder name within the
 * project should be the job name.
 * 
 * <br/>
 * 
 * $Id: JavaProcessor.java 2007-1-22 上�?�10:53:24 yzhang $
 * 
 */
@SuppressWarnings("restriction")
public class JavaProcessor extends AbstractJavaProcessor implements IJavaBreakpointListener {

    /** The compiled job class path. */
    private IPath compiledCodePath;

    /** The compiled context file path. */
    private IPath compiledContextPath;

    /** Tells if filename is based on id or label of the process. */
    private final boolean filenameFromLabel;

    private IJavaProcessorStates states;

    private ISyntaxCheckableEditor checkableEditor;

    private String formatedCode;

    /**
     * Matchs placeholder in subprocess_header.javajet, it will be replaced by the size of method code.
     */
    private static final String SIZE_COMMENT = "?SIZE?"; //$NON-NLS-1$

    private static final String METHOD_END_COMMENT = "End of Function:"; //$NON-NLS-1$

    private static final String METHOD_START_COMMENT = "Start of Function:"; //$NON-NLS-1$

    protected Property property;

    protected Set<JobInfo> buildChildrenJobs;

    private final ITalendProcessJavaProject talendJavaProject;

    protected boolean isTestJob = false;

    private boolean doClean = false;

    /**
     * Set current status.
     * 
     * DOC yzhang Comment method "setStatus".
     * 
     * @param states
     */
    public void setStatus(IJavaProcessorStates states) {
        this.states = states;
    }

    /**
     * Constructs a new JavaProcessor.
     * 
     * @param process Process to be turned in Java code.
     * @param filenameFromLabel Tells if filename is based on id or label of the process.
     */
    public JavaProcessor(IProcess process, Property property, boolean filenameFromLabel) {
        super(process);
        this.property = property;
        this.talendJavaProject = JavaProcessorUtilities.getTalendJavaProject();
        Assert.isNotNull(this.talendJavaProject, Messages.getString("JavaProcessor.notFoundedProjectException"));
        this.project = this.talendJavaProject.getProject();
        if (ProcessUtils.isTestContainer(process)) {
            isTestJob = true;
        }
        if (property != null) {
            if (property.getItem() != null && property.getItem() instanceof ProcessItem) {
                final ProcessItem processItem = (ProcessItem) property.getItem();
                final ProcessType process2 = processItem.getProcess();
                if (process2 != null) {
                    // resolve the node
                    process2.getNode();
                }
            }
        }

        this.filenameFromLabel = filenameFromLabel;
        setProcessorStates(STATES_RUNTIME);
        if (checkableEditor == null && process instanceof IProcess2 && ((IProcess2) process).getEditor() != null) {
            checkableEditor = CodeEditorFactory.getInstance().getCodeEditor((IProcess2) process);
        }
    }

    @Override
    public ITalendProcessJavaProject getTalendJavaProject() {
        return this.talendJavaProject;
    }

    @Override
    public Set<JobInfo> getBuildChildrenJobs() {
        if (buildChildrenJobs == null) {
            buildChildrenJobs = new HashSet<JobInfo>();

            JobInfo lastMainJob = LastGenerationInfo.getInstance().getLastMainJob();
            Set<JobInfo> infos = null;
            if (lastMainJob == null && property != null) {
                infos = ProcessorUtilities.getChildrenJobInfo((ProcessItem) property.getItem());
            } else {
                infos = LastGenerationInfo.getInstance().getLastGeneratedjobs();
            }
            for (JobInfo jobInfo : infos) {
                buildChildrenJobs.add(jobInfo);
            }
        }
        return this.buildChildrenJobs;
    }

    /*
     * Initialization of the variable codePath and contextPath.
     * 
     * @see org.talend.designer.runprocess.IProcessor#initPaths(org.talend.core.model .process.IContext)
     */
    @Override
    public void initPaths(IContext c) throws ProcessorException {
        if (c.equals(this.context)) {
            return;
        }
        createInternalPackage();

        initCodePath(c);
        this.context = c;
    }

    @Override
    public void initPath() throws ProcessorException {
        IContext initContext = this.context;
        if (initContext == null) {
            initContext = this.process.getContextManager().getDefaultContext();
        }
        initCodePath(initContext);
    }

    @Override
    protected boolean isStandardJob() {
        return property != null && property.getItem() != null && process instanceof IProcess2;
    }

    public void initCodePath(IContext c) throws ProcessorException {
        if (buildChildrenJobs != null) {
            buildChildrenJobs.clear();
        }

        ITalendProcessJavaProject tProcessJavaProject = getTalendJavaProject();

        IProgressMonitor monitor = new NullProgressMonitor();

        IFolder srcFolder = null;
        IFolder resourcesFolder = null;
        IFolder outputFolder = null;
        if (isTestJob) {
            srcFolder = tProcessJavaProject.getTestSrcFolder();
            resourcesFolder = tProcessJavaProject.getTestResourcesFolder();
            outputFolder = tProcessJavaProject.getTestOutputFolder();
        } else {
            srcFolder = tProcessJavaProject.getSrcFolder();
            resourcesFolder = tProcessJavaProject.getResourcesFolder();
            outputFolder = tProcessJavaProject.getOutputFolder();
        }

        /*
         * assume the job is "TestJob 0.1", project is "Test" .
         */
        String jobClassPackageFolder = null;
        String jobClassFilePath = null;
        // only for "standard" job
        if (isStandardJob()) {
            Item item = property.getItem();
            // test/testjob_0_1
            jobClassPackageFolder = JavaResourcesHelper.getJobClassPackageFolder(item, isTestJob);
            // test/testjob_0_1/TestJob.java
            jobClassFilePath = JavaResourcesHelper.getJobClassFilePath(item, filenameFromLabel, isTestJob);
            // test.testjob_0_1.TestJob
            this.mainClass = JavaResourcesHelper.getJobPackagedClass(item, filenameFromLabel, isTestJob);
        } else { // for shadow process, or preview process
            // test/shadowfileinputtodelimitedoutput_0_1, test/preview_data
            jobClassPackageFolder = JavaResourcesHelper.getProjectFolderName(property) + JavaUtils.PATH_SEPARATOR
                    + JavaResourcesHelper.getJobFolderName(process.getName(), process.getVersion());
            // test/shadowfileinputtodelimitedoutput_0_1/ShadowFileInputToDelimitedOutput.java,
            // test/preview_data/Preview_Data.java
            jobClassFilePath = jobClassPackageFolder + JavaUtils.PATH_SEPARATOR
                    + (filenameFromLabel ? JavaResourcesHelper.escapeFileName(process.getName()) : process.getId())
                    + JavaUtils.JAVA_EXTENSION;
            // test.shadowfileinputtodelimitedoutput_0_1.ShadowFileInputToDelimitedOutput,
            // test.preview_data.Preview_Data
            this.mainClass = new Path(jobClassFilePath).removeFileExtension().toString().replace('/', '.');
        }

        // create job packages, src/main/java/test/testjob_0_1
        // or test job packages, src/main/java/test/testjob_0_1/testjunitjob_0_1
        tProcessJavaProject.createSubFolder(monitor, srcFolder, jobClassPackageFolder);
        // src/main/java/test/testjob_0_1/TestJob.java
        // or src/main/java/test/testjob_0_1/testjunitjob_0_1/TestjunitJob.java
        this.codePath = srcFolder.getProjectRelativePath().append(jobClassFilePath);

        // target/classes/test/testjob_0_1
        // or target/test-classes/test/testjob_0_1/testjunitjob_0_1
        IFolder jobClassFolder = outputFolder.getFolder(jobClassPackageFolder);
        // TestJob
        String jobName = new Path(jobClassFilePath).removeFileExtension().lastSegment();
        // target/classes/test/testjob_0_1/TestJob
        // or target/test-classes/test/testjob_0_1/testjunitjob_0_1/TestjunitJob
        this.compiledCodePath = jobClassFolder.getProjectRelativePath().append(jobName);

        if (isTestJob) {
            // test/testjob_0_1/datas
            IPath dataPath = new Path(jobClassPackageFolder).append(JavaUtils.JAVA_DATAS_DIRECTORY);
            this.dataFilePath = resourcesFolder.getFolder(dataPath).getProjectRelativePath();
        }

        /*
         * for context.
         */
        // test/testjob_0_1/contexts
        // or test/testjob_0_1/testjunitjob_0_1/contexts
        IPath jobContextFolderPath = new Path(jobClassPackageFolder).append(JavaUtils.JAVA_CONTEXTS_DIRECTORY);
        // src/main/resources/test/testjob_0_1/contexts
        // or src/test/resources/test/testjob_0_1/testjunitjob_0_1/contexts
        tProcessJavaProject.createSubFolder(monitor, resourcesFolder, jobContextFolderPath.toString());
        // for example, Default
        String contextFileName = JavaResourcesHelper.getJobContextFileName(c);
        // test/testjob_0_1/contexts/Default.properties
        // or test/testjob_0_1/testjunitjob_0_1/contexts/Default.properties
        IPath jobContextPath = jobContextFolderPath.append(contextFileName);
        // src/main/resources/test/testjob_0_1/contexts/Default.properties
        // or src/test/resources/test/testjob_0_1/testjunitjob_0_1/contexts/Default.properties
        this.contextPath = resourcesFolder.getFile(jobContextPath).getProjectRelativePath();
        // target/classes/test/testjob_0_1/contexts/Default.properties
        // or target/test-classes/test/testjob_0_1/testjunitjob_0_1/contexts/Default.properties
        this.compiledContextPath = outputFolder.getFile(jobContextPath).getProjectRelativePath();
    }

    /**
     * DOC chuang Comment method "computeMethodSizeIfNeeded".
     * 
     * @param processCode
     * @return
     */
    private String computeMethodSizeIfNeeded(String processCode) {
        // must match TalendDesignerPrefConstants.DISPLAY_METHOD_SIZE
        boolean displayMethodSize = Boolean.parseBoolean(CorePlugin.getDefault().getDesignerCoreService()
                .getPreferenceStore("displayMethodSize")); //$NON-NLS-1$
        if (displayMethodSize) {
            StringBuffer code = new StringBuffer(processCode);
            int fromIndex = 0;
            while (fromIndex != -1 && fromIndex < code.length()) {
                int methodStartPos = code.indexOf(METHOD_START_COMMENT, fromIndex);
                if (methodStartPos < 0) {
                    break;
                }
                int sizeCommentPos = code.indexOf(SIZE_COMMENT, fromIndex);

                // move ahead to the start position of source code
                methodStartPos = code.indexOf("*/", sizeCommentPos) + 2; //$NON-NLS-1$

                int methodEndPos = code.indexOf(METHOD_END_COMMENT, fromIndex);
                if (methodEndPos < 0) {
                    break;
                }
                // start position for next search
                fromIndex = methodEndPos + METHOD_END_COMMENT.length();
                // go back to the end position of source code
                methodEndPos = code.lastIndexOf("/*", methodEndPos); //$NON-NLS-1$
                int size = methodEndPos - methodStartPos;
                code.replace(sizeCommentPos, sizeCommentPos + SIZE_COMMENT.length(), String.valueOf(size));

            }
            return code.toString();
        } else {
            return processCode;
        }
    }

    protected boolean needDoClean() {
        return doClean;
    }

    protected void setDoClean(boolean doClean) {
        this.doClean = doClean;
    }

    @Override
    public void cleanBeforeGenerate(int options) throws ProcessorException {
        setDoClean(false);
        if (this.getProcess().isNeedRegenerateCode() || this.getProcess() instanceof IProcess2
                && ((IProcess2) this.getProcess()).isProcessModified()) {
            // will do clean
            setDoClean(true);
        } else {
            return;
        }
        // clean the generated java source codes.
        if (BitwiseOptionUtils.containOption(options, CLEAN_JAVA_CODES)) {
            IFolder javaCodeFolder = getCodeProject().getFolder(this.getSrcCodePath().removeLastSegments(1));
            // cleanFolder(javaCodeFolder);
            try {
                if (javaCodeFolder != null) {
                    String processSourceFileName = null;
                    if (process != null) {
                        processSourceFileName = process.getName() + ".java"; //$NON-NLS-1$
                    }
                    for (IResource resource : javaCodeFolder.members()) {
                        if ("java".equals(resource.getFileExtension())) {//$NON-NLS-1$
                            if (processSourceFileName != null && processSourceFileName.equals(resource.getName())) {
                                ((IFile) resource).setContents(new ByteArrayInputStream(new byte[0]), true, false,
                                        null);
                            } else {
                                resource.delete(true, null);
                            }
                        } else {
                            resource.delete(true, null);
                        }
                    }
                }
            } catch (CoreException e) {
                // do nothing
            }

            IFolder classCodeFolder = getCodeProject().getFolder(this.getCompiledCodePath().removeLastSegments(1));
            cleanFolder(classCodeFolder);
        }

        // clean the context groups. Sometimes, if remove the context group, the context file be kept still.
        if (BitwiseOptionUtils.containOption(options, CLEAN_CONTEXTS)) {
            IFolder srcContextFolder = getCodeProject().getFolder(this.getSrcContextPath().removeLastSegments(1));
            cleanFolder(srcContextFolder);

            IFolder classContextFolder = getCodeProject().getFolder(this.getCompiledContextPath().removeLastSegments(1));
            cleanFolder(classContextFolder);
        }

        // if test case, clean the data
        if (BitwiseOptionUtils.containOption(options, CLEAN_DATA_SETS) && isTestJob) {
            IFolder srcDatasetFolder = getCodeProject().getFolder(this.getSrcDataSetPath());
            cleanFolder(srcDatasetFolder);
        }
    }

    private void cleanFolder(IFolder folder) {
        try {
            FilesUtils.deleteFolder(folder.getLocation().toFile(), false);
            folder.refreshLocal(IResource.DEPTH_ONE, null);
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
    }

    /*
     * Append the generated java code form context into java file wihtin the project. If the file not existed new one
     * will be created.
     * 
     * @see org.talend.designer.runprocess.IProcessor#generateCode(org.talend.core .model.process.IContext, boolean,
     * boolean, boolean)
     */
    @Override
    public void generateCode(boolean statistics, boolean trace, boolean javaProperties, int option) throws ProcessorException {
        super.generateCode(statistics, trace, javaProperties, option);
        try {
            // hywang modified for 6484
            String currentJavaProject = ProjectManager.getInstance().getProject(property).getTechnicalLabel();

            ICodeGenerator codeGen;
            ICodeGeneratorService service = RunProcessPlugin.getDefault().getCodeGeneratorService();
            if (javaProperties) {
                String javaInterpreter = ""; //$NON-NLS-1$
                String javaLib = ""; //$NON-NLS-1$
                String javaContext = getContextPath().toPortableString();

                codeGen = service.createCodeGenerator(process, statistics, trace, javaInterpreter, javaLib, javaContext,
                        currentJavaProject);
            } else {
                codeGen = service.createCodeGenerator(process, statistics, trace);
            }
            // set the selected context. if don't find, will keep default
            if (!process.getContextManager().getDefaultContext().getName().equals(context.getName())) {
                boolean found = false;
                for (IContext c : process.getContextManager().getListContext()) {
                    if (c.getName().equals(context.getName())) {
                        found = true;
                    }
                }
                if (found) {
                    codeGen.setContextName(context.getName());
                }
            }
            String processCode = ""; //$NON-NLS-1$
            try {
                // must before codegen for job to set the rule flag.
                if (PluginChecker.isRulesPluginLoaded()) {
                    IRulesProviderService rulesService = (IRulesProviderService) GlobalServiceRegister.getDefault().getService(
                            IRulesProviderService.class);
                    if (rulesService != null) {
                        boolean useGenerateRuleFiles = false;
                        List<? extends INode> allNodes = this.process.getGeneratingNodes();
                        for (int i = 0; i < allNodes.size(); i++) {
                            if (allNodes.get(i) instanceof INode) {
                                INode node = allNodes.get(i);
                                if (rulesService.isRuleComponent(node)
                                        && !node.getElementParameter(EParameterName.PROPERTY_TYPE.getName()).getValue()
                                                .toString().equals("BUILT_IN")) { //$NON-NLS-1$
                                    useGenerateRuleFiles = true;
                                    break;
                                }
                            }
                        }
                        if (useGenerateRuleFiles && rulesService != null && currentJavaProject != null) {
                            rulesService.generateFinalRuleFiles(currentJavaProject, this.process);
                            LastGenerationInfo.getInstance().setUseRules(this.process.getId(), this.process.getVersion(), true);
                        }
                    }
                }

                processCode = codeGen.generateProcessCode();

            } catch (SystemException e) {
                throw new ProcessorException(Messages.getString("Processor.generationFailed"), e); //$NON-NLS-1$
            } catch (IOException e) {
                ExceptionHandler.process(e);
            }

            if (!BitwiseOptionUtils.containOption(option, TalendProcessOptionConstants.GENERATE_WITHOUT_FORMAT)) {
                processCode = doFormat(processCode);
            }

            // see feature 4610:option to see byte length of each code method
            processCode = computeMethodSizeIfNeeded(processCode);
            InputStream codeStream = new ByteArrayInputStream(processCode.getBytes());

            // Generating files
            IFile codeFile = this.getCodeProject().getFile(this.getSrcCodePath());
            if (!codeFile.exists()) {
                // maybe have been removed in cleanBeforeGenerate. just confirm to remove the files with different case
                // in win.
                try {
                    org.talend.commons.utils.io.FilesUtils.removeExistedResources(null, codeFile, true, true);
                } catch (Exception e) {
                    throw new ProcessorException(e);
                }
                IFolder parentFolder = (IFolder) codeFile.getParent();
                if (!parentFolder.exists()) {
                    parentFolder.create(true, true, null);
                }
                codeFile.create(codeStream, true, null);
            } else {
                codeFile.setContents(codeStream, true, false, null);

            }
            // codeFile.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);

            processCode = null;

            // updateContextCode(codeGen);

            codeFile.getProject().deleteMarkers("org.eclipse.jdt.debug.javaLineBreakpointMarker", true, IResource.DEPTH_INFINITE); //$NON-NLS-1$

            List<INode> breakpointNodes = CorePlugin.getContext().getBreakpointNodes(process);
            if (!breakpointNodes.isEmpty()) {
                String[] nodeNames = new String[breakpointNodes.size()];
                int pos = 0;
                String nodeName;
                for (INode node : breakpointNodes) {
                    nodeName = node.getUniqueName();
                    if (node.getComponent().getMultipleComponentManagers().size() > 0) {
                        nodeName += "_" + node.getComponent().getMultipleComponentManagers().get(0).getInput().getName(); //$NON-NLS-1$
                    }
                    nodeNames[pos++] = "[" + nodeName + " main ] start"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                int[] lineNumbers = getLineNumbers(codeFile, nodeNames);
                setBreakpoints(codeFile, getMainClass(), lineNumbers);
            }
        } catch (CoreException e1) {
            if (e1.getStatus() != null && e1.getStatus().getException() != null) {
                ExceptionHandler.process(e1.getStatus().getException());
            }
            throw new ProcessorException(Messages.getString("Processor.tempFailed"), e1); //$NON-NLS-1$
        }
    }

    /**
     * 
     * Test the formating source codes,only when talend debug mode
     */
    private void writeCodesToFile(String contents, String fileBaseName) {
        if (!CommonsPlugin.isDebugMode()) {
            return;
        }
        IFolder folder = this.getTalendJavaProject().getTempFolder().getFolder("--CodesFormat");//$NON-NLS-1$
        File codesDir = folder.getLocation().toFile();
        codesDir.mkdirs();
        FileWriter fw = null;
        try {
            fw = new FileWriter(new File(codesDir, this.getProcess().getName() + '_' + fileBaseName));
            fw.write(contents);
            fw.flush();
        } catch (IOException e) {
            ExceptionHandler.process(e);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    //
                }
            }
        }
    }

    private String doFormat(String processCode) {
        // format the code before save the file.
        final String toFormat = processCode;
        writeCodesToFile(toFormat, "1-beforeFormat");//$NON-NLS-1$
        // fix for 21320
        final Job job = new Job("t") { //$NON-NLS-1$

            private Thread workThread;

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask("Format code", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                FutureTask<Boolean> ft = new FutureTask<Boolean>(new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws Exception {
                        formatedCode = formatCode(toFormat);
                        return Boolean.TRUE;
                    }
                });
                Boolean isSucceed = null;
                try {
                    workThread = new Thread(ft);
                    workThread.start();
                    isSucceed = ft.get();
                } catch (Throwable e) {
                    if (!monitor.isCanceled()) {
                        ExceptionHandler.process(e);
                    }
                }
                if (isSucceed == Boolean.TRUE && !monitor.isCanceled()) {
                    writeCodesToFile(formatedCode, "2-afterFormat");//$NON-NLS-1$
                }
                monitor.done();
                return Status.OK_STATUS;
            }

            @Override
            protected void canceling() {
                try {
                    super.canceling();
                    if (workThread != null) {
                        workThread.stop();
                    }
                } catch (Throwable e) {
                    // should catch the ThreadDeath, in case to crash Studio
                }
            }
        };
        long time1 = System.currentTimeMillis();

        job.setSystem(true);
        job.schedule();
        boolean f = true;
        long timeout = 1000 * DesignerPlugin.getDefault().getPreferenceStore()
                .getInt(ITalendCorePrefConstants.PERFORMANCE_JAVA_PROCESS_CODE_FORMATE_TIMEOUT);
        if (timeout <= 0) {
            timeout = 30000;
        }
        while (f) {
            long time2 = System.currentTimeMillis();
            if (time2 - time1 > timeout) {
                if (job.getResult() == null || !job.getResult().isOK()) {
                    f = false;
                    job.cancel();
                    if (!CommonsPlugin.isHeadless()) {
                        Display.getDefault().asyncExec(new Runnable() {

                            @Override
                            public void run() {
                                MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                        Messages.getString("JavaProcessor.warn.codeFormatTimeout.title"), //$NON-NLS-1$
                                        Messages.getString("JavaProcessor.warn.codeFormatTimeout.message")); //$NON-NLS-1$
                            }
                        });
                    }
                } else {
                    processCode = formatedCode;
                    f = false;
                }
            } else {
                if (job.getState() != Job.RUNNING) {
                    if (job.getResult() != null && job.getResult().isOK()) {
                        processCode = formatedCode;
                        f = false;
                    }
                }
            }

        }
        formatedCode = null;
        return processCode;
    }

    /**
     * DOC nrousseau Comment method "formatCode".
     * 
     * from SourceViewer.doOperation for FORMAT.
     * 
     * @param processCode
     * @return
     */
    @SuppressWarnings({ "unchecked" })
    private String formatCode(String processCode) {
        // if export, won't format too.
        // we cannot make calls to Ui in headless mode
        if (ProcessorUtilities.isExportConfig() || CommonsPlugin.isHeadless()) {
            return processCode; // nothing to do
        }
        final IDocument document = new Document(processCode);

        JavaTextTools tools = JavaPlugin.getDefault().getJavaTextTools();
        tools.setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING);

        IFormattingContext context = null;
        DocumentRewriteSession rewriteSession = null;
        if (document instanceof IDocumentExtension4) {
            rewriteSession = ((IDocumentExtension4) document).startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
        }

        try {
            final String rememberedContents = document.get();

            try {
                final MultiPassContentFormatter formatter = new MultiPassContentFormatter(IJavaPartitions.JAVA_PARTITIONING,
                        IDocument.DEFAULT_CONTENT_TYPE);

                formatter.setMasterStrategy(new JavaFormattingStrategy());
                // formatter.setSlaveStrategy(new CommentFormattingStrategy(),
                // IJavaPartitions.JAVA_DOC);
                // formatter.setSlaveStrategy(new CommentFormattingStrategy(),
                // IJavaPartitions.JAVA_SINGLE_LINE_COMMENT);
                // formatter.setSlaveStrategy(new CommentFormattingStrategy(),
                // IJavaPartitions.JAVA_MULTI_LINE_COMMENT);

                context = new FormattingContext();
                context.setProperty(FormattingContextProperties.CONTEXT_DOCUMENT, Boolean.TRUE);

                Map<String, String> preferences;
                if (this.getTalendJavaProject() == null) {
                    preferences = new HashMap<String, String>(JavaCore.getOptions());
                } else { // use project options
                    preferences = new HashMap<String, String>(this.getTalendJavaProject().getJavaProject().getOptions(true));
                }
                context.setProperty(FormattingContextProperties.CONTEXT_PREFERENCES, preferences);

                formatter.format(document, context);
            } catch (RuntimeException x) {
                // fire wall for
                // https://bugs.eclipse.org/bugs/show_bug.cgi?id=47472
                // if something went wrong we undo the changes we just did
                // TODO to be removed after 3.0 M8
                document.set(rememberedContents);
                throw x;
            }

        } finally {
            if (rewriteSession != null && document instanceof IDocumentExtension4) {
                ((IDocumentExtension4) document).stopRewriteSession(rewriteSession);
            }
            if (context != null) {
                context.dispose();
            }
        }
        return document.get();
    }

    @Override
    public void setSyntaxCheckableEditor(ISyntaxCheckableEditor checkableEditor) {
        this.checkableEditor = checkableEditor;
    }

    @Override
    public void syntaxCheck() {
        if (checkableEditor != null) {
            checkableEditor.validateSyntax();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getCodeContext()
     */
    @Override
    public String getCodeContext() {
        return getCodeProject().getLocation().append(getContextPath()).removeLastSegments(1).toPortableString();
    }

    private String escapeFilename(final String filename) {
        return filename != null ? filename.replace(" ", "") : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getCodePath()
     */
    @Override
    public IPath getCodePath() {
        return this.states.getCodePath();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getContextPath()
     */
    @Override
    public IPath getContextPath() {
        return this.states.getContextPath();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getDataSetPath()
     */
    @Override
    public IPath getDataSetPath() {
        return this.states.getDataSetPath();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getCodeProject()
     */
    @Override
    public IProject getCodeProject() {
        return this.project;
    }

    /**
     * Find line numbers of the beginning of the code of process nodes.
     * 
     * @param file Code file where we are searching node's code.
     * @param nodes List of nodes searched.
     * @return Line numbers where code of nodes appears.
     * @throws CoreException Search failed.
     */
    private static int[] getLineNumbers(IFile file, String[] nodes) throws CoreException {
        List<Integer> lineNumbers = new ArrayList<Integer>();

        // List of code's lines searched in the file
        List<String> searchedLines = new ArrayList<String>();
        for (String node : nodes) {
            searchedLines.add(node);
        }

        LineNumberReader lineReader = new LineNumberReader(new InputStreamReader(file.getContents()));
        try {
            String line = lineReader.readLine();
            while (!searchedLines.isEmpty() && line != null) {
                boolean nodeFound = false;
                for (Iterator<String> i = searchedLines.iterator(); !nodeFound && i.hasNext();) {
                    String nodeMain = i.next();
                    if (line.indexOf(nodeMain) != -1) {
                        nodeFound = true;
                        i.remove();

                        // Search the first valid code line
                        boolean lineCodeFound = false;
                        line = lineReader.readLine();
                        while (line != null && !lineCodeFound) {
                            if (isCodeLine(line)) {
                                lineCodeFound = true;
                                lineNumbers.add(new Integer(lineReader.getLineNumber() + 1));
                            }
                            line = lineReader.readLine();
                        }
                    }
                }
                line = lineReader.readLine();
            }
        } catch (IOException ioe) {
            IStatus status = new Status(IStatus.ERROR, "", IStatus.OK, "Source code read failure.", ioe); //$NON-NLS-1$ //$NON-NLS-2$
            throw new CoreException(status);
        }

        int[] res = new int[lineNumbers.size()];
        int pos = 0;
        for (Integer i : lineNumbers) {
            res[pos++] = i.intValue();
        }
        return res;
    }

    /**
     * Return line number where stands specific node in code generated.
     * 
     * @param nodeName
     */
    @Override
    public int getLineNumber(String nodeName) {
        IFile codeFile = this.getCodeProject().getFile(this.getSrcCodePath());
        int[] lineNumbers = new int[] { 0 };
        try {
            lineNumbers = JavaProcessor.getLineNumbers(codeFile, new String[] { nodeName });
        } catch (CoreException e) {
            lineNumbers = new int[] { 0 };
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
        if (lineNumbers.length > 0) {
            return lineNumbers[0];
        } else {
            return 0;
        }
    }

    /**
     * Tells if a line is a line of perl code, not an empty or comment line.
     * 
     * @param line The tested line of code.
     * @return true if the line is a line of code.
     */
    private static boolean isCodeLine(String line) {
        String trimed = line.trim();
        return trimed.length() > 0 && trimed.charAt(0) != '#';
    }

    /**
     * Set java breakpoints in a java file.
     * 
     * @param srcFile Java file in wich breakpoints are added.
     * @param lineNumbers Line numbers in the source file where breakpoints are installed.
     * @throws CoreException Breakpoint addition failed.
     */
    private static void setBreakpoints(IFile codeFile, String typeName, int[] lines) throws CoreException {
        final String javaLineBrekPointMarker = "org.eclipse.jdt.debug.javaLineBreakpointMarker"; //$NON-NLS-1$
        codeFile.deleteMarkers(javaLineBrekPointMarker, true, IResource.DEPTH_ZERO);

        for (int line : lines) {
            JDIDebugModel.createLineBreakpoint(codeFile, typeName, line + 1, -1, -1, 0, true, null);
        }
    }

    private void createInternalPackage() {

        ITalendProcessJavaProject tProcessJvaProject = getTalendJavaProject();
        if (tProcessJvaProject == null) {
            return;
        }
        tProcessJvaProject.createSubFolder(null, tProcessJvaProject.getSrcFolder(), JavaUtils.JAVA_INTERNAL_DIRECTORY);
    }

    /*
     * Get the interpreter of Java.
     * 
     * @see org.talend.designer.runprocess.IProcessor#getInterpreter()
     */
    @Override
    public String getInterpreter() throws ProcessorException {
        // if the interpreter has been set to a specific one (not standard),
        // then this value won't be null
        String interpreter = super.getInterpreter();
        if (interpreter != null) {
            return interpreter;
        }
        return getDefaultInterpreter();

    }

    public static String getDefaultInterpreter() throws ProcessorException {
        IPreferenceStore prefStore = CorePlugin.getDefault().getPreferenceStore();
        String javaInterpreter = prefStore.getString(ITalendCorePrefConstants.JAVA_INTERPRETER);

        if (javaInterpreter == null || javaInterpreter.length() == 0) {
            throw new ProcessorException(Messages.getString("Processor.configureJava")); //$NON-NLS-1$
        }
        Path path = new Path(javaInterpreter);
        javaInterpreter = path.toPortableString();
        return javaInterpreter;
    }

    @Override
    public String getCodeLocation() throws ProcessorException {
        // if the routine path has been set to a specific one (not standard),
        // then this value won't be null
        String codeLocation = super.getCodeLocation();
        if (codeLocation != null) {
            return codeLocation;
        }
        return this.getCodeProject().getLocation().toPortableString();
    }

    /**
     * Getter for compliedCodePath.
     * 
     * @return the compliedCodePath
     */
    public IPath getCompiledCodePath() {
        checkPath(this.compiledCodePath);
        return this.compiledCodePath;
    }

    /**
     * Getter for compiledContextPath.
     * 
     * @return the compiledContextPath
     */
    public IPath getCompiledContextPath() {
        checkPath(this.compiledContextPath);
        return this.compiledContextPath;
    }

    /**
     * Getter for codePath.
     * 
     * @return the codePath
     */
    public IPath getSrcCodePath() {
        checkPath(this.codePath);
        return this.codePath;
    }

    /**
     * Getter for srcContextPath.
     * 
     * @return the srcContextPath
     */
    public IPath getSrcContextPath() {
        checkPath(this.contextPath);
        return this.contextPath;
    }

    /**
     * Getter for SrcDataSetPath.
     * 
     * @return the SrcDataSetPath
     */
    public IPath getSrcDataSetPath() {
        checkPath(this.dataFilePath);
        return this.dataFilePath;
    }

    private void checkPath(IPath path) {
        if (path == null) {
            try {
                initPath();
            } catch (ProcessorException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    @Override
    public String[] getCommandLine() throws ProcessorException {
        // java -cp libdirectory/*.jar;project_path classname;

        List<String> tmpParams = new ArrayList<String>();
        tmpParams.add(getCommand());

        // proxy params
        String[] proxyParameters = getProxyParameters();
        if (proxyParameters != null && proxyParameters.length > 0) {
            for (String str : proxyParameters) {
                tmpParams.add(str);
            }
        }
        // classpath
        tmpParams.add(JavaUtils.JAVA_CP);
        tmpParams.add(getLibsClasspath()); // libs

        tmpParams.add(getMainClass()); // main class

        //
        String[] additionCommandStrings = getAdditionCommandStrings();
        if (additionCommandStrings != null) {
            tmpParams.addAll(Arrays.asList(additionCommandStrings));
        }

        // vm args
        String[] cmd2 = addVMArguments(tmpParams.toArray(new String[0]));

        if (isExportConfig()) { // only for export
            // add bat/sh header lines.
            List<String> list = extractAheadCommandSegments();
            list.addAll(Arrays.asList(cmd2));
            return list.toArray(new String[0]);
        } else {
            return cmd2;
        }
    }

    @Override
    public List<String> extractAheadCommandSegments() {
        List<String> aheadSegments = new ArrayList<String>();
        if (isExportConfig()) {
            if (isWinTargetPlatform()) {
                aheadSegments.add("%~d0\r\n"); //$NON-NLS-1$
                aheadSegments.add("cd %~dp0\r\n"); //$NON-NLS-1$
            } else {
                aheadSegments.add("cd `dirname $0`\n"); //$NON-NLS-1$
                aheadSegments.add("ROOT_PATH=`pwd`\n"); //$NON-NLS-1$
            }
        }
        return aheadSegments;

    }

    protected String getCommand() {
        // init java interpreter
        if (isExportConfig() || isRunAsExport()) {
            return JavaUtils.JAVA_APP_NAME;
        }
        String command;
        try {
            command = getInterpreter();
        } catch (ProcessorException e1) {
            command = JavaUtils.JAVA_APP_NAME;
        }
        return new Path(command).toPortableString();
    }

    @Override
    protected String getRootWorkingDir(boolean withSep) {
        if (!isWinTargetPlatform() && (isExportConfig() || isRunAsExport())) {
            // "$ROOT_PATH/";
            if (withSep) {
                return ProcessorConstants.CMD_KEY_WORD_ROOTPATH + JavaUtils.PATH_SEPARATOR;
            } else {
                return ProcessorConstants.CMD_KEY_WORD_ROOTPATH;
            }
        }
        return ""; //$NON-NLS-1$
    }

    protected String getLibsClasspath() throws ProcessorException {
        final String classPathSeparator = extractClassPathSeparator();

        final String basePathClasspath = getBasePathClasspath();
        // for -cp libs str
        final String neededModulesJarStr = getNeededModulesJarStr();
        String libsStr = basePathClasspath + classPathSeparator + neededModulesJarStr.toString();
        if (isExportConfig() || isRunAsExport()) {
            libsStr += classPathSeparator + getExportJarsStr();
        } else {
            // add just for context when running job.
            ITalendProcessJavaProject tProcessJvaProject = this.getTalendJavaProject();
            IFolder classesFolder = tProcessJvaProject.getOutputFolder();
            String outputPath = classesFolder.getLocation().toPortableString();
            libsStr += classPathSeparator + outputPath;
        }
        // no classPathSeparator in the end.
        if (libsStr.lastIndexOf(classPathSeparator) != libsStr.length() - 1) {
            libsStr += classPathSeparator;
        }
        return libsStr;
    }

    protected String getBasePathClasspath() throws ProcessorException {
        final String classPathSeparator = extractClassPathSeparator();
        final String rootWorkingDir = getRootWorkingDir(false);

        StringBuffer basePath = new StringBuffer(50);
        if (isExportConfig() || isRunAsExport()) {
            // current path.
            basePath.append('.');

            if (rootWorkingDir.length() > 0) {
                basePath.append(classPathSeparator);
                // $ROOT_PATH
                basePath.append(rootWorkingDir);
            }
            if (isExternalUse()) { // for tRunJob with independent option and init pom for classpath
                List<String> codesJars = PomUtil.getCodesExportJars(this.getProcess());
                for (String codesJar : codesJars) {
                    basePath.append(classPathSeparator);
                    if (rootWorkingDir.length() > 0) {
                        basePath.append(rootWorkingDir);
                        basePath.append(JavaUtils.PATH_SEPARATOR);
                    }
                    basePath.append(getBaseLibPath());
                    basePath.append(JavaUtils.PATH_SEPARATOR);
                    basePath.append(codesJar);
                }
            } else {
                String outputPath = getCodeLocation();
                if (outputPath != null) {
                    outputPath = outputPath.replace(ProcessorUtilities.TEMP_JAVA_CLASSPATH_SEPARATOR, classPathSeparator);
                    if (outputPath.endsWith(classPathSeparator)) { // remove the seperator
                        outputPath = outputPath.substring(0, outputPath.length() - 1);
                    }

                    if (!Platform.OS_WIN32.equals(getTargetPlatform())) {
                        String libraryPath = ProcessorUtilities.getLibraryPath();
                        if (libraryPath != null) {
                            String unixRootPath = getRootWorkingDir(true);
                            outputPath = outputPath.replace(libraryPath, unixRootPath + libraryPath);
                        }
                    }
                }
                basePath.append(classPathSeparator);
                basePath.append(outputPath);
            }

            // FIXME for old build (JobJavaScriptsManager) temp, when "ProcessorUtilities.setExportConfig(dir,true)"
            String codeLocation = getCodeLocation();
            if (codeLocation != null && codeLocation.contains(JavaUtils.SYSTEM_ROUTINE_JAR)
                    && codeLocation.contains(JavaUtils.USER_ROUTINE_JAR)
                    && !basePath.toString().contains(JavaUtils.SYSTEM_ROUTINE_JAR)) {
                basePath.append(classPathSeparator);
                codeLocation = codeLocation.replace(ProcessorUtilities.TEMP_JAVA_CLASSPATH_SEPARATOR, classPathSeparator);
                basePath.append(codeLocation);
            }
        } else {
            ITalendProcessJavaProject tProcessJvaProject = this.getTalendJavaProject();
            String jarName = JavaResourcesHelper.getJobJarName(process.getName(), process.getVersion());
            IFolder targetFolder = tProcessJvaProject.getTargetFolder();
            String outputPath = targetFolder.getLocation().toPortableString() + JavaUtils.PATH_SEPARATOR;
            
            basePath.append("."); //$NON-NLS-1$
            
            List<String> codesJars = PomUtil.getCodesExportJars(this.getProcess());
            for (String codesJar : codesJars) {
                basePath.append(classPathSeparator);
                basePath.append(outputPath);
                basePath.append(JavaUtils.PATH_SEPARATOR);
                basePath.append(codesJar);
            }
            basePath.append(classPathSeparator);
            basePath.append(outputPath);
            basePath.append(JavaUtils.PATH_SEPARATOR);
            basePath.append(jarName);
            basePath.append(FileExtensions.JAR_FILE_SUFFIX);

            Set<JobInfo> infos = getBuildChildrenJobs();
            for (JobInfo jobInfo : infos) {
                if (jobInfo.isTestContainer()) {
                    continue;
                }
                String childJarName = JavaResourcesHelper.getJobJarName(jobInfo.getJobName(), jobInfo.getJobVersion());
                basePath.append(classPathSeparator);
                basePath.append(outputPath);
                basePath.append(JavaUtils.PATH_SEPARATOR);
                basePath.append(childJarName);
                basePath.append(FileExtensions.JAR_FILE_SUFFIX);
            }
        }

        return basePath.toString();
    }

    protected String getNeededModulesJarStr() {
        final String classPathSeparator = extractClassPathSeparator();
        final String libPrefixPath = getRootWorkingDir(true);

        Set<ModuleNeeded> neededModules = getNeededModules();
        JavaProcessorUtilities.checkJavaProjectLib(neededModules);

        StringBuffer libPath = new StringBuffer();
        if (isExportConfig() || isRunAsExport()) {
            boolean hasLibPrefix = libPrefixPath.length() > 0;
            for (ModuleNeeded neededModule : neededModules) {
                if (hasLibPrefix) {
                    libPath.append(libPrefixPath);
                }
                libPath.append(getBaseLibPath());
                libPath.append(JavaUtils.PATH_SEPARATOR);
                libPath.append(neededModule.getModuleName());
                libPath.append(classPathSeparator);
            }
        } else {
            Set<String> neededLibraries = new HashSet<String>();
            for (ModuleNeeded neededModule : neededModules) {
                neededLibraries.add(neededModule.getModuleName());
            }

            final File libDir = JavaProcessorUtilities.getJavaProjectLibFolder();
            if (libDir == null) {
                return ""; //$NON-NLS-1$
            }
            File[] jarFiles = libDir.listFiles(FilesUtils.getAcceptJARFilesFilter());

            if (jarFiles != null && jarFiles.length > 0) {
                for (File jarFile : jarFiles) {
                    if (jarFile.isFile() && neededLibraries.contains(jarFile.getName())) {
                        String singleLibPath = new Path(jarFile.getAbsolutePath()).toPortableString();
                        libPath.append(singleLibPath).append(classPathSeparator);
                    }
                }
            }
        }

        final int lastSep = libPath.length() - 1;
        if (libPath.length() != 0 && classPathSeparator.equals(String.valueOf(libPath.charAt(lastSep)))) {
            libPath.deleteCharAt(lastSep);
        }
        return libPath.toString();
    }

    protected String getBaseLibPath() {
        return "../" + JavaUtils.JAVA_LIB_DIRECTORY; //$NON-NLS-1$
    }

    protected String getExportJarsStr() {
        final String rootWorkingDir = getRootWorkingDir(true);
        final String classPathSeparator = extractClassPathSeparator();

        String jarName = JavaResourcesHelper.getJobFolderName(process.getName(), process.getVersion());
        String exportJar = rootWorkingDir + jarName + FileExtensions.JAR_FILE_SUFFIX;

        Set<JobInfo> infos = getBuildChildrenJobs();
        for (JobInfo jobInfo : infos) {
            String childJarName = JavaResourcesHelper.getJobFolderName(jobInfo.getJobName(), jobInfo.getJobVersion());
            exportJar += classPathSeparator + rootWorkingDir + childJarName + FileExtensions.JAR_FILE_SUFFIX;
        }
        return exportJar;

    }

    @Override
    public Set<ModuleNeeded> getNeededModules() {
        Set<ModuleNeeded> neededLibraries = JavaProcessorUtilities.getNeededModulesForProcess(process);
        boolean isLog4jEnabled = Boolean.parseBoolean(ElementParameterParser.getValue(process, "__LOG4J_ACTIVATE__")); //$NON-NLS-1$
        if (isLog4jEnabled) {
            JavaProcessorUtilities.addLog4jToModuleList(neededLibraries);
        }
        return neededLibraries;
    }

    protected String[] getAdditionCommandStrings() {
        return new String[0];
    }

    protected String getLibFolderInWorkingDir() {
        File libFolder = JavaProcessorUtilities.getJavaProjectLibFolder();
        if (libFolder == null) {
            return ""; //$NON-NLS-1$
        }
        return libFolder.getAbsolutePath();
    }

    protected String extractClassPathSeparator() {
        if (isWinTargetPlatform()) {
            return ";"; //$NON-NLS-1$
        } else {
            return ":"; //$NON-NLS-1$
        }
    }

    protected String[] addVMArguments(String[] strings) {
        String[] vmargs = getJVMArgs();

        if (vmargs != null && vmargs.length > 0) {
            String[] lines = new String[strings.length + vmargs.length];
            System.arraycopy(strings, 0, lines, 0, 1);
            System.arraycopy(vmargs, 0, lines, 1, vmargs.length);
            System.arraycopy(strings, 1, lines, vmargs.length + 1, strings.length - 1);
            return lines;
        }
        return strings; // old
    }

    @Override
    public String[] getJVMArgs() {
        String[] vmargs = getSettingsJVMArguments();
        /* check parameter won't happened on exportingJob */
        List<String> asList = convertArgsToList(vmargs);
        if (!isExportConfig() && !isRunAsExport()) {
            String fileEncoding = System.getProperty("file.encoding"); //$NON-NLS-1$
            String encodingFromIni = "-Dfile.encoding=" + fileEncoding; //$NON-NLS-1$
            boolean encodingSetInjob = false;
            for (String arg : asList) {
                if (arg.startsWith("-Dfile.encoding") && fileEncoding != null) { //$NON-NLS-1$
                    /* if user has set the encoding on .ini file,should use this when exetucte job */
                    arg = encodingFromIni;
                    encodingSetInjob = true;
                }
            }
            if (!encodingSetInjob) {
                asList.add(encodingFromIni);
            }
        }
        // add args if using JMX.
        RunProcessContext runProcessContext = RunProcessPlugin.getDefault().getRunProcessContextManager().getActiveContext();
        if (runProcessContext != null) {
            ITargetExecutionConfig config = runProcessContext.getSelectedTargetExecutionConfig();
            if (config != null && config.getCommandlineServerConfig() == null) {
                if (config.isRemote()) {
                    if (config.isUseJMX()) {
                        asList.add("-Dcom.sun.management.jmxremote"); //$NON-NLS-1$
                        asList.add("-Dcom.sun.management.jmxremote.port=" + config.getRemotePort()); //$NON-NLS-1$
                        asList.add("-Dcom.sun.management.jmxremote.ssl=false"); //$NON-NLS-1$
                        asList.add("-Dcom.sun.management.jmxremote.authenticate=false"); //$NON-NLS-1$
                    }
                }
            }
        }

        vmargs = asList.toArray(new String[0]);
        return vmargs;
    }

    protected String[] getSettingsJVMArguments() {
        String string = "";//$NON-NLS-1$
        if (this.process != null) {
            IElementParameter param = this.process.getElementParameter(EParameterName.JOB_RUN_VM_ARGUMENTS_OPTION.getName());
            if (param != null && param.getValue() instanceof Boolean && (Boolean) param.getValue()) { // checked
                param = this.process.getElementParameter(EParameterName.JOB_RUN_VM_ARGUMENTS.getName());
                if (param != null) {
                    string = (String) param.getValue();
                }
            }
        }
        // if not check or the value is empty, should use preference
        if (string == null || "".equals(string)) { //$NON-NLS-1$
            string = RunProcessPlugin.getDefault().getPreferenceStore().getString(RunProcessPrefsConstants.VMARGUMENTS);
        }
        String replaceAll = string.trim();
        String[] vmargs = replaceAll.split(" "); //$NON-NLS-1$
        return vmargs;
    }

    private List<String> convertArgsToList(String[] args) {
        List<String> toReturn = new ArrayList<String>();
        for (String arg : args) {
            toReturn.add(arg);
        }
        return toReturn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getProcessorType()
     */
    @Override
    public String getProcessorType() {
        return JavaUtils.PROCESSOR_TYPE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getProcessorStates()
     */
    @Override
    public void setProcessorStates(int states) {
        if (states == STATES_RUNTIME) {
            new JavaProcessorRuntimeStates(this);
        } else if (states == STATES_EDIT) {
            new JavaProcessorEditStates(this);
        }

    }

    /*
     * Get current class name, and it imported package structure.
     * 
     * @see org.talend.designer.runprocess.IProcessor#getTypeName()
     */
    @Override
    public String getTypeName() {
        return this.getMainClass();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#saveLaunchConfiguration()
     */
    @Override
    public Object saveLaunchConfiguration() throws CoreException {

        /*
         * When launch debug progress, just share all libraries between farther job and child jobs
         */
        // computeLibrariesPath(this.getProcess().getNeededLibraries(true));

        ILaunchConfiguration config = null;
        ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        String projectName = this.getCodeProject().getName();
        ILaunchConfigurationType type = launchManager
                .getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
        if (type != null) {
            ILaunchConfigurationWorkingCopy wc = type.newInstance(null,
                    launchManager.generateUniqueLaunchConfigurationNameFrom(this.getCodePath().lastSegment()));
            wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
            wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, this.getMainClass());
            wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, true);
            wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, CTX_ARG + context.getName());
            config = wc.doSave();
        }
        return config;
    }

    // generate the ILaunchConfiguration with the parameter string.
    @Override
    public Object saveLaunchConfigurationWithParam(String parameterStr) throws CoreException {

        /*
         * When launch debug progress, just share all libraries between farther job and child jobs
         */
        // computeLibrariesPath(this.getProcess().getNeededLibraries(true));

        ILaunchConfiguration config = null;
        ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        String projectName = this.getCodeProject().getName();
        ILaunchConfigurationType type = launchManager
                .getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
        if (type != null) {
            ILaunchConfigurationWorkingCopy wc = type.newInstance(null,
                    launchManager.generateUniqueLaunchConfigurationNameFrom(this.getCodePath().lastSegment()));
            wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
            wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, this.getMainClass());
            wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, true);
            wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, CTX_ARG + context.getName() + parameterStr);
            config = wc.doSave();
        }
        return config;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.Processor#generateContextCode()
     */
    @Override
    public void generateContextCode() throws ProcessorException {
        RepositoryContext repositoryContext = (RepositoryContext) CorePlugin.getContext().getProperty(
                Context.REPOSITORY_CONTEXT_KEY);
        Project project = repositoryContext.getProject();

        ICodeGenerator codeGen;
        ICodeGeneratorService service = RunProcessPlugin.getDefault().getCodeGeneratorService();
        String javaInterpreter = ""; //$NON-NLS-1$
        String javaLib = ""; //$NON-NLS-1$
        String currentJavaProject = project.getTechnicalLabel();
        String javaContext = getContextPath().toPortableString();

        codeGen = service.createCodeGenerator(process, false, false, javaInterpreter, javaLib, javaContext, currentJavaProject);

        updateContextCode(codeGen);
    }

    private void generateWSDL(IFolder wsdlsPackageFolder, INode node) throws CoreException, IOException {
        // retrieve WSDL content (compressed-n-encoded) -> zip-content.-> wsdls.(first named main.wsdl)
        String wsdlContent = node.getElementParameter("WSDL_CONTENT").getValue().toString();
        // String wsdlContent = (String) node.getPropertyValue("WSDL_CONTENT"); //$NON-NLS-1$

        String uniqueName = node.getUniqueName();
        if (null != uniqueName && null != wsdlContent && !wsdlContent.trim().isEmpty()) {

            // configure decoding and uncompressing
            InputStream wsdlStream = new BufferedInputStream(new InflaterInputStream(new Base64InputStream(
                    new ByteArrayInputStream(wsdlContent.getBytes()))));

            if (!wsdlsPackageFolder.exists()) {
                wsdlsPackageFolder.create(true, true, null);
            }
            // generate WSDL file
            if (checkIsZipStream(wsdlStream)) {

                ZipInputStream zipIn = new ZipInputStream(wsdlStream);
                ZipEntry zipEntry = null;

                while ((zipEntry = zipIn.getNextEntry()) != null) {
                    String outputName = zipEntry.getName();
                    if ("main.wsdl".equals(outputName)) { //$NON-NLS-1$
                        outputName = uniqueName + ".wsdl"; //$NON-NLS-1$
                    }
                    IFile wsdlFile = wsdlsPackageFolder.getFile(outputName);
                    if (!wsdlFile.exists()) {
                        // cause create file will do a close. add a warp to ignore close.
                        InputStream unCloseIn = new FilterInputStream(zipIn) {

                            @Override
                            public void close() throws IOException {
                            };
                        };

                        wsdlFile.create(unCloseIn, true, null);
                    }
                    zipIn.closeEntry();
                }
                zipIn.close();
            } else {
                IFile wsdlFile = wsdlsPackageFolder.getFile(uniqueName + ".wsdl"); //$NON-NLS-1$
                wsdlFile.create(wsdlStream, true, null);
            }
        }
    }

    /*
     * (non-Javadoc) generate ESB files on classpath for jobs with ESB components
     */
    @Override
    public void generateEsbFiles() throws ProcessorException {
        List<? extends INode> graphicalNodes = process.getGraphicalNodes(); // process.getGeneratingNodes();

        try {
            IPath jobPackagePath = getSrcCodePath().removeLastSegments(1);
            IFolder jobPackageFolder = this.getCodeProject().getFolder(jobPackagePath);
            IFolder wsdlsPackageFolder = jobPackageFolder.getFolder("wsdl"); //$NON-NLS-1$
            if (wsdlsPackageFolder.exists()) {
                wsdlsPackageFolder.delete(true, null);
            }

            for (INode node : graphicalNodes) {

                if (node.getComponent().getComponentType() == EComponentType.JOBLET) {
                    List<? extends INode> graphicalNodesOfJoblet = node.getProcess().getGeneratingNodes();
                    for (INode nodeOfJoblet : graphicalNodesOfJoblet) {
                        if ("tESBConsumer".equals(nodeOfJoblet.getComponent().getName()) && nodeOfJoblet.isActivate()) { //$NON-NLS-1$
                            generateWSDL(wsdlsPackageFolder, nodeOfJoblet);
                        }
                    }
                } else {
                    if ("tESBConsumer".equals(node.getComponent().getName()) && node.isActivate()) { //$NON-NLS-1$
                        generateWSDL(wsdlsPackageFolder, node);
                    }
                }

            }
        } catch (CoreException e) {
            if (e.getStatus() != null && e.getStatus().getException() != null) {
                ExceptionHandler.process(e.getStatus().getException());
            }
            throw new ProcessorException(Messages.getString("Processor.tempFailed"), e); //$NON-NLS-1$
        } catch (IOException e) {
            throw new ProcessorException(Messages.getString("Processor.tempFailed"), e); //$NON-NLS-1$
        }

        boolean samEnabled = false;
        boolean slEnabled = false;
        for (INode node : graphicalNodes) {
            if (node.isActivate()) {
                final String nodeName = node.getComponent().getName();
                Object slValue = null, samValue = null;
                if ("tESBConsumer".equals(nodeName) //$NON-NLS-1$
                        || "tRESTClient".equals(nodeName) //$NON-NLS-1$
                        || "tRESTRequest".equals(nodeName) //$NON-NLS-1$
                        || "cCXFRS".equals(nodeName)) { //$NON-NLS-1$
                    if (!slEnabled) {
                        slValue = node.getPropertyValue("SERVICE_LOCATOR"); //$NON-NLS-1$
                    }
                    if (!samEnabled) {
                        samValue = node.getPropertyValue("SERVICE_ACTIVITY_MONITOR"); //$NON-NLS-1$
                    }
                } else if ("cCXF".equals(nodeName)) { //$NON-NLS-1$
                    if (!slEnabled) {
                        slValue = node.getPropertyValue("ENABLE_SL"); //$NON-NLS-1$
                    }
                    if (!samEnabled) {
                        samValue = node.getPropertyValue("ENABLE_SAM"); //$NON-NLS-1$
                    }
                }
                if (null != slValue) {
                    slEnabled = (Boolean) slValue;
                }
                if (null != samValue) {
                    samEnabled = (Boolean) samValue;
                }
                if (samEnabled && slEnabled) {
                    break;
                }
            }
        }

        if (samEnabled || slEnabled) {
            File esbConfigsSourceFolder = EsbConfigUtils.getEclipseEsbFolder();
            if (!esbConfigsSourceFolder.exists()) {
                RunProcessPlugin
                        .getDefault()
                        .getLog()
                        .log(new Status(IStatus.WARNING, RunProcessPlugin.getDefault().getBundle().getSymbolicName(),
                                "ESB configuration folder does not exists - " + esbConfigsSourceFolder.toURI())); //$NON-NLS-1$
                return;
            }
            ITalendProcessJavaProject tProcessJvaProject = this.getTalendJavaProject();
            if (tProcessJvaProject == null) {
                return;
            }
            IFolder esbConfigsTargetFolder = tProcessJvaProject.getResourcesFolder();

            // add SAM config file to classpath
            if (samEnabled) {
                copyEsbConfigFile(esbConfigsSourceFolder, esbConfigsTargetFolder, "agent.properties"); //$NON-NLS-1$
            }

            // add SL config file to classpath
            if (slEnabled) {
                copyEsbConfigFile(esbConfigsSourceFolder, esbConfigsTargetFolder, "locator.properties"); //$NON-NLS-1$
            }
        }
    }

    /**
     * Check current stream whether zip stream by check header signature. Zip header signature = 0x504B 0304(big endian)
     * 
     * @param wsdlStream the wsdl stream. <b>Must Support Mark&Reset . Must at beginning of stream.</b>
     * @return true, if is zip stream.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private boolean checkIsZipStream(InputStream wsdlStream) throws IOException {
        boolean isZip = false;
        byte[] headerB = new byte[4];
        wsdlStream.mark(4);
        wsdlStream.read(headerB);
        int header = ByteBuffer.wrap(headerB).getInt();

        if (header == 0x504B0304) {
            isZip = true;
        }
        wsdlStream.reset();
        return isZip;
    }

    private static void copyEsbConfigFile(File esbConfigsSourceFolder, IFolder esbConfigsTargetFolder, String configFile) {
        File esbConfig = new File(esbConfigsSourceFolder, configFile);
        if (esbConfig.exists()) {
            try {
                IFile target = esbConfigsTargetFolder.getFile(configFile);
                InputStream is = null;
                try {
                    is = new FileInputStream(esbConfig);
                    if (!target.exists()) {
                        target.create(is, true, null);
                    } else {
                        target.setContents(is, true, false, null);
                    }
                } finally {
                    if (null != is) {
                        is.close();
                    }
                }
                // esbConfig.copy(esbConfigsTargetFolder.getChild(configFile), EFS.OVERWRITE, null);
            } catch (Exception e) {
                RunProcessPlugin
                        .getDefault()
                        .getLog()
                        .log(new Status(IStatus.WARNING, RunProcessPlugin.getDefault().getBundle().getSymbolicName(),
                                "cannot add configuration file on classpath - " + configFile, //$NON-NLS-1$
                                e));
            }
        } else {
            RunProcessPlugin
                    .getDefault()
                    .getLog()
                    .log(new Status(IStatus.WARNING, RunProcessPlugin.getDefault().getBundle().getSymbolicName(),
                            "cannot find configuration file - " + esbConfig.toURI())); //$NON-NLS-1$
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#addingBreakpoint(org
     * .eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.debug.core.IJavaBreakpoint)
     */
    @Override
    public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {

    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.eclipse.jdt.debug.core.IJavaBreakpointListener#
     * breakpointHasCompilationErrors(org.eclipse.jdt.debug.core. IJavaLineBreakpoint,
     * org.eclipse.jdt.core.dom.Message[])
     */
    @Override
    public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.eclipse.jdt.debug.core.IJavaBreakpointListener# breakpointHasRuntimeException(org.eclipse.jdt.debug.core.
     * IJavaLineBreakpoint, org.eclipse.debug.core.DebugException)
     */
    @Override
    public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, DebugException exception) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointHit(org. eclipse.jdt.debug.core.IJavaThread,
     * org.eclipse.jdt.debug.core.IJavaBreakpoint)
     */
    @Override
    public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointInstalled
     * (org.eclipse.jdt.debug.core.IJavaDebugTarget , org.eclipse.jdt.debug.core.IJavaBreakpoint)
     */
    @Override
    public void breakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
        updateGraphicalNodeBreaking(breakpoint, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointRemoved(
     * org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.debug.core.IJavaBreakpoint)
     */
    @Override
    public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
        if (!target.isTerminated()) {
            updateGraphicalNodeBreaking(breakpoint, true);
        }

    }

    /**
     * yzhang Comment method "updateGraphicalNodeBreaking".
     * 
     * @param breakpoint
     */
    private void updateGraphicalNodeBreaking(IJavaBreakpoint breakpoint, boolean removed) {
        try {
            Integer breakLineNumber = (Integer) breakpoint.getMarker().getAttribute(IMarker.LINE_NUMBER);
            if (breakLineNumber == null || breakLineNumber == -1) {
                return;
            }
            IFile codeFile = this.getCodeProject().getFile(this.getSrcCodePath());
            if (!codeFile.exists()) {
                JDIDebugModel.removeJavaBreakpointListener(this);
                return;
            }
            LineNumberReader lineReader = new LineNumberReader(new InputStreamReader(codeFile.getContents()));
            String content = null;
            while (lineReader.getLineNumber() < breakLineNumber - 3) {
                content = lineReader.readLine();
                if (content == null) {
                    return;
                }
            }
            int startIndex = content.indexOf("[") + 1; //$NON-NLS-1$
            int endIndex = content.indexOf(" main ] start"); //$NON-NLS-1$
            if (startIndex != -1 && endIndex != -1) {
                String nodeUniqueName = content.substring(startIndex, endIndex);
                List<? extends INode> breakpointNodes = CorePlugin.getContext().getBreakpointNodes(process);
                List<? extends INode> graphicalNodes = process.getGraphicalNodes();
                if (graphicalNodes == null) {
                    return;
                }
                for (INode node : graphicalNodes) {
                    if (node.getUniqueName().equals(nodeUniqueName) && removed && breakpointNodes.contains(node)) {
                        CorePlugin.getContext().removeBreakpoint(process, node);
                        if (node instanceof Node) {
                            final INode currentNode = node;
                            Display.getDefault().syncExec(new Runnable() {

                                @Override
                                public void run() {
                                    ((Node) currentNode).removeStatus(Process.BREAKPOINT_STATUS);

                                }

                            });
                        }
                    } else if (node.getUniqueName().equals(nodeUniqueName) && !removed && !breakpointNodes.contains(node)) {
                        CorePlugin.getContext().addBreakpoint(process, node);
                        if (node instanceof Node) {
                            final INode currentNode = node;
                            Display.getDefault().syncExec(new Runnable() {

                                @Override
                                public void run() {
                                    ((Node) currentNode).addStatus(Process.BREAKPOINT_STATUS);

                                }

                            });
                        }
                    }
                }

            }

        } catch (CoreException e) {
            RuntimeExceptionHandler.process(e);
        } catch (IOException e) {
            RuntimeExceptionHandler.process(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#installingBreakpoint
     * (org.eclipse.jdt.debug.core.IJavaDebugTarget , org.eclipse.jdt.debug.core.IJavaBreakpoint,
     * org.eclipse.jdt.debug.core.IJavaType)
     */
    @Override
    public int installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type) {
        return 0;
    }

    @Override
    public Property getProperty() {
        if (property == null) {
            property = ItemCacheManager.getProcessItem(process.getId(), process.getVersion()).getProperty();
        }
        return property;
    }

    @Override
    public void build(IProgressMonitor monitor) throws Exception {
        // build whole project by default.
        getTalendJavaProject().buildModules(monitor, null, null);
    }

    @Override
    public void build(JobInfo jobInfo, IProgressMonitor monitor) throws Exception {
        build(monitor);
    }
}
