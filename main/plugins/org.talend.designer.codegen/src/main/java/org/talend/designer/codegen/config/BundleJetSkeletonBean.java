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

/**
 * created by ggu on 25 Mar 2015 Detailled comment
 *
 */
public class BundleJetSkeletonBean extends JetBean {

    private static final long serialVersionUID = 3056911086854877951L;

    public BundleJetSkeletonBean(String jetPluginRepository, String templateRelativeUri) {
        super(jetPluginRepository, templateRelativeUri, EMPTY, EMPTY, EMPTY);
    }

}
