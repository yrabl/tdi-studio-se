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
package org.talend.designer.codegen.model;

import java.util.ArrayList;
import java.util.List;

import org.talend.designer.codegen.additionaljet.AbstractJetFileProvider;
import org.talend.designer.codegen.additionaljet.CustomizeJetFilesProviderManager;
import org.talend.designer.codegen.model.template.BundleJetTemplate;

/**
 * Create a list of Available templates in the application.
 * 
 * $Id$
 * 
 */
public class CodeGeneratorInternalTemplatesFactory {

    private List<BundleJetTemplate> bundleJetTemplates;

    /**
     * Constructor.
     */
    public CodeGeneratorInternalTemplatesFactory() {
    }

    /**
     * init list of templates.
     */
    public void init() {
        bundleJetTemplates = new ArrayList<BundleJetTemplate>();
        retrieveBundleTemplatesFromExtension();
    }

    private void retrieveBundleTemplatesFromExtension() {
        CustomizeJetFilesProviderManager componentsProviderManager = CustomizeJetFilesProviderManager.getInstance();
        for (AbstractJetFileProvider componentsProvider : componentsProviderManager.getProviders()) {
            List<BundleJetTemplate> retrievedTempaltes = componentsProvider.retrieveTempaltes();
            if (retrievedTempaltes != null) {
                this.bundleJetTemplates.addAll(retrievedTempaltes);
            }
        }
    }

    public List<BundleJetTemplate> getBundleJetTemplates() {
        return this.bundleJetTemplates;
    }

}
