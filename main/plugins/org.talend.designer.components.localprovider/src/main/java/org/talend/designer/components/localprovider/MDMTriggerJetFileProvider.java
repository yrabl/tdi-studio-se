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
package org.talend.designer.components.localprovider;

import org.eclipse.core.runtime.IPath;
import org.talend.designer.codegen.additionaljet.AbstractResourcesJetFileProvider;

/**
 * @author rdubois
 *
 */
public class MDMTriggerJetFileProvider extends AbstractResourcesJetFileProvider {

    // @Override
    // public String getBundleId() {
    // return ComponentsLocalProviderPlugin.PLUGIN_ID;
    // }

    @Override
    protected IPath getBasePath() {
        return super.getBasePath().append("mdmTrigger"); //$NON-NLS-1$
    }

}
