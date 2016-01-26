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
package org.talend.component.ui.wizard.ui.context.handler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.component.ui.model.genericMetadata.GenericConnection;
import org.talend.component.ui.wizard.util.GenericContextUtil;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.ui.context.model.table.ConectionAdaptContextVariableModel;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.metadata.managment.ui.model.IConnParamName;
import org.talend.metadata.managment.ui.wizard.context.AbstractRepositoryContextHandler;

/**
 * 
 * created by ycbai on 2015年11月20日 Detailled comment
 *
 */
public class GenericRepositoryContextHandler extends AbstractRepositoryContextHandler {

    @Override
    public boolean isRepositoryConType(Connection connection) {
        return connection instanceof GenericConnection;
    }

    @Override
    public List<IContextParameter> createContextParameters(String prefixName, Connection connection, Set<IConnParamName> paramSet) {
        return GenericContextUtil.createContextParameters(prefixName, connection, paramSet);
    }

    @Override
    public void setPropertiesForContextMode(String prefixName, Connection connection, Set<IConnParamName> paramSet) {
        GenericContextUtil.setPropertiesForContextMode(prefixName, connection, paramSet);
    }

    @Override
    public void setPropertiesForExistContextMode(Connection connection, Set<IConnParamName> paramSet,
            Map<ContextItem, List<ConectionAdaptContextVariableModel>> adaptMap) {
        GenericContextUtil.setPropertiesForExistContextMode(connection, paramSet, adaptMap);
    }

    @Override
    public void revertPropertiesForContextMode(Connection connection, ContextType contextType) {
        GenericContextUtil.revertPropertiesForContextMode(connection, contextType);
    }

    @Override
    public Set<String> getConAdditionPropertiesForContextMode(Connection conn) {
        return Collections.EMPTY_SET;
    }

    @Override
    protected void matchAdditionProperties(Connection conn, Map<ContextItem, List<ConectionAdaptContextVariableModel>> adaptMap) {
        // Do nothing.
    }

    @Override
    protected void matchContextForAttribues(Connection conn, IConnParamName param, String contextVariableName) {
        // Do nothing.
    }

}
