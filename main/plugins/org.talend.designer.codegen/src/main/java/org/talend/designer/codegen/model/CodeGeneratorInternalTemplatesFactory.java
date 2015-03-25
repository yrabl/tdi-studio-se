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
import org.talend.designer.codegen.config.BundleExtJetBean;
import org.talend.designer.codegen.config.BundleJetSkeletonBean;

/**
 * Create a list of Available templates in the application.
 * 
 * $Id$
 * 
 */
public class CodeGeneratorInternalTemplatesFactory {

    private List<BundleExtJetBean> extJetBeans;

    private List<BundleJetSkeletonBean> jetSkeletonBeans;

    CodeGeneratorInternalTemplatesFactory() {
        init();
    }

    /**
     * init list of templates.
     */
    private void init() {
        this.extJetBeans = new ArrayList<BundleExtJetBean>();
        this.jetSkeletonBeans = new ArrayList<BundleJetSkeletonBean>();

        retrieveBundleJetBeansFromExtension();
    }

    private void retrieveBundleJetBeansFromExtension() {
        CustomizeJetFilesProviderManager jetsProviderManager = CustomizeJetFilesProviderManager.getInstance();
        final Map<String, AbstractJetFileProvider> providers = jetsProviderManager.getProviders();

        final Map<AbstractJetFileProvider, List<BundleExtJetBean>> bundleJetBeansMap = new LinkedHashMap<AbstractJetFileProvider, List<BundleExtJetBean>>();
        // retrieve jet beans
        for (AbstractJetFileProvider jetProvider : providers.values()) {
            List<BundleExtJetBean> retrievedExtJetBeans = jetProvider.retrieveExtJetBeans();
            if (retrievedExtJetBeans != null) {
                bundleJetBeansMap.put(jetProvider, retrievedExtJetBeans);
            }
            List<BundleJetSkeletonBean> retrievedJetSkeletonBeans = jetProvider.retrieveJetSkeletonBeans();
            if (retrievedJetSkeletonBeans != null) {
                this.jetSkeletonBeans.addAll(retrievedJetSkeletonBeans);
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
                List<BundleExtJetBean> overridedProviderJetBeans = bundleJetBeansMap.get(overridedProvider);
                if (overridedProviderJetBeans == null) {
                    continue;
                }
                // find the overrided provider to remove the jet bean.
                Iterator<BundleExtJetBean> iterator = overridedProviderJetBeans.iterator();
                while (iterator.hasNext()) {
                    BundleExtJetBean jetBean = iterator.next();
                    if (jetBean.getName().equals(overridedFileName)) {
                        iterator.remove();
                    }
                }
            }
        }

        // add all in list.
        for (AbstractJetFileProvider provider : bundleJetBeansMap.keySet()) {
            List<BundleExtJetBean> list = bundleJetBeansMap.get(provider);
            if (list != null) {
                this.extJetBeans.addAll(list);
            }
        }

    }

    public List<BundleExtJetBean> getBundleExtJetBeans() {
        return this.extJetBeans;
    }

    public List<BundleJetSkeletonBean> getBundleJetSkeletonBeans() {
        return this.jetSkeletonBeans;
    }

}
