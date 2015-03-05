// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.codegen.additionaljet;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * created by ggu on 19 Mar 2015 Detailled comment
 *
 */
public class AbstractAdditionalJetFileProvider extends AbstractJetFileProvider {

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.codegen.additionaljet.AbstractJetFileProvider#getBasePath()
     */
    @Override
    protected IPath getBasePath() {
        return new Path("additional"); //$NON-NLS-1$
    }

}
