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
package org.talend.designer.codegen.ui.service;

import java.beans.PropertyChangeEvent;
import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.core.CorePlugin;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.process.Element;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.core.ui.services.IComponentsLocalProviderService;
import org.talend.core.views.IComponentSettingsView;
import org.talend.designer.codegen.CodeGeneratorActivator;
import org.talend.designer.codegen.components.model.ComponentFileChecker;
import org.talend.designer.codegen.components.prefs.IComponentPreferenceConstant;
import org.talend.designer.codegen.ui.CodegenUiPlugin;
import org.talend.designer.codegen.ui.i18n.Messages;

/**
 * DOC Administrator class global comment. Detailled comment <br/>
 * 
 * @author ftang, 17/08, 2007
 * 
 */
public class ComponentsLocalProviderService implements IComponentsLocalProviderService {

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.components.IComponentsLocalProviderService#isAvoidToShowJobAfterDoubleClick()
     */
    @Override
    public boolean isAvoidToShowJobAfterDoubleClick() {
        return CodegenUiPlugin.getDefault().getPreferenceStore().getBoolean(IComponentPreferenceConstant.IS_AVOID);
    }

    @Override
    public boolean isAvoidToShowJobletAfterDoubleClick() {
        return CodegenUiPlugin.getDefault().getPreferenceStore().getBoolean(IComponentPreferenceConstant.IS_AVOID_JOBLET);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.components.IComponentsLocalProviderService#getPreferenceStore()
     */
    @Override
    public IPreferenceStore getPreferenceStore() {
        return CodegenUiPlugin.getDefault().getPreferenceStore();
    }

    @Override
    public void setPreferenceStoreValue(String key, Object value) {
        if (key != null) {
            if (value != null) {
                if (value instanceof String) {
                    getPreferenceStore().setValue(key, (String) value);
                } else if (value instanceof Boolean) {
                    getPreferenceStore().setValue(key, (Boolean) value);
                } else if (value instanceof Integer) {
                    getPreferenceStore().setValue(key, (Integer) value);
                }
            }
        }
    }

    @Override
    public boolean validateComponent(String componentFolder, ECodeLanguage language) {
        if (componentFolder != null && language != null) {
            File folder = new File(componentFolder);
            if (folder.exists() && folder.isDirectory()) {
                try {
                    ComponentFileChecker.checkComponentFolder(folder, language.getName().toLowerCase());
                    return true; // It's ok
                } catch (BusinessException e) {
                    final BusinessException tempE = e;
                    Display.getDefault().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            Status status = new Status(IStatus.ERROR, CodeGeneratorActivator.PLUGIN_ID, 1, tempE.getMessage(),
                                    tempE.getCause());
                            ErrorDialog dlg = new ErrorDialog(
                                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                    Messages.getString("ComponentsLocalProviderService.talendExchangeError"), Messages.getString("ComponentsLocalProviderService.componentLoadError"), status, IStatus.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
                            dlg.open();
                        }

                    });

                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.services.IComponentsLocalProviderService#clearComponentIconImages()
     */
    @Override
    public void clearComponentIconImages() {
        CoreImageProvider.clearComponentIconImages();
    }

    @Override
    public void showJetEmitterGenerationCancelMessage() {
        if (!CommonsPlugin.isHeadless()) {
            Display.getDefault().syncExec(new Runnable() {

                @Override
                public void run() {
                    MessageDialog.openError(Display.getDefault().getActiveShell(),
                            Messages.getString("CodeGeneratorEmittersPoolFactory.operationCanceled"), //$NON-NLS-1$
                            Messages.getString("CodeGeneratorEmittersPoolFactory.dialogContent")); //$NON-NLS-1$

                }
            });
        }
        return;

    }

    @Override
    public void synchronizeDesignerUIAfterRefreshTemplates() {
        // TDI-25866:In case select a component and sctrl+shift+f3,need clean its componentSetting view
        Element oldComponent = null;
        IComponentSettingsView viewer = null;
        IWorkbenchWindow wwindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (wwindow != null && wwindow.getActivePage() != null) {
            viewer = (IComponentSettingsView) wwindow.getActivePage().findView(IComponentSettingsView.ID);

            if (viewer != null) {
                oldComponent = viewer.getElement();
                viewer.cleanDisplay();
            }
        }
        if (oldComponent != null && viewer != null) {
            viewer.setElement(oldComponent);
        }

        CorePlugin.getDefault().getDesignerCoreService()
                .synchronizeDesignerUI(new PropertyChangeEvent(this, IComponentConstants.NORMAL, null, null));
    }

}
