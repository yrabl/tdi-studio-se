package org.talend.designer.components.localprovider;

import org.eclipse.core.runtime.IPath;
import org.talend.designer.codegen.additionaljet.AbstractResourcesJetFileProvider;

public class BoxJetFileProvider extends AbstractResourcesJetFileProvider {

    // @Override
    // public String getBundleId() {
    // return ComponentsLocalProviderPlugin.PLUGIN_ID;
    // }

    @Override
    protected IPath getBasePath() {
        return super.getBasePath().append("box"); //$NON-NLS-1$
    }

}