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
package org.talend.designer.core.ui.editor.connections;

import java.util.List;

import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.INodeConnector;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;

/**
 * DOC Talend class global comment. Detailled comment
 */
public class NodeConnectorTool {

    private NodePart nodePart;

    public NodeConnectorTool(NodePart nodePart) {
        this.nodePart = nodePart;
    }

    public INodeConnector getConnector() {
        Node node = (Node) nodePart.getModel();
        INodeConnector mainConnector = null;
        if (node.isELTComponent()) {
            mainConnector = node.getConnectorFromType(EConnectionType.TABLE);
        } else if (ComponentCategory.CATEGORY_4_CAMEL.getName().equals(node.getComponent().getType())) {
            INodeConnector tmp = null;
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault()
                        .getService(ICamelDesignerCoreService.class);
                tmp = node.getConnectorFromType(camelService.getTargetConnectionType(node));
            } else {
                tmp = node.getConnectorFromType(EConnectionType.ROUTE);
            }
            mainConnector = tmp;
        } else {
            List<? extends INodeConnector> nodeConnList = node.getConnectorsFromType(EConnectionType.FLOW_MAIN);
            for (INodeConnector nodeConn : nodeConnList) {
                if (isConnectorValid(nodeConn)) {
                    return nodeConn;
                }
            }
        }

        if (!isConnectorValid(mainConnector)) {
            return null;
        }
        return mainConnector;
    }

    private boolean isConnectorValid(INodeConnector mainConnector) {

        if (mainConnector == null) {
            return false;
        }

        if (!mainConnector.isShow()) {
            return false;
        }

        if (mainConnector.getMaxLinkOutput() != -1) {
            if (mainConnector.getCurLinkNbOutput() >= mainConnector.getMaxLinkOutput()) {
                return false;
            }
        }
        if (mainConnector.getMaxLinkOutput() == 0) {
            return false;
        }

        return true;
    }

}
