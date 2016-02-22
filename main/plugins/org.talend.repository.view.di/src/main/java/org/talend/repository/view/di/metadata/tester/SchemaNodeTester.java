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
package org.talend.repository.view.di.metadata.tester;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.tester.SubNodeTester;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class SchemaNodeTester extends SubNodeTester {

    private static final String IS_SCHEMA = "isSchema"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.tester.AbstractNodeTester#testProperty(java.lang.Object, java.lang.String,
     * java.lang.Object[], java.lang.Object)
     */
    @Override
    protected Boolean testProperty(Object receiver, String property, Object[] args, Object expectedValue) {
        if (receiver instanceof RepositoryNode) {
            RepositoryNode repositoryNode = (RepositoryNode) receiver;
            if (IS_SCHEMA.equals(property)) {
                return isSchema(repositoryNode);
            }
        }
        return null;
    }

    public boolean isSchema(RepositoryNode repositoryNode) {
        return isTypeNode(repositoryNode, ERepositoryObjectType.METADATA_CON_TABLE);
    }

    @Override
    public ERepositoryObjectType findParentItemType(RepositoryNode repositoryNode) {
        final ERepositoryObjectType objectType = repositoryNode.getObjectType();
        if (objectType == ERepositoryObjectType.METADATA_CON_TABLE) {
            if (repositoryNode.getObject() != null) {
                ERepositoryObjectType parentType = null;
                if (repositoryNode.getObject() instanceof MetadataTableRepositoryObject) {
                    MetadataTableRepositoryObject object = (MetadataTableRepositoryObject) repositoryNode.getObject();
                    String itemId = object.getViewObject().getId();
                    parentType = ERepositoryObjectType.getTypeCacheById().get(itemId);
                    if (parentType == null) {
                        parentType = ERepositoryObjectType.getType(object.getViewObject().getProperty());
                    }
                } else {
                    parentType = ERepositoryObjectType.getType(repositoryNode.getObject().getProperty());
                }
                return parentType;
            }
        }
        return null;
    }

}
