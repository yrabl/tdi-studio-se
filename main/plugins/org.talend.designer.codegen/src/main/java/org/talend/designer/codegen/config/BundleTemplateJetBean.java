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
import org.talend.core.language.ECodeLanguage;

/**
 * 
 * created by ggu on 2 Mar 2015 Detailled comment
 *
 */
public class BundleTemplateJetBean extends JetBean implements Cloneable {

    private static final long serialVersionUID = -668847534098310517L;

    public static final String DEFAULT_VERSION = "0.0.1"; //$NON-NLS-1$

    private String name;

    public BundleTemplateJetBean(String jetPluginRepository, String templateRelativeUri, String className, String version) {
        super(jetPluginRepository, templateRelativeUri, className, version, ECodeLanguage.JAVA.getName(), EMPTY);
        this.name = new Path(templateRelativeUri).removeFileExtension().lastSegment();
    }

    public BundleTemplateJetBean(String jetPluginRepository, String templateRelativeUri, String className) {
        this(jetPluginRepository, templateRelativeUri, className, DEFAULT_VERSION);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public BundleTemplateJetBean clone() throws CloneNotSupportedException {
        return (BundleTemplateJetBean) super.clone();
    }

}
