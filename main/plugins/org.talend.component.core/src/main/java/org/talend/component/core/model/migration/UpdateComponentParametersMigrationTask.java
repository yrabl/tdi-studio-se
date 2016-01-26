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
package org.talend.component.core.model.migration;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.component.core.utils.ComponentsUtils;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.migration.AbstractJobItemComponentMigrationTask;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

/**
 * 
 * created by hcyi on Nov 17, 2015 Detailled comment
 *
 */
public class UpdateComponentParametersMigrationTask extends AbstractJobItemComponentMigrationTask {

    String[] componentsName = new String[] { "tSalesforceInput", "tSalesforceOutput", "tSalesforceConnection", //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            "tSalesforceBulkExec", "tSalesforceGetDeleted", "tSalesforceGetUpdated", "tSalesforceOutputBulkExec",//$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "tSalesforceGetServerTimestamp", "tSalesforceOutputBulk", "tSalesforceWaveBulkExec", "tSalesforceWaveOutputBulkExec" };//$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.core.model.migration.AbstractJobItemComponentMigrationTask#execute(org.talend.designer.core.model.
     * utils.emf.talendfile.NodeType)
     */
    @Override
    protected boolean execute(NodeType currentNode) {
        List<?> elementParameter = currentNode.getElementParameter();
        ElementParameterType schemasParam = findElementByName(elementParameter, "SCHEMAS", ElementParameterType.class);

        ComponentProperties componentProperties = ComponentsUtils.getComponentProperties(getComponentNameRegex());
        componentProperties.toSerialized();

        ElementParameterType module = ComponentUtilities.getNodeProperty(currentNode, "MODULENAME"); //$NON-NLS-1$
        if (module != null && "CustomModule".equals(module.getValue())) { //$NON-NLS-1$
            ElementParameterType customModule = ComponentUtilities.getNodeProperty(currentNode, "CUSTOM_MODULE");//$NON-NLS-1$
            if (customModule == null) {
                customModule = ComponentUtilities.getNodeProperty(currentNode, "CUSTOM_MODULE_NAME");//$NON-NLS-1$
            }
            if (customModule != null) {
                String customModuleName = customModule.getValue();
                if (customModuleName != null) {
                    if (!customModuleName.startsWith("\"")) {//$NON-NLS-1$
                        customModuleName = "\"" + customModuleName;//$NON-NLS-1$
                    }
                    if (!customModuleName.endsWith("\"")) {//$NON-NLS-1$
                        customModuleName = customModuleName + "\"";//$NON-NLS-1$
                    }
                    customModule.setValue(customModuleName);
                }
            }
        }
        return false;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.migration.AbstractJobItemComponentMigrationTask#getComponentNameRegex()
     */
    @Override
    protected String getComponentNameRegex() {
        // TODO Auto-generated method stub
        return "tSalesforceOutput"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2015, 11, 17, 12, 0, 0);
        return gc.getTime();
    }
}
