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
package org.talend.designer.dbmap.external.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.talend.core.model.process.IExternalData;
import org.talend.core.model.process.node.IExternalMapEntry;
import org.talend.core.model.process.node.IExternalMapTable;
import org.talend.designer.abstractmap.managers.MapDataDelegateHelper;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 * $Id: ExternalMapperData.java 1 2006-09-29 17:06:40Z nrousseau $
 * 
 */
public class ExternalDbMapData implements IExternalData {

    /**
     * 
     */
    private static final long serialVersionUID = 4877887839142463534L;

    private ExternalDbMapUiProperties uiProperties = new ExternalDbMapUiProperties();

    private List<ExternalDbMapTable> inputTables = new ArrayList<ExternalDbMapTable>(0);

    private List<ExternalDbMapTable> outputTables = new ArrayList<ExternalDbMapTable>(0);

    /** not used ... */
    private List<ExternalDbMapTable> varsTables = new ArrayList<ExternalDbMapTable>(0);

    public List<ExternalDbMapTable> getInputTables() {
        return this.inputTables;
    }

    public void setInputTables(List<ExternalDbMapTable> tables) {
        this.inputTables = tables;
    }

    public List<ExternalDbMapTable> getOutputTables() {
        return this.outputTables;
    }

    public void setOutputTables(List<ExternalDbMapTable> outputTables) {
        this.outputTables = outputTables;
    }

    /** not used ... */
    public List<ExternalDbMapTable> getVarsTables() {
        return this.varsTables;
    }

    /** not used ... */
    public void setVarsTables(List<ExternalDbMapTable> varsTables) {
        this.varsTables = varsTables;
    }

    public ExternalDbMapUiProperties getUiProperties() {
        return this.uiProperties;
    }

    public void setUiProperties(ExternalDbMapUiProperties layoutProperties) {
        this.uiProperties = layoutProperties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @SuppressWarnings("unchecked")
    @Override
    public IExternalData clone() throws CloneNotSupportedException {
        ExternalDbMapData cloned = (ExternalDbMapData) super.clone();
        cloned.uiProperties = (ExternalDbMapUiProperties) uiProperties.clone();
        cloned.inputTables = (List<ExternalDbMapTable>) ((ArrayList) inputTables).clone();
        int inputTablesListSize = inputTables.size();
        for (int i = 0; i < inputTablesListSize; i++) {
            cloned.inputTables.set(i, (ExternalDbMapTable) cloned.inputTables.get(i).clone());
        }
        cloned.outputTables = (List<ExternalDbMapTable>) ((ArrayList) outputTables).clone();
        int listSizeoutputTables = outputTables.size();
        for (int i = 0; i < listSizeoutputTables; i++) {
            cloned.outputTables.set(i, (ExternalDbMapTable) cloned.outputTables.get(i).clone());
        }
        cloned.varsTables = (List<ExternalDbMapTable>) ((ArrayList) varsTables).clone();
        int listSizevarsTables = varsTables.size();
        for (int i = 0; i < listSizevarsTables; i++) {
            cloned.varsTables.set(i, (ExternalDbMapTable) cloned.varsTables.get(i).clone());
        }
        return cloned;
    }

    public Map<IExternalMapTable, List<IExternalMapEntry>> getExpressionColumns(String expression, ExternalDataType... types) {
        MapDataDelegateHelper delegate = new MapDataDelegateHelper(getInputTables(), getOutputTables(), getVarsTables());
        return delegate.getExpressionColumns(expression, types);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IExternalData#getJoinTableNames(java.lang.String)
     */
    public List<String> getJoinedTableNames(String mainTable) {
        // TODO Auto-generated method stub
        return null;
    }

}
