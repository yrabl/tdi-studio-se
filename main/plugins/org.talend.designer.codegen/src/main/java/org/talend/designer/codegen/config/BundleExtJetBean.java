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
package org.talend.designer.codegen.config;

import org.eclipse.core.runtime.Path;

/**
 * 
 * created by ggu on 2 Mar 2015 Detailled comment
 *
 */
public class BundleExtJetBean extends JetBean implements Cloneable {

    private static final long serialVersionUID = -668847534098310517L;

    private String name;

    public BundleExtJetBean(String jetPluginRepository, String templateRelativeUri, String className, String version) {
        super(jetPluginRepository, templateRelativeUri, className, version, EMPTY);
        this.name = new Path(templateRelativeUri).removeFileExtension().lastSegment();
    }

    public BundleExtJetBean(String jetPluginRepository, String templateRelativeUri, String className) {
        this(jetPluginRepository, templateRelativeUri, className, DEFAULT_VERSION);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public BundleExtJetBean clone() throws CloneNotSupportedException {
        return (BundleExtJetBean) super.clone();
    }

}
