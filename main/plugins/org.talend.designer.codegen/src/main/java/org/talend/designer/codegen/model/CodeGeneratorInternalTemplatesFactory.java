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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.talend.designer.codegen.additionaljet.AbstractJetFileProvider;
import org.talend.designer.codegen.additionaljet.CustomizeJetFilesProviderManager;
import org.talend.designer.codegen.config.BundleTemplateJetBean;

/**
 * Create a list of Available templates in the application.
 * 
 * $Id$
 * 
 */
public class CodeGeneratorInternalTemplatesFactory {

    private List<BundleTemplateJetBean> bundleJetBeans;

    CodeGeneratorInternalTemplatesFactory() {
        init();
    }

    /**
     * init list of templates.
     */
    private void init() {
        this.bundleJetBeans = new ArrayList<BundleTemplateJetBean>();

        retrieveBundleJetBeansFromExtension();
    }

    private void retrieveBundleJetBeansFromExtension() {
        CustomizeJetFilesProviderManager componentsProviderManager = CustomizeJetFilesProviderManager.getInstance();
        final Map<String, AbstractJetFileProvider> providers = componentsProviderManager.getProviders();

        final Map<AbstractJetFileProvider, List<BundleTemplateJetBean>> bundleJetBeansMap = new LinkedHashMap<AbstractJetFileProvider, List<BundleTemplateJetBean>>();
        // retrieve jet beans
        for (AbstractJetFileProvider componentsProvider : providers.values()) {
            List<BundleTemplateJetBean> retrievedJetBeans = componentsProvider.retrieveJetBeans();
            if (retrievedJetBeans != null) {
                bundleJetBeansMap.put(componentsProvider, retrievedJetBeans);
            }
        }

        // process overrides
        for (AbstractJetFileProvider provider : bundleJetBeansMap.keySet()) {
            Map<String, String> overrideElementsMap = provider.getOverrideElementsMap();
            if (overrideElementsMap == null || overrideElementsMap.isEmpty()) {
                continue;
            }
            for (String providerId : overrideElementsMap.keySet()) {
                String overridedFileName = overrideElementsMap.get(providerId);

                if (overridedFileName == null) {
                    continue;
                }
                AbstractJetFileProvider overridedProvider = providers.get(providerId);
                List<BundleTemplateJetBean> overridedProviderJetBeans = bundleJetBeansMap.get(overridedProvider);
                if (overridedProviderJetBeans == null) {
                    continue;
                }
                // find the overrided provider to remove the jet bean.
                Iterator<BundleTemplateJetBean> iterator = overridedProviderJetBeans.iterator();
                while (iterator.hasNext()) {
                    BundleTemplateJetBean jetBean = iterator.next();
                    if (jetBean.getName().equals(overridedFileName)) {
                        iterator.remove();
                    }
                }
            }
        }

        // add all in list.
        for (AbstractJetFileProvider provider : bundleJetBeansMap.keySet()) {
            List<BundleTemplateJetBean> list = bundleJetBeansMap.get(provider);
            if (list != null) {
                this.bundleJetBeans.addAll(list);
            }
        }
    }

    public List<BundleTemplateJetBean> getBundleJetBeans() {
        return this.bundleJetBeans;
    }

}
