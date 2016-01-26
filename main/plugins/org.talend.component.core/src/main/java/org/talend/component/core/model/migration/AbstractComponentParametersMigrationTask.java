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
import java.util.List;

import org.talend.component.core.constants.IComponentConstants;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

/**
 * created by hcyi on Nov 18, 2015 Detailled comment
 *
 */
public class AbstractComponentParametersMigrationTask extends AbstractJobMigrationTask {

    protected String[] salesforceComponentsName = new String[] {
            "tSalesforceInput", "tSalesforceOutput", "tSalesforceConnection", //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            "tSalesforceBulkExec", "tSalesforceGetDeleted", "tSalesforceGetUpdated", "tSalesforceOutputBulkExec",//$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "tSalesforceGetServerTimestamp", "tSalesforceOutputBulk", "tSalesforceWaveBulkExec", "tSalesforceWaveOutputBulkExec" };//$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    /**
     * UtilTool to handle NodeType.
     */
    protected static class ParameterUtilTool {

        public static ElementParameterType findParameterType(NodeType node, String paramName) {
            for (Object param : node.getElementParameter()) {
                ElementParameterType paramType = (ElementParameterType) param;
                if (paramType.getName().equals(paramName)) {
                    return paramType;
                }
            }
            return null;
        }

        public static String getParameterValue(NodeType currentNode, String paramName) {
            ElementParameterType param = findParameterType(currentNode, paramName);
            if (param == null) {
                return null;
            }
            return param.getValue();
        }

        /**
         * Replace param sub sequence.
         *
         * @return true, if rename value sub string
         */
        public static boolean replaceValueSubSequence(ElementParameterType param, CharSequence oldSeq, CharSequence newSeq) {
            String oldValue = param.getValue();
            if (!oldValue.contains(oldSeq)) {
                return false;
            }
            String newValue = oldValue.replace(oldSeq, newSeq);
            param.setValue(newValue);
            return true;
        }

        @SuppressWarnings("unchecked")
        public static boolean addParameterType(NodeType node, ElementParameterType param) {
            return node.getElementParameter().add(param);
        }

        public static boolean removeParameterType(NodeType node, ElementParameterType param) {
            return node.getElementParameter().remove(param);
        }

        public static ElementParameterType createParameterType(String field, String name, String value) {
            return createParameterType(field, name, value, null);
        }

        @SuppressWarnings("unchecked")
        public static ElementParameterType createParameterType(String field, String name, String value,
                List<?> elementParameterTypes) {
            ElementParameterType paramType = TalendFileFactory.eINSTANCE.createElementParameterType();
            paramType.setField(field);
            paramType.setName(name);
            paramType.setValue(value);
            if (elementParameterTypes != null) {
                paramType.getElementValue().addAll(elementParameterTypes);
            }
            return paramType;
        }

        public static void addParameterType(NodeType node, String field, String name, String value, List<?> elementParameterTypes) {
            ElementParameterType paramType = createParameterType(field, name, value, elementParameterTypes);
            addParameterType(node, paramType);
        }

        public static void addParameterType(NodeType node, String field, String name, String value) {
            addParameterType(node, field, name, value, null);
        }

        /**
         * for special parameter value Comment method "convertSpecialParameterValue".
         * 
         * @param oldParameterValue
         * @return
         */
        public static String convertSpecialParameterValue(String oldParameterValue) {
            if (oldParameterValue != null) {
                if (IComponentConstants.ACTION_INSERT.equalsIgnoreCase(oldParameterValue)) {
                    return IComponentConstants.ACTION_INSERT;
                } else if (IComponentConstants.ACTION_UPDATE.equalsIgnoreCase(oldParameterValue)) {
                    return IComponentConstants.ACTION_UPDATE;
                } else if (IComponentConstants.ACTION_UPSERT.equalsIgnoreCase(oldParameterValue)) {
                    return IComponentConstants.ACTION_UPSERT;
                } else if (IComponentConstants.ACTION_DELETE.equalsIgnoreCase(oldParameterValue)) {
                    return IComponentConstants.ACTION_DELETE;
                } else if (IComponentConstants.LOGIN_BASIC.equalsIgnoreCase(oldParameterValue)) {
                    return IComponentConstants.LOGIN_BASIC;
                } else if (IComponentConstants.LOGIN_OAUTH.equalsIgnoreCase(oldParameterValue)
                        || (!oldParameterValue.equals("") && oldParameterValue.startsWith(IComponentConstants.LOGIN_OAUTH))) {//$NON-NLS-1$
                    return IComponentConstants.LOGIN_OAUTH;
                } else if (IComponentConstants.QUERY_QUERY.equalsIgnoreCase(oldParameterValue)) {
                    return IComponentConstants.QUERY_QUERY;
                } else if (IComponentConstants.QUERY_BULK.equalsIgnoreCase(oldParameterValue)) {
                    return IComponentConstants.QUERY_BULK;
                }
            }
            return oldParameterValue;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        // TODO Auto-generated method stub
        return null;
    }
}
