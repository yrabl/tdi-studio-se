package org.talend.designer.components.localprovider;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.talend.designer.codegen.additionaljet.AbstractJetFileProvider;

public class BoxJetFileProvider extends AbstractJetFileProvider {

    // @Override
    // public String getBundleId() {
    // return ComponentsLocalProviderPlugin.PLUGIN_ID;
    // }

    @Override
    protected IPath getBasePath() {
        return new Path("resources/box"); //$NON-NLS-1$
    }

}