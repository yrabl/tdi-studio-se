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
package org.talend.designer.codegen.model;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.language.ECodeLanguage;
import org.talend.designer.codegen.CodeGeneratorActivator;
import org.talend.designer.codegen.additionaljet.AbstractJetFileProvider;
import org.talend.designer.codegen.additionaljet.CustomizeJetFilesProviderManager;
import org.talend.designer.codegen.config.EInternalTemplate;
import org.talend.designer.codegen.config.TemplateUtil;

/**
 * Create a list of Available templates in the application.
 * 
 * $Id$
 * 
 */
public class CodeGeneratorInternalTemplatesFactory {

    private List<TemplateUtil> templates;

    /**
     * Constructor.
     */
    public CodeGeneratorInternalTemplatesFactory() {
    }

    /**
     * init list of templates.
     */
    public void init() {
        templates = new ArrayList<TemplateUtil>();

        File installationFolder = null;
        URL url = FileLocator.find(Platform.getBundle(CodeGeneratorActivator.PLUGIN_ID), new Path(
                ICodegenConstants.PATH_RESOURCES), null);
        try {
            if (url != null) {
                installationFolder = new File(FileLocator.toFileURL(url).getPath());
                if (installationFolder.exists()) {
                    org.talend.utils.io.FilesUtils.deleteFolder(installationFolder, true);
                }
            } else {
                url = Platform.getBundle(CodeGeneratorActivator.PLUGIN_ID).getEntry("/");//$NON-NLS-1$
                installationFolder = new File(FileLocator.toFileURL(url).getPath(), ICodegenConstants.PATH_RESOURCES);
            }
            installationFolder.mkdirs();
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }

        // 0. clear the content of the "header_additional.javajet"
        copyStubAdditionalJetFile();

        // 1. copy additional jet file from extension point: additional_jetfile
        copyAdditionalJetFileFromProviderExtension();

        // 2. Load system frame:
        loadSystemFrame();

    }

    private void copyStubAdditionalJetFile() {
        try {
            File stubForder = null;
            URL url = null;
            url = FileLocator.find(Platform.getBundle(CodeGeneratorActivator.PLUGIN_ID),
                    new Path(ICodegenConstants.PATH_JET_STUB), null); //$NON-NLS-1$
            stubForder = new File(FileLocator.toFileURL(url).getPath());

            File installationFolder = null;
            url = FileLocator.find(Platform.getBundle(CodeGeneratorActivator.PLUGIN_ID), new Path(
                    ICodegenConstants.PATH_RESOURCES), null); //$NON-NLS-1$
            installationFolder = new File(FileLocator.toFileURL(url).getPath());

            final FileFilter sourceFolderFilter = new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return false;
                }
            };

            FilesUtils.copyFolder(stubForder, installationFolder, false, sourceFolderFilter, null, true);

        } catch (IOException e) {
            ExceptionHandler.process(e);
        }

    }

    private void loadSystemFrame() {
        templates.clear();
        URL url = FileLocator.find(Platform.getBundle(CodeGeneratorActivator.PLUGIN_ID), new Path(
                ICodegenConstants.PATH_RESOURCES), null);
        File file;
        try {
            for (EInternalTemplate utilTemplate : EInternalTemplate.values()) {
                file = new File(FileLocator.toFileURL(url).getPath() + utilTemplate.getTemplateName() + TemplateUtil.EXT_SEP
                        + ECodeLanguage.JAVA.getExtension() + TemplateUtil.TEMPLATE_EXT);
                if (file.exists()) {
                    TemplateUtil template = new TemplateUtil(utilTemplate);
                    templates.add(template);
                }
            }
            // Add all additional headers
            file = new File(FileLocator.toFileURL(url).getPath());
            for (File f : file.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    if (pathname.getName().contains(EInternalTemplate.HEADER_ADDITIONAL.toString())) {
                        if (pathname.getName().contains(ECodeLanguage.JAVA.getExtension() + TemplateUtil.TEMPLATE_EXT)) {
                            return true;
                        }
                    }
                    return false;
                }
            })) {
                if (f.exists()) {
                    TemplateUtil template = new TemplateUtil(f.getName().substring(0, f.getName().lastIndexOf(".")), "0.0.1");
                    if (!templates.contains(template)) {
                        templates.add(template);
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            ExceptionHandler.process(e);
        }
    }

    private void copyAdditionalJetFileFromProviderExtension() {
        CustomizeJetFilesProviderManager componentsProviderManager = CustomizeJetFilesProviderManager.getInstance();
        for (AbstractJetFileProvider componentsProvider : componentsProviderManager.getProviders()) {
            try {
                componentsProvider.overwriteStubAdditionalFile();
            } catch (IOException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    /**
     * Return list of available templates.
     * 
     * @return
     */
    public List<TemplateUtil> getTemplates() {
        return templates;
    }

}
