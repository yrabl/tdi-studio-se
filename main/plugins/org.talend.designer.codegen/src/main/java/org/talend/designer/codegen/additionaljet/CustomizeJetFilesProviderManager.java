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
package org.talend.designer.codegen.additionaljet;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.talend.designer.codegen.i18n.Messages;

/***/
public final class CustomizeJetFilesProviderManager {

    private static Logger log = Logger.getLogger(CustomizeJetFilesProviderManager.class);

    private static final CustomizeJetFilesProviderManager INSTANCE = new CustomizeJetFilesProviderManager();

    private Map<String, AbstractJetFileProvider> providers;

    private CustomizeJetFilesProviderManager() {
    }

    public static CustomizeJetFilesProviderManager getInstance() {
        return INSTANCE;
    }

    public Map<String, AbstractJetFileProvider> getProviders() {
        if (providers == null) {
            loadJetsProvidersFromExtension();
        }
        return providers;
    }

    @SuppressWarnings("nls")
    private void loadJetsProvidersFromExtension() {
        providers = new LinkedHashMap<String, AbstractJetFileProvider>();

        IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint("org.talend.designer.codegen.additional_jetfile");
        IExtension[] extensions = extensionPoint.getExtensions();
        for (IExtension extension : extensions) {
            IConfigurationElement[] configurationElements = extension.getConfigurationElements();
            for (IConfigurationElement configurationElement : configurationElements) {
                String id = configurationElement.getAttribute("id");
                try {
                    AbstractJetFileProvider jetProvider = (AbstractJetFileProvider) configurationElement
                            .createExecutableExtension("class");
                    jetProvider.setId(id);
                    jetProvider.setBundleId(configurationElement.getContributor().getName());

                    final AbstractJetFileProvider existedOne = providers.get(id);
                    if (existedOne != null) { // existed same id one.
                        throw new RuntimeException(Messages.getString("CustomizeJetFilesProviderManager.sameIdForProvider", id,
                                existedOne.getBundleId(), jetProvider.getBundleId()));
                    }
                    providers.put(id, jetProvider);

                    // overrides
                    IConfigurationElement[] overridesChildren = configurationElement.getChildren("overrides");
                    if (overridesChildren != null) {
                        for (IConfigurationElement c : overridesChildren) {
                            IConfigurationElement[] overrideElementChildren = c.getChildren("overrideElement");
                            if (overrideElementChildren != null) {
                                for (IConfigurationElement element : overrideElementChildren) {
                                    String providerId = element.getAttribute("providerId");
                                    String fileName = element.getAttribute("fileName");
                                    if (providerId != null && providerId.trim().length() > 0 && fileName != null
                                            && fileName.trim().length() > 0) {
                                        jetProvider.getOverrideElementsMap().put(providerId.trim(), fileName.trim());
                                    }
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    log.error(Messages.getString("JetFilesProviderManager.unableLoad", id), e); //$NON-NLS-1$
                }
            }
        }
    }

}
