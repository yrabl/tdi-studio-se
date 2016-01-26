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
package org.talend.designer.codegen;

import org.eclipse.core.runtime.jobs.Job;
import org.talend.commons.CommonsPlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ILibraryManagerService;
import org.talend.core.model.components.IComponentsService;
import org.talend.core.model.general.ILibrariesService;
import org.talend.core.model.process.IProcess;
import org.talend.core.ui.services.IComponentsLocalProviderService;
import org.talend.designer.codegen.components.model.ComponentCompilations;
import org.talend.designer.codegen.model.CodeGeneratorEmittersPoolFactory;
import org.talend.designer.core.IDesignerCoreService;

/**
 * DOC bqian class global comment. Provides services for CodeGenerator plugin. <br/>
 * 
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (星期五, 29 九月 2006) nrousseau $
 * 
 */
public class CodeGeneratorService implements ICodeGeneratorService {

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.codegen.ICodeGeneratorFactory#getCodeGenerator()
     */
    @Override
    public ICodeGenerator createCodeGenerator() {
        return new CodeGenerator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.codegen.ICodeGeneratorFactory#getCodeGenerator(org.talend.core.model.process.IProcess,
     * boolean, boolean, boolean, java.lang.String)
     */
    @Override
    public ICodeGenerator createCodeGenerator(IProcess process, boolean statistics, boolean trace, String... options) {
        ICodeGeneratorService codeGenService = null;

        codeGenService = CodeGeneratorService.getService(ISparkCodeGeneratorService.class);
        if (codeGenService != null && ((ISparkCodeGeneratorService) codeGenService).validProcess(process)) {
            return codeGenService.createCodeGenerator(process, statistics, trace, options);
        }

        codeGenService = CodeGeneratorService.getService(ISparkStreamingCodeGeneratorService.class);
        if (codeGenService != null && ((ISparkStreamingCodeGeneratorService) codeGenService).validProcess(process)) {
            return codeGenService.createCodeGenerator(process, statistics, trace, options);
        }

        codeGenService = CodeGeneratorService.getService(IMRCodeGeneratorService.class);
        if (codeGenService != null && ((IMRCodeGeneratorService) codeGenService).validProcess(process)) {
            return codeGenService.createCodeGenerator(process, statistics, trace, options);
        }

        codeGenService = CodeGeneratorService.getService(ICamelCodeGeneratorService.class);
        if (codeGenService != null && ((ICamelCodeGeneratorService) codeGenService).validProcess(process)) {
            return codeGenService.createCodeGenerator(process, statistics, trace, options);
        }

        codeGenService = CodeGeneratorService.getService(IStormCodeGeneratorService.class);
        if (codeGenService != null && ((IStormCodeGeneratorService) codeGenService).validProcess(process)) {
            return codeGenService.createCodeGenerator(process, statistics, trace, options);
        }

        return new CodeGenerator(process, statistics, trace, options);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.codegen.ICodeGeneratorService#initializeTemplates(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public Job initializeTemplates() {
        return CodeGeneratorEmittersPoolFactory.initialize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.codegen.ICodeGeneratorService#refreshTemplates()
     */
    @Override
    public Job refreshTemplates() {

        ComponentCompilations.deleteMarkers();
        ((IComponentsService) GlobalServiceRegister.getDefault().getService(IComponentsService.class)).getComponentsFactory()
                .resetCache();
        ILibraryManagerService librairesManagerService = (ILibraryManagerService) GlobalServiceRegister.getDefault().getService(
                ILibraryManagerService.class);
        librairesManagerService.clearCache();
        ((ILibrariesService) GlobalServiceRegister.getDefault().getService(ILibrariesService.class)).syncLibraries();
        Job job = CodeGeneratorEmittersPoolFactory.initialize();
        // achen modify to record ctrl+shift+f3 is pressed to fix bug 0006107
        IDesignerCoreService designerCoreService = (IDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                IDesignerCoreService.class);
        designerCoreService.getLastGeneratedJobsDateMap().clear();
        if (!CommonsPlugin.isHeadless()) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IComponentsLocalProviderService.class)) {
                IComponentsLocalProviderService service = (IComponentsLocalProviderService) GlobalServiceRegister.getDefault()
                        .getService(IComponentsLocalProviderService.class);
                if (service != null) {
                    service.synchronizeDesignerUIAfterRefreshTemplates();
                }
            }
        }
        return job;
    }

    @Override
    public boolean isInitializingJet() {
        return !CodeGeneratorEmittersPoolFactory.isInitialized() && CodeGeneratorEmittersPoolFactory.isInitializeStart();
    }

    private static ICodeGeneratorService getService(Class<? extends ICodeGeneratorService> klass) {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(klass)) {
            return (ICodeGeneratorService) GlobalServiceRegister.getDefault().getService(klass);
        }
        return null;
    }
}
