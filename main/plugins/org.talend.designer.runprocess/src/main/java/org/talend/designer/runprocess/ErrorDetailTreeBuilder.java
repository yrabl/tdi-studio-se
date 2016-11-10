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

import org.eclipse.swt.graphics.Image;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.core.hadoop.HadoopConstants;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.IContainerEntry;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.TalendProblem;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.designer.core.DesignerPlugin;

/**
 * DOC chuang class global comment. Detailled comment
 */
public class ErrorDetailTreeBuilder {

    private static final String GENERAL_ERROR = "General"; //$NON-NLS-1$

    Map<String, JobErrorEntry> jobs = new HashMap<String, JobErrorEntry>();

    /**
     * DOC chuang Comment method "createTreeInput".
     * 
     * @param errors
     * @param jobNames
     * @return
     */
    public List<JobErrorEntry> createTreeInput(List<Problem> errors, Map<String, Boolean> jobIds) {
        for (Problem error : errors) {
            if (error instanceof TalendProblem) {
                TalendProblem talendProblem = (TalendProblem) error;
                if (talendProblem != null && talendProblem.getJobInfo() != null) {
                    String jobId = talendProblem.getJobInfo().getJobId();
                    if (!jobIds.containsKey(jobId)) {
                        continue;
                    } else if (jobIds.get(jobId)) {
                        // this job has checked compile error ok.
                        continue;
                    }
                    String componentName = GENERAL_ERROR;
                    // System.out.println("tp----" + talendProblem.getElement().getClass());
                    JobErrorEntry jobEntry = getJobEntry(talendProblem.getJavaUnitName());
                    jobEntry.addItem(componentName, talendProblem);

                    /*
                     * ignore the routine errors.
                     */
                    // } else if (talendProblem.getType() == ProblemType.ROUTINE) { // should add the routine always for
                    // job.
                    // JobErrorEntry routineEntry = getJobEntry(talendProblem.getJavaUnitName());
                    // routineEntry.addItem(ProblemType.ROUTINE.getTypeName(), talendProblem);
                }

            } else {
                if (error != null && error.getJobInfo() != null) {
                    String jobId = error.getJobInfo().getJobId();
                    if (!jobIds.containsKey(jobId)) {
                        continue;
                    }
                    String componentName = error.getNodeName();
                    JobErrorEntry jobEntry = getJobEntry(error.getJobInfo().getJobName());
                    jobEntry.addItem(componentName, error);
                }
            }
        }

        return new ArrayList<JobErrorEntry>(jobs.values());
    }

    private JobErrorEntry getJobEntry(String name) {
        JobErrorEntry entry = jobs.get(name);
        if (entry == null) {
            entry = new JobErrorEntry();
            jobs.put(name, entry);
            entry.setLabel(name);
        }
        return entry;
    }

    /**
     * 
     * DOC chuang ErrorDetailTreeBuilder class global comment. Detailled comment
     */
    class JobErrorEntry implements IContainerEntry {

        private String label;

        private Map<String, ComponentErrorEntry> componentEntryMap = new HashMap<String, ComponentErrorEntry>();

        public void setLabel(String label) {
            this.label = label;
        }

        public void addItem(String name, Problem problem) {
            ComponentErrorEntry entry = componentEntryMap.get(name);
            if (entry == null) {
                entry = new ComponentErrorEntry();
                componentEntryMap.put(name, entry);
                entry.setLabel(name);
            }
            entry.addItem(problem);
        }

        @Override
        public List getChildren() {
            return new ArrayList<ComponentErrorEntry>(componentEntryMap.values());
        }

        @Override
        public Image getImage() {
            IProcess activeProcess = DesignerPlugin.getDefault().getRunProcessService().getActiveProcess();
            if (activeProcess != null) {
                String componentsType = activeProcess.getComponentsType();
                if (HadoopConstants.MAPREDUCE_TYPE.equals(componentsType)) {
                    return ImageProvider.getImage(ECoreImage.PROCESS_BATCH_MR_ICON);
                } else if (HadoopConstants.SPARK_TYPE.equals(componentsType)) {
                    return ImageProvider.getImage(ECoreImage.PROCESS_BATCH_SPARK_ICON);
                } else if (HadoopConstants.STORM_TYPE.equals(componentsType)) {
                    return ImageProvider.getImage(ECoreImage.PROCESS_STREAMING_STORM_ICON);
                } else if (HadoopConstants.SPARKSTREAMING_TYPE.equals(componentsType)) {
                    return ImageProvider.getImage(ECoreImage.PROCESS_STREAMING_SPARK_ICON);
                } else {
                    return ImageProvider.getImage(ECoreImage.PROCESS_ICON);
                }
            }
            return ImageProvider.getImage(ECoreImage.PROCESS_ICON);
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public boolean hasChildren() {
            return componentEntryMap.values().size() > 0;
        }

    }

    /**
     * 
     * DOC chuang ErrorDetailTreeBuilder class global comment. Detailled comment
     */
    class ComponentErrorEntry implements IContainerEntry {

        private String label;

        private List<Problem> errors = new ArrayList<Problem>();

        private Image icon;

        public void setLabel(String label) {
            this.label = label;
        }

        @Override
        public String getLabel() {
            return label;
        }

        public void addItem(Problem problem) {
            errors.add(problem);
            if (icon == null && problem.getNodeName() != null) {
                IComponent component = ComponentsFactoryProvider.getInstance().get(problem.getComponentName());
                icon = CoreImageProvider.getComponentIcon(component, ICON_SIZE.ICON_16);
            }
        }

        @Override
        public List getChildren() {
            return errors;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.talend.designer.runprocess.ErrorDetailTreeBuilder.IContainerEntry#getImage()
         */
        @Override
        public Image getImage() {
            if (label.equals(GENERAL_ERROR)) {
                return ImageProvider.getImage(ECoreImage.UNKNOWN);
            } else if (icon != null) {
                return icon;
            }
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.talend.designer.runprocess.ErrorDetailTreeBuilder.IContainerEntry#hasChildren()
         */
        @Override
        public boolean hasChildren() {
            return errors.size() > 0;
        }

    }

}
