// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.codegen.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.codegen.jet.JETException;
import org.osgi.framework.FrameworkUtil;
import org.talend.commons.utils.PasswordEncryptUtil;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentFileNaming;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.temp.ECodePart;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.codegen.CodeGeneratorActivator;
import org.talend.designer.codegen.ICodeGenerator;
import org.talend.designer.codegen.config.BundleTemplateJetBean;
import org.talend.designer.codegen.config.CodeGeneratorArgument;
import org.talend.designer.codegen.config.EInternalTemplate;
import org.talend.designer.codegen.config.JetBean;
import org.talend.designer.codegen.config.TemplateUtil;
import org.talend.designer.codegen.exception.CodeGeneratorException;
import org.talend.designer.codegen.model.CodeGeneratorInternalTemplatesFactoryProvider;
import org.talend.designer.codegen.proxy.JetProxy;
import org.talend.designer.core.model.components.EmfComponent;

/**
 * created by ggu on 24 Mar 2015 Detailled comment
 *
 */
public abstract class AbstractCodeGenerator implements ICodeGenerator {

    protected static Map<EInternalTemplate, JetBean> templateJetBeans = new HashMap<EInternalTemplate, JetBean>();

    protected final Logger log = Logger.getLogger(getClass());

    protected IProcess process;

    protected String currentProjectName, jobName, jobVersion, contextName;

    public AbstractCodeGenerator() {
        //
    }

    public AbstractCodeGenerator(IProcess process) {
        if (process == null) {
            throw new NullPointerException();
        } else {
            this.process = process;
            this.jobName = process.getName();
            this.jobVersion = ""; //$NON-NLS-1$
            if (process.getVersion() != null) {
                this.jobVersion = process.getVersion().replace(".", "_"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            this.contextName = process.getContextManager().getDefaultContext().getName();
        }

    }

    /**
     * 
     * get the template jet bean.
     */
    protected JetBean getTemplateJetBean(EInternalTemplate type) {
        JetBean jetBean = templateJetBeans.get(type);
        if (jetBean == null) {
            List<BundleTemplateJetBean> bundleJetBeans = CodeGeneratorInternalTemplatesFactoryProvider.getInstance()
                    .getBundleJetBeans();
            for (BundleTemplateJetBean bundleBean : bundleJetBeans) {
                if (bundleBean.getName().equals(type.getTemplateName())) {
                    try {
                        jetBean = bundleBean.clone();
                        templateJetBeans.put(type, jetBean);
                        break;
                    } catch (CloneNotSupportedException e) {
                        //
                    }
                }
            }
        }
        if (jetBean == null) { // can't find, should be never null
            throw new RuntimeException();
            // jetBean = new JetBean();
            // jetBean.setJetPluginRepository(getTemplateBundleName());
            // jetBean.setTemplateRelativeUri(TemplateUtil.RESOURCES_DIRECTORY + TemplateUtil.DIR_SEP + type +
            // TemplateUtil.EXT_SEP
            // + ECodeLanguage.JAVA.getExtension() + TemplateUtil.TEMPLATE_EXT);
        }
        return jetBean;
    }

    protected String getTemplateBundleName() {
        return FrameworkUtil.getBundle(this.getClass()).getSymbolicName();
    }

    /**
     * get the node jet bean via argument.
     */
    protected JetBean getNodeJetBean(Object argument) {
        return getNodeJetBean(argument, null);
    }

    protected JetBean getNodeJetBean(Object argument, ECodePart part) {
        JetBean jetBean = new JetBean();

        // by default
        String nodeBundleName = CodeGeneratorActivator.PLUGIN_ID;
        if (argument != null) {
            if (argument instanceof CodeGeneratorArgument) {
                CodeGeneratorArgument codeArgument = (CodeGeneratorArgument) argument;
                if (codeArgument.getArgument() instanceof INode) {
                    final IComponent component = ((INode) codeArgument.getArgument()).getComponent();
                    // set path for component
                    if (component != null && part != null) {
                        IComponentFileNaming componentFileNaming = ComponentsFactoryProvider.getFileNamingInstance();
                        String templateURI = component.getPathSource() + TemplateUtil.DIR_SEP + component.getName()
                                + TemplateUtil.DIR_SEP
                                + componentFileNaming.getJetFileName(component, ECodeLanguage.JAVA.getExtension(), part);

                        jetBean.setTemplateRelativeUri(templateURI);
                    }
                    // depend on the component.
                    if (component != null && component instanceof EmfComponent
                            && ((EmfComponent) component).getSourceBundleName() != null) {
                        nodeBundleName = ((EmfComponent) component).getSourceBundleName();
                    } else { // maybe for fake components.
                        nodeBundleName = IComponentsFactory.COMPONENTS_LOCATION;
                        IBrandingService breaningService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                                IBrandingService.class);
                        if (breaningService.isPoweredOnlyCamel()) {
                            nodeBundleName = IComponentsFactory.CAMEL_COMPONENTS_LOCATION;
                        }
                    }
                }
            }
        }
        jetBean.setJetPluginRepository(nodeBundleName);

        jetBean.setArgument(argument);
        return jetBean;
    }

    /**
     * Parse Process, and generate Code for Context Variables.
     * 
     * @param designerContext the context to generate code from
     * @return the generated code
     * @throws CodeGeneratorException if an error occurs during Code Generation
     */
    @Override
    public String generateContextCode(final IContext designerContext) throws CodeGeneratorException {
        if (process != null) {
            IContext workContext = designerContext;
            if (workContext == null) {
                workContext = process.getContextManager().getDefaultContext();
            }
            List<IContextParameter> listParameters = workContext.getContextParameterList();

            if (listParameters != null) {
                List<IContextParameter> listParametersCopy = new ArrayList<IContextParameter>(listParameters.size());
                CodeGeneratorArgument codeGenArgument = new CodeGeneratorArgument();
                // encrypt the password
                for (IContextParameter iContextParameter : listParameters) {
                    if (PasswordEncryptUtil.isPasswordType(iContextParameter.getType())) {
                        IContextParameter icp = iContextParameter.clone();
                        String pwd = icp.getValue();
                        if (pwd != null && !pwd.isEmpty()) {
                            try {
                                icp.setValue(PasswordEncryptUtil.encryptPasswordHex(pwd));
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                        listParametersCopy.add(icp);
                    } else {
                        listParametersCopy.add(iContextParameter);
                    }
                }

                codeGenArgument.setNode(listParametersCopy);
                codeGenArgument.setContextName(workContext.getName());
                codeGenArgument.setCurrentProjectName(currentProjectName);
                codeGenArgument.setJobName(jobName);
                codeGenArgument.setJobVersion(jobVersion);

                codeGenArgument.setIsRunInMultiThread(isRunInMultiThread());
                codeGenArgument.setPauseTime(CorePlugin.getDefault().getRunProcessService().getPauseTime());

                JetBean jetBean = getTemplateJetBean(EInternalTemplate.CONTEXT);
                if (jetBean != null) {
                    jetBean.setArgument(codeGenArgument);
                    JetProxy proxy = new JetProxy(jetBean);
                    String content;
                    try {
                        content = proxy.generate();
                    } catch (JETException e) {
                        log.error(e.getMessage(), e);
                        throw new CodeGeneratorException(e);
                    } catch (CoreException e) {
                        log.error(e.getMessage(), e);
                        throw new CodeGeneratorException(e);
                    }
                    return content;
                }
            }
        }
        return ""; //$NON-NLS-1$
    }

    protected boolean isRunInMultiThread() {
        boolean running = false;
        // check the mutli-thread parameter in Job Settings.
        if (process != null) {
            IElementParameter parameter = process.getElementParameter("MULTI_THREAD_EXECATION"); //$NON-NLS-1$
            if (parameter != null) {
                Object obj = parameter.getValue();
                if (obj instanceof Boolean && ((Boolean) obj).booleanValue()) {
                    running = true;
                }
            }
        }
        return running;
    }

    protected StringBuffer instantiateJetProxy(JetBean jetBean) throws CodeGeneratorException {
        JetProxy proxy = new JetProxy(jetBean);
        StringBuffer content = new StringBuffer();
        try {
            content.append(proxy.generate());
        } catch (JETException e) {
            log.error(e.getMessage(), e);
            throw new CodeGeneratorException(e);
        } catch (CoreException e) {
            log.error(e.getMessage(), e);
            throw new CodeGeneratorException(e);
        }
        return content;
    }

}
