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
import org.osgi.framework.FrameworkUtil;
import org.talend.commons.utils.PasswordEncryptUtil;
import org.talend.core.CorePlugin;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess;
import org.talend.designer.codegen.ICodeGenerator;
import org.talend.designer.codegen.config.BundleExtJetBean;
import org.talend.designer.codegen.config.CodeGeneratorArgument;
import org.talend.designer.codegen.config.EInternalTemplate;
import org.talend.designer.codegen.config.JetBean;
import org.talend.designer.codegen.exception.CodeGeneratorException;
import org.talend.designer.codegen.model.CodeGeneratorInternalTemplatesFactoryProvider;

/**
 * created by ggu on 24 Mar 2015 Detailled comment
 *
 */
public abstract class AbstractCodeGenerator implements ICodeGenerator {

    private static Map<EInternalTemplate, JetBean> templateJetBeans = new HashMap<EInternalTemplate, JetBean>();

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
            List<BundleExtJetBean> bundleJetBeans = CodeGeneratorInternalTemplatesFactoryProvider.getInstance()
                    .getBundleExtJetBeans();
            for (BundleExtJetBean bundleBean : bundleJetBeans) {
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
            // 1?
            // if the Internal Template is deleted but won't remove in the enum. so comment this.
            // throw new RuntimeException();

            // 2?
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

                return JetGeneratorUtil.jetGenerate(jetBean, codeGenArgument);

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

}
