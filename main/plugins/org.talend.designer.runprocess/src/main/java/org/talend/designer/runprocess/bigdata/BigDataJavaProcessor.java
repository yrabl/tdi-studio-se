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
package org.talend.designer.runprocess.bigdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.designer.maven.tools.creator.CreateMavenBundleTemplatePom;
import org.talend.designer.maven.tools.creator.CreateMavenJobPom;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.runprocess.ProcessorConstants;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.designer.runprocess.java.JavaProcessorUtilities;
import org.talend.designer.runprocess.maven.MavenJavaProcessor;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;

/**
 * created by rdubois on 27 janv. 2015 Detailled comment
 *
 */
public abstract class BigDataJavaProcessor extends MavenJavaProcessor {

    protected String windowsAddition, unixAddition;

    protected String unzipFolder;

    protected String archive;

    /**
     * DOC rdubois BigDataJavaProcessor constructor comment.
     * 
     * @param process
     * @param property
     * @param filenameFromLabel
     */
    public BigDataJavaProcessor(IProcess process, Property property, boolean filenameFromLabel) {
        super(process, property, filenameFromLabel);
    }

    protected abstract JobScriptsManager createJobScriptsManager(ProcessItem processItem,
            Map<ExportChoice, Object> exportChoiceMap);

    @Override
    public boolean shouldRunAsExport() {
        return true; // for BD job, will run for export mode always.
    }

    protected String getLibFolderInWorkingDir() {
        // ../lib/
        String libRelativePath = ProcessorConstants.CMD_KEY_WORD_TWO_DOT + ProcessorConstants.CMD_KEY_WORD_SLASH
                + ProcessorConstants.CMD_KEY_WORD_LIB + ProcessorConstants.CMD_KEY_WORD_SLASH;

        // if linux, "$ROOT_PATH/../lib/";
        libRelativePath = getRootWorkingDir(true) + libRelativePath;
        return libRelativePath;
    }

    /**
     * This method gets the command list by the method {@link #makeUpCommandSegments()}.
     */
    @Override
    public String[] getCommandLine() throws ProcessorException {
        List<String> commandList = makeUpCommandSegments();
        return commandList.toArray(new String[commandList.size()]);
    }

    /**
     * <pre>
     * Makes all command segments up, like ahead commands, jvm commands, cp commands, main-class command, and others.
     * This method should be invoked by {@link #getCommandLine()}. The following is about some methods invoked by this
     * method. 
     * <li>{@link #extractAheadCommandSegments()} to extract ahead command segments like "cd `dirname $0`\n".
     * <li>{@link #extractJavaCommandSegments()} to extract java command segments like "java -Xms256M ". 
     * <li>{@link #extractCPCommandSegments()} to extract cp command segments like "cp ../a.jar:../b.jar..". 
     * <li>{@link #extractMainClassSegments()} to extract the main-class command segment. 
     * <li>{@link #extractArgumentSegments()} to extract other arguments for commands.
     * </pre>
     * 
     * Added by Marvin Wang on Mar 22, 2013.
     * 
     * @return
     */
    @Override
    protected List<String> makeUpCommandSegments() {
        return super.makeUpCommandSegments();
    }

    @Override
    public List<String> extractAheadCommandSegments() {
        if (!ProcessorUtilities.isExportConfig() && isExternalUse()) {
            return new ArrayList<String>();
        }
        return super.extractAheadCommandSegments();
    }

    @Override
    public List<String> extractJavaCommandSegments() {
        List<String> commandSegments = new ArrayList<String>();
        String command = ProcessorConstants.CMD_KEY_WORD_JAVA;
        try {
            command = getInterpreter();
        } catch (ProcessorException e1) {
            CommonExceptionHandler.process(e1);
        }

        commandSegments.add(command);
        commandSegments.addAll(extractJavaVMArguments());
        return commandSegments;
    }

    /**
     * Extracts all JVM arguments from job or preference store. It invokes by {@link #extractJavaCommandSegments()}.
     * Added by Marvin Wang on Mar 20, 2013.
     * 
     * @return
     */
    protected List<String> extractJavaVMArguments() {
        List<String> vmArgsSegments = new ArrayList<String>();
        String[] vmArgs = getSettingsJVMArguments();
        vmArgsSegments.addAll(Arrays.asList(vmArgs));
        return vmArgsSegments;
    }

    @Override
    public List<String> extractCPCommandSegments() {
        List<String> cpCommandSegments = new ArrayList<String>();
        cpCommandSegments.add(ProcessorConstants.CMD_KEY_WORD_CP);
        cpCommandSegments.add(makeUpClassPathString());
        return cpCommandSegments;
    }

    @Override
    public String extractMainClassSegments() {
        return super.extractMainClassSegments();
    }

    @Override
    public List<String> extractArgumentSegments() {
        List<String> list = new ArrayList<String>();
        list.add(ProcessorConstants.CMD_KEY_WORD_LIBJAR);
        StringBuffer libJars = new StringBuffer();
        Set<String> libNames = JavaProcessorUtilities.extractLibNamesOnlyForMapperAndReducer(process);
        if (libNames != null && libNames.size() > 0) {
            Iterator<String> itLibNames = libNames.iterator();
            while (itLibNames.hasNext()) {
                libJars.append(getLibFolderInWorkingDir() + itLibNames.next()).append(',');
            }
        }
        list.add(libJars.substring(0, libJars.length() - 1));
        return list;
    }

    /**
     * Makes up the class path string that should be like this
     * "[rootPath]/../lib/a.jar:[rootPath]/../lib/b.jar:[rootPath]/job.jar:" in linux. In windows, ":" should be
     * replaced by ";". About the root path it depends on {@link #getRootWorkingDir()}. The job jar can be gotton by
     * {@link #makeupJobJarName()}. Added by Marvin Wang on Mar 21, 2013.
     * 
     * @return
     */
    protected String makeUpClassPathString() {
        StringBuffer sb = new StringBuffer();
        Set<String> libs = extractAllLibs();
        for (String lib : libs) {
            sb.append(getLibFolderInWorkingDir()).append(lib);
            sb.append(extractClassPathSeparator());
        }

        // add current always
        sb.append("."); //$NON-NLS-1$
        sb.append(extractClassPathSeparator());

        // Append root path to class path.
        String rootWorkingDir = getRootWorkingDir(false);
        if (rootWorkingDir != null && rootWorkingDir.length() > 0) {
            sb.append(rootWorkingDir);
            sb.append(extractClassPathSeparator());
        }

        // Append job jar to class path.
        sb.append(getRootWorkingDir(true));
        sb.append(makeupJobJarName());
        sb.append(extractClassPathSeparator());

        return sb.toString();
    }

    /**
     * Returns all libs required by classpath, refer to this method {@link #extractCPCommandSegments()}. Added by Marvin
     * Wang on Mar 20, 2013.
     * 
     * @return
     */
    protected Set<String> extractAllLibs() {
        Set<String> libsRequiredByJob = new HashSet<String>();
        Set<ModuleNeeded> neededModules = JavaProcessorUtilities.getNeededModulesForProcess(process);
        if (!ProcessorUtilities.isExportConfig()) {
            JavaProcessorUtilities.addLog4jToModuleList(neededModules);
        }
        JavaProcessorUtilities.checkJavaProjectLib(neededModules);

        for (ModuleNeeded neededModule : neededModules) {
            libsRequiredByJob.add(neededModule.getModuleName());
        }
        libsRequiredByJob.addAll(PomUtil.getCodesExportJars(this.getProcess()));
        return libsRequiredByJob;
    }

    /**
     * Makes up a job jar name that should be like "Test-0.1.jar" or "Test.jar". Added by Marvin Wang on Mar 21, 2013.
     * 
     * @return
     */
    protected String makeupJobJarName() {
        String version = process.getVersion();
        String jobJarName = process.getName().toLowerCase() + "_" + version.replace(".", "_") + ".jar"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
        return jobJarName;
    }

    @Override
    protected void setValuesFromCommandline(String tp, String[] cmds) {
        super.setValuesFromCommandline(tp, cmds);

        String libJarStr = null;
        int libJarIndex = ArrayUtils.indexOf(cmds, ProcessorConstants.CMD_KEY_WORD_LIBJAR);
        if (libJarIndex > -1) { // found
            // return the cp values in the next index.
            libJarStr = cmds[libJarIndex + 1];
        }
        if (Platform.OS_WIN32.equals(tp)) {
            this.windowsAddition = libJarStr;
        } else {
            String oldTargetPlatform = this.getTargetPlatform();
            try {
                // because the cmds are from another processor, so must set the target platform same.
                setTargetPlatform(tp);
                // reuse the same api
                String unixRootPath = getRootWorkingDir(true);
                this.unixAddition = libJarStr.replace(unixRootPath, ""); // remove the Path root string
            } finally {
                setTargetPlatform(oldTargetPlatform);
            }
        }
    }

    @Override
    protected CreateMavenBundleTemplatePom createMavenTemplatePom() {
        CreateMavenBundleTemplatePom createMavenTemplatePom = super.createMavenTemplatePom();
        if (createMavenTemplatePom instanceof CreateMavenJobPom) {
            CreateMavenJobPom createMavenJobPom = (CreateMavenJobPom) createMavenTemplatePom;

            if (!StringUtils.isBlank(this.windowsAddition)) {
                String windowsScriptAddition = createMavenJobPom.getWindowsScriptAddition();
                if (windowsScriptAddition != null && windowsScriptAddition.length() > 0) {
                    windowsScriptAddition += ' '; // add one space to separate existed additions.
                } else {
                    windowsScriptAddition = ""; //$NON-NLS-1$
                }
                windowsScriptAddition += ProcessorConstants.CMD_KEY_WORD_LIBJAR + ' ' + this.windowsAddition;
                createMavenJobPom.setWindowsScriptAddition(windowsScriptAddition);
            }
            if (!StringUtils.isBlank(this.unixAddition)) {
                String unixScriptAddition = createMavenJobPom.getUnixScriptAddition();
                if (unixScriptAddition != null && unixScriptAddition.length() > 0) {
                    unixScriptAddition += ' '; // add one space to separate existed additions.
                } else {
                    unixScriptAddition = ""; //$NON-NLS-1$
                }
                unixScriptAddition += ProcessorConstants.CMD_KEY_WORD_LIBJAR + ' ' + this.unixAddition;
                createMavenJobPom.setUnixCcriptAddition(unixScriptAddition);
            }
        }
        return createMavenTemplatePom;
    }

    @Override
    public void cleanBeforeGenerate(int options) throws ProcessorException {
        super.cleanBeforeGenerate(options);
        if (needDoClean() && this.getProcess() instanceof IProcess2) {
            // need re-generate the "getGeneratingNodes" to make sure generate the record struct
            // (IRecordStructGenerator) again.
            ((IProcess2) this.getProcess()).setProcessModified(true);
        }
    }

    @Override
    protected String[] checkExecutingCommands(String path, String[] cmds) {
        String[] checkedcmds = super.checkExecutingCommands(path, cmds);
        // also, need check the "-libjars" for BD jobs.
        return checkExecutingCommandsForRootPath(path, checkedcmds, ProcessorConstants.CMD_KEY_WORD_LIBJAR);
    }

}
