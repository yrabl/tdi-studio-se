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
package org.talend.component.core.model;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.component.core.palette.ComponentsPaletteFactory;
import org.talend.component.core.utils.ComponentsUtils;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.update.UpdateResult;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.repository.ProjectManager;

/**
 * created by hcyi on Sep 11, 2015 Detailled comment
 *
 */
public class ComponentsProvider extends AbstractProcessProvider {

    private static Logger log = Logger.getLogger(ComponentsProvider.class);

    @Override
    public void loadComponentsFromExtensionPoint() {
        if (ProjectManager.getInstance().getCurrentProject() != null) {
            ComponentsUtils.loadComponents(ComponentsUtils.getComponentService());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.core.model.process.IReplaceNodeInProcess#rebuildGraphicProcessFromNode(org.talend.core.model.process
     * .INode, java.util.List)
     */
    @Override
    public void rebuildGraphicProcessFromNode(INode node, List<INode> graphicalNodeList) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.core.model.process.IReplaceNodeInProcess#isNeedForceRebuild(org.talend.core.model.process.IProcess2)
     */
    @Override
    public boolean isNeedForceRebuild(IProcess2 process) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.core.model.process.IReplaceNodeInProcess#beforeRunJobInGUI(org.talend.core.model.process.IProcess2)
     */
    @Override
    public void beforeRunJobInGUI(IProcess2 process) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.process.AbstractProcessProvider#buildNewGraphicProcess(org.talend.core.model.
     * properties.Item)
     */
    @Override
    public Process buildNewGraphicProcess(Item node) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.process.AbstractProcessProvider#buildNewGraphicProcess(org.talend.core.model.
     * properties.Item, boolean)
     */
    @Override
    public Process buildNewGraphicProcess(Item node, boolean needScreenshot) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.model.process.AbstractProcessProvider#updateProcessContexts(org.talend.core.model.process
     * .IProcess)
     */
    @Override
    public List<String> updateProcessContexts(IProcess process) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.model.process.AbstractProcessProvider#updateProcessContextsWithoutUI(org.talend.core
     * .model.process.IProcess)
     */
    @Override
    public List<String> updateProcessContextsWithoutUI(IProcess process) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.model.process.AbstractProcessProvider#checkJobletNodeSchema(org.talend.core.model.process
     * .IProcess)
     */
    @Override
    public List<UpdateResult> checkJobletNodeSchema(IProcess process) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.model.process.AbstractProcessProvider#hasJobletComponent(org.talend.core.model.process
     * .IProcess)
     */
    @Override
    public boolean hasJobletComponent(IProcess process) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.model.process.AbstractProcessProvider#getIcons(org.talend.core.model.process.IProcess2)
     */
    @Override
    public ImageDescriptor getIcons(IProcess2 process) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.model.process.AbstractProcessProvider#setIcons(org.talend.core.model.process.IProcess,
     * org.eclipse.jface.resource.ImageDescriptor)
     */
    @Override
    public void setIcons(IProcess process, ImageDescriptor image) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.process.AbstractProcessProvider#addJobletEntry()
     */
    @Override
    public List<PaletteEntry> addJobletEntry() {
        return ComponentsPaletteFactory.createPalette();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.model.process.AbstractProcessProvider#getProcessFromJobletProcessItem(org.talend.core
     * .model.properties.JobletProcessItem)
     */
    @Override
    public IProcess getProcessFromJobletProcessItem(JobletProcessItem jobletProcessItem) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.model.process.AbstractProcessProvider#getJobletItem(org.talend.core.model.process.INode)
     */
    @Override
    public Item getJobletItem(INode node) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.model.process.AbstractProcessProvider#getJobletItem(org.talend.core.model.process.INode,
     * java.lang.String)
     */
    @Override
    public Item getJobletItem(INode node, String version) {
        // TODO Auto-generated method stub
        return null;
    }
}
