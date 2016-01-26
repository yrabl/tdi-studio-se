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
package org.talend.designer.codegen.components.prefs;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.talend.designer.codegen.CodeGeneratorActivator;

/**
 * DOC zli class global comment. Detailled comment
 */
public class ComponentPreferenceInitializer extends AbstractPreferenceInitializer {

    public ComponentPreferenceInitializer() {
    }

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences preferenceStore = InstanceScope.INSTANCE.getNode(CodeGeneratorActivator.PLUGIN_ID);

        preferenceStore.put(IComponentPreferenceConstant.LIMIT, "1000"); //$NON-NLS-1$

        preferenceStore.put(IComponentPreferenceConstant.LINK_STYLE, LINK_STYLE.AUTO.toString());
    }
}
