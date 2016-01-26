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
package org.talend.component.core.dnd;

import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.AbstractDragAndDropServiceHandler;

/**
 * 
 * created by hcyi on Oct 26, 2015 Detailled comment
 *
 */
public abstract class AbstractComponentDragAndDropHandler extends AbstractDragAndDropServiceHandler {

    protected static final String UNDEFINE = "UNDEFINE"; //$NON-NLS-1$

    protected static final String COMPONENT_PREFIX = "t"; //$NON-NLS-1$

    protected static final String INPUT = "Input"; //$NON-NLS-1$

    protected static final String OUTPUT = "Output"; //$NON-NLS-1$

    protected static final String MAP = "MAP"; //$NON-NLS-1$

    @Override
    public ERepositoryObjectType getType(String repositoryType) {
        return null;
    }

    protected String getInputComponentName(Connection connection) {
        return getInputOrOutputComponentName(connection, true);
    }

    protected String getOutputComponentName(Connection connection) {
        return getInputOrOutputComponentName(connection, false);
    }

    private String getInputOrOutputComponentName(Connection connection, boolean isInput) {
        if (connection == null || connection.getConnectionTypeName() == null) {
            return UNDEFINE;
        }
        if (isInput) {
            return COMPONENT_PREFIX.concat(connection.getConnectionTypeName()).concat(INPUT);
        }
        return COMPONENT_PREFIX.concat(connection.getConnectionTypeName()).concat(OUTPUT);
    }
}
