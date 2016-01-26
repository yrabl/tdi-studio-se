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
package org.talend.repository.view.di.viewer.tester;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.tester.AbstractNodeTester;

public class NodeTester extends AbstractNodeTester {

    /**
     * 
     */
    private static final String ALWAYS_FALSE = "alwaysFalse"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String IS_BUSINESS_MODEL_BRANCH = "isBusinessModelBranch"; //$NON-NLS-1$

    /**
     * property used to check if object is a job
     */
    public static final String IS_JOB_BRANCH = "isJobBranch"; //$NON-NLS-1$

    @Override
    protected Boolean testProperty(Object receiver, String property, Object[] args, Object expectedValue) {
        if (receiver instanceof RepositoryNode) {
            RepositoryNode repositoryNode = (RepositoryNode) receiver;
            if (ALWAYS_FALSE.equals(property)) {
                return false;
            }
            if (IS_JOB_BRANCH.equals(property)) {
                return isJobBranch(repositoryNode);
            }

            if (IS_BUSINESS_MODEL_BRANCH.equals(property)) {
                return isBusinessModelBranch(repositoryNode);
            }
        }
        return null;
    }

    /**
     * DOC sgandon Comment method "isBusinessModel".
     * 
     * @param repositoryNode
     */
    public boolean isBusinessModelBranch(RepositoryNode repositoryNode) {
        // && repositoryNode.getType() == ENodeType.REPOSITORY_ELEMENT;
        return isTypeNode(repositoryNode, ERepositoryObjectType.BUSINESS_PROCESS);

    }

    /**
     * DOC sgandon Comment method "isJob".
     * 
     * @param repositoryNode
     * @return
     */
    public boolean isJobBranch(RepositoryNode repositoryNode) {
        return isTypeNode(repositoryNode, ERepositoryObjectType.PROCESS);
    }

}
