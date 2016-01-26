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
package org.talend.component.ui.wizard.view.content;

import org.eclipse.swt.graphics.Image;
import org.talend.component.ui.wizard.util.GenericWizardServiceFactory;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.viewer.label.RepositoryViewLabelProvider;

/**
 * created by ycbai on 2015年9月14日 Detailled comment
 *
 */
public class MetadataGenericLabelProvider extends RepositoryViewLabelProvider {

    @Override
    public Image getImage(Object element) {
        if (element instanceof RepositoryNode) {
            RepositoryNode node = (RepositoryNode) element;
            ERepositoryObjectType repObjType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
            if (repObjType != null) {
                return GenericWizardServiceFactory.getGenericWizardService().getNodeImage(repObjType.getType());
            }

        }
        return super.getImage(element);
    }

}
