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
package org.talend.repository.view.di.viewer.content.doc;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.view.di.viewer.tester.doc.GeneratedDocNodeTester;
import org.talend.repository.viewer.content.SubEmptyTopNodeContentProvider;

/**
 * DOC ggu class global comment. Detailled comment
 */
public abstract class AbstractGeneratedDocContentProvider extends SubEmptyTopNodeContentProvider {

    GeneratedDocNodeTester nodeTester = new GeneratedDocNodeTester();

    public AbstractGeneratedDocContentProvider() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.viewer.content.SingleTopLevelContentProvider#isRootNodeType(java.lang.Object)
     */
    @Override
    protected boolean isRootNodeType(Object element) {
        if (element instanceof RepositoryNode) {
            return nodeTester.isGeneratedDocTopNode((RepositoryNode) element);
        } else {
            return false;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.repository.viewer.content.ProjectRepoAbstractContentProvider#getTopLevelNodeFromProjectRepositoryNode
     * (org.talend.repository.model.ProjectRepositoryNode)
     */
    @Override
    protected RepositoryNode getTopLevelNodeFromProjectRepositoryNode(ProjectRepositoryNode projectNode) {
        return projectNode.getRootRepositoryNode(getTopLevelNodeType());
    }

    @Override
    public void resetTopLevelNode(RepositoryNode aTopLevelNode) {
        // not need re-init it.
        // super.initAndClear();
    }

    abstract protected ERepositoryObjectType getTopLevelNodeType();

}
