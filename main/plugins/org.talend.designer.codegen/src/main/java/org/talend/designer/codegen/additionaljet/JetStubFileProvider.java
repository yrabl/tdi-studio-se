// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
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
 * created by ggu on Sep 22, 2014 Detailled comment
 *
 */
public class JetStubFileProvider extends AbstractJetFileProvider {

    // @Override
    // public String getBundleId() {
    // return CodeGeneratorActivator.PLUGIN_ID;
    // }

    @Override
    protected IPath getBasePath() {
        return new Path("jet_stub"); //$NON-NLS-1$
    }

    // @Override
    // protected ClassLoader getJetTempalteClassLoader() {
    // // same as ComponentsFactoryProviderManager.getInstance().getClass().getClassLoader();
    // return this.getClass().getClassLoader();
    // }

}
