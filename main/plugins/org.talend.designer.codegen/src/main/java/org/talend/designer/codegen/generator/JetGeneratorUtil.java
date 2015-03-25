package org.talend.designer.codegen.generator;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.codegen.jet.JETException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentFileNaming;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.process.INode;
import org.talend.core.model.temp.ECodePart;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.codegen.CodeGeneratorActivator;
import org.talend.designer.codegen.config.CodeGeneratorArgument;
import org.talend.designer.codegen.config.JetBean;
import org.talend.designer.codegen.config.TemplateUtil;
import org.talend.designer.codegen.exception.CodeGeneratorException;
import org.talend.designer.codegen.i18n.Messages;
import org.talend.designer.codegen.model.CodeGeneratorEmittersPoolFactory;
import org.talend.designer.codegen.proxy.JetProxy;
import org.talend.designer.core.model.components.EmfComponent;

public class JetGeneratorUtil {

    private static Logger log = Logger.getLogger(JetGeneratorUtil.class);

    private static final long INIT_TIMEOUT = 15L * 60 * 1000; // 15s

    private static final long INIT_PAUSE = 1000L; // 1s

    /**
     * get the node jet bean via argument.
     */
    protected JetBean createJetBean(Object argument) {
        return createJetBean(argument, null);
    }

    public static JetBean createJetBean(Object argument, ECodePart part) {
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

                        jetBean.setRelativeUri(templateURI);
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
        jetBean.setJetBundle(nodeBundleName);

        jetBean.setArgument(argument);
        return jetBean;
    }

    public static String jetGenerate(JetBean jetBean, CodeGeneratorArgument codeGenArgument) throws CodeGeneratorException {
        if (jetBean != null) {
            jetBean.setArgument(codeGenArgument);
            JetProxy proxy = new JetProxy(jetBean);
            try {
                String codePart = proxy.generate();
                String generationError = jetBean.getGenerationError();
                if (generationError != null) {
                    throw new CodeGeneratorException(generationError);
                }
                return codePart;
            } catch (JETException e) {
                log.error(e.getMessage(), e);
                CodeGeneratorArgument argument = (CodeGeneratorArgument) jetBean.getArgument();
                throw new CodeGeneratorException(e + " in " + argument.getJobName() + ' ' + jetBean.getResolvedURL(), e); //$NON-NLS-1$
            } catch (CoreException e) {
                log.error(e.getMessage(), e);
                throw new CodeGeneratorException(e);
            }
        }
        return ""; //$NON-NLS-1$
    }

    public static String jetGenerate(CodeGeneratorArgument argument, ECodePart part) throws CodeGeneratorException {
        JetBean jetBean = createJetBean(argument, part);
        return jetGenerate(jetBean, argument);
    }

    public static void checkEmittersPoolFactoryIsReady() throws CodeGeneratorException {
        long startTimer = System.currentTimeMillis();
        long endTimer = startTimer;
        try {
            while ((!CodeGeneratorEmittersPoolFactory.isInitialized()) && ((endTimer - startTimer) < INIT_TIMEOUT)) {
                Thread.sleep(INIT_PAUSE);
                endTimer = System.currentTimeMillis();
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new CodeGeneratorException(e);
        }
        if ((endTimer - startTimer) > INIT_TIMEOUT) {
            throw new CodeGeneratorException(Messages.getString("CodeGenerator.JET.TimeOut")); //$NON-NLS-1$
        }
    }
}
