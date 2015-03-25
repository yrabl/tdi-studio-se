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
package org.talend.designer.codegen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.temp.ECodePart;
import org.talend.core.model.temp.ETypeGen;
import org.talend.designer.codegen.config.BundleExtJetBean;
import org.talend.designer.codegen.config.CloseBlocksCodeArgument;
import org.talend.designer.codegen.config.CodeGeneratorArgument;
import org.talend.designer.codegen.config.EInternalTemplate;
import org.talend.designer.codegen.config.JetBean;
import org.talend.designer.codegen.config.NodesSubTree;
import org.talend.designer.codegen.config.NodesTree;
import org.talend.designer.codegen.config.SubTreeArgument;
import org.talend.designer.codegen.exception.CodeGeneratorException;
import org.talend.designer.codegen.generator.AbstractCodeGenerator;
import org.talend.designer.codegen.i18n.Messages;
import org.talend.designer.codegen.model.CodeGeneratorEmittersPoolFactory;
import org.talend.designer.codegen.model.CodeGeneratorInternalTemplatesFactoryProvider;
import org.talend.designer.core.ICamelDesignerCoreService;

/**
 * CodeGenerator.
 * 
 * $Id$
 * 
 */
public class CodeGenerator extends AbstractCodeGenerator {

    private boolean statistics;

    private boolean trace;

    private String interpreterPath;

    private String libPath;

    private String runtimeFilePath;

    private boolean checkingSyntax = false;

    private String contextName;

    private List<? extends INode> nodes;

    private NodesTree processTree;

    private String exportAsOSGI = "false";

    private static final long INIT_TIMEOUT = 15 * 60 * 1000; // 15s

    private static final long INIT_PAUSE = 1000; // 1s

    private static final boolean DEBUG = false;

    /**
     * Constructor : use the process and laguage to initialize internal components.
     * 
     * @param process
     * @param language
     */
    public CodeGenerator(IProcess process, boolean statistics, boolean trace, String... options) {
        super(process);
        this.statistics = statistics;
        this.trace = trace;

        this.checkingSyntax = false;

        if ((options != null) && (options.length == 5)) {
            this.interpreterPath = options[0];
            this.libPath = options[1];
            this.runtimeFilePath = options[2];
            this.currentProjectName = options[3];
            this.exportAsOSGI = options[4];
        } else if ((options != null) && (options.length == 4)) {
            this.interpreterPath = options[0];
            this.libPath = options[1];
            this.runtimeFilePath = options[2];
            this.currentProjectName = options[3];
        } else {
            this.interpreterPath = ""; //$NON-NLS-1$
            this.libPath = ""; //$NON-NLS-1$
            this.runtimeFilePath = ""; //$NON-NLS-1$
            this.currentProjectName = ""; //$NON-NLS-1$
        }

        if (DEBUG) {
            nodes = process.getGraphicalNodes();
            System.out.println(Messages.getString("CodeGenerator.getGraphicalNode1")); //$NON-NLS-1$
            printForDebug();
        }

        nodes = process.getGeneratingNodes();

        if (DEBUG) {
            System.out.println(Messages.getString("CodeGenerator.getGraphicalNode2")); //$NON-NLS-1$
            printForDebug();
        }
        boolean isCamel = false;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
            ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                    ICamelDesignerCoreService.class);
            if (process != null && process instanceof IProcess2) {
                IProcess2 process2 = (IProcess2) process;
                if (camelService.isInstanceofCamelRoutes(process2.getProperty().getItem())) {
                    isCamel = true;
                }
            }
        }
        if (isCamel) {
            processTree = new NodesTree(process, nodes, true, ETypeGen.CAMEL);
        } else {
            processTree = new NodesTree(process, nodes, true);
        }

    }

    public CodeGenerator() {
        //
    }

    /**
     * Return true to display size of source code in method comment.
     * 
     * @return
     */
    private boolean isMethodSizeNeeded() {
        // must match TalendDesignerPrefConstants.DISPLAY_METHOD_SIZE
        boolean displayMethodSize = Boolean.parseBoolean(CorePlugin.getDefault().getDesignerCoreService()
                .getPreferenceStore("displayMethodSize")); //$NON-NLS-1$
        return displayMethodSize;
    }

    /**
     * Generate the code for the process given to the constructor.
     * 
     * @return the generated code
     * @throws CodeGeneratorException if an error occurs during Code Generation
     */
    @Override
    @SuppressWarnings("unchecked")
    public String generateProcessCode() throws CodeGeneratorException {
        // Parse Process, generate Code for Individual Components
        // generate Assembly Code for individual Components
        StringBuffer componentsCode = new StringBuffer();

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
        } else {
            // ####0005204: Cannot Call SubJob with RunJob Component
            Vector headerArgument = new Vector(3);
            headerArgument.add(process);

            headerArgument.add(VersionUtils.getVersion());
            headerArgument.add(exportAsOSGI);
            boolean isCamel = false;
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault()
                        .getService(ICamelDesignerCoreService.class);
                if (process != null && process instanceof IProcess2) {
                    IProcess2 process2 = (IProcess2) process;
                    if (camelService.isInstanceofCamelRoutes(process2.getProperty().getItem())) {
                        isCamel = true;
                    }
                }
            }
            if (isCamel) {
                componentsCode.append(generateTypedComponentCode(EInternalTemplate.HEADER_ROUTE, headerArgument));
            } else {
                componentsCode.append(generateTypedComponentCode(EInternalTemplate.HEADER, headerArgument));
            }
            if (isCamel) {
                if ((processTree.getSubTrees() != null) && (processTree.getSubTrees().size() > 0)) {

                    // sortSubTree(processTree.getSubTrees());

                    boolean displayMethodSize = isMethodSizeNeeded();
                    NodesSubTree lastSubtree = null;
                    boolean generateHeaders = true;
                    boolean isFirstRoute = true;

                    List<NodesSubTree> nodeSubTreeList = new ArrayList<NodesSubTree>();
                    List<INode> sortedNodeList = new ArrayList<INode>();

                    for (NodesSubTree subTree : processTree.getSubTrees()) {
                        lastSubtree = subTree;

                        // Generate headers only one time, for each routes in the CamelContext.
                        if (generateHeaders) {
                            componentsCode.append(generateTypedComponentCode(EInternalTemplate.SUBPROCESS_HEADER_ROUTE, subTree));
                            componentsCode.append(generateTypedComponentCode(EInternalTemplate.CAMEL_HEADER, headerArgument));
                            generateHeaders = false;
                        }

                        // Fix bug TESB-2951 Generated Codes error when Route
                        // starts with cFile/cFTP/cActiveMQ/cFTP/cJMS
                        // LiXiaopeng 2011-09-05
                        INode subProcessStartNode = subTree.getRootNode().getSubProcessStartNode(true);
                        String startNodeName = subProcessStartNode.getComponent().getName();
                        IElementParameter family = subProcessStartNode.getElementParameter("FAMILY");
                        if (subProcessStartNode.isStart() && null != family && "Messaging".equals(family.getValue())) {
                            nodeSubTreeList.add(subTree);
                        } else if ("cConfig".equals(startNodeName)) {
                            // Customized remove the cConfig routeId codes.
                            // TESB-2825 LiXP 20110823
                            // Do nothing.
                        } else {
                            // And generate the component par of code
                            componentsCode.append(generateComponentsCode(subTree, subTree.getRootNode(), ECodePart.MAIN, null,
                                    ETypeGen.CAMEL));
                            componentsCode.append(";");
                        }
                    }

                    for (NodesSubTree subTree : nodeSubTreeList) {
                        lastSubtree = subTree;
                        componentsCode.append(generateComponentsCode(subTree, subTree.getRootNode(), ECodePart.MAIN, null,
                                ETypeGen.CAMEL)); // And generate the component par of code
                        componentsCode.append(";");
                    }
                    // Close the last route in the CamelContext
                    componentsCode.append(generateTypedComponentCode(EInternalTemplate.CAMEL_FOOTER, headerArgument));
                    componentsCode.append(generateTypedComponentCode(EInternalTemplate.SUBPROCESS_FOOTER_ROUTE, lastSubtree));
                }
            } else {
                // ####
                componentsCode.append(generateTypedComponentCode(EInternalTemplate.HEADER_ADDITIONAL, headerArgument));
                if ((processTree.getSubTrees() != null) && (processTree.getSubTrees().size() > 0)) {

                    boolean displayMethodSize = isMethodSizeNeeded();
                    for (NodesSubTree subTree : processTree.getSubTrees()) {
                        subTree.setMethodSizeNeeded(displayMethodSize);
                        if (!subTree.isMergeSubTree()) {
                            componentsCode.append(generateTypedComponentCode(EInternalTemplate.SUBPROCESS_HEADER, subTree));
                            componentsCode.append(generateComponentsCode(subTree, subTree.getRootNode(), ECodePart.BEGIN, null));
                            componentsCode.append(generateComponentsCode(subTree, subTree.getRootNode(), ECodePart.MAIN, null));
                            componentsCode.append(generateTypedComponentCode(EInternalTemplate.PART_ENDMAIN,
                                    subTree.getRootNode()));
                            componentsCode.append(generateComponentsCode(subTree, subTree.getRootNode(), ECodePart.END, null));
                            StringBuffer finallyPart = new StringBuffer();
                            finallyPart.append(generateComponentsCode(subTree, subTree.getRootNode(), ECodePart.FINALLY, null));
                            Vector subprocess_footerArgument = new Vector(2);
                            subprocess_footerArgument.add(subTree);
                            subprocess_footerArgument.add(finallyPart.toString());
                            componentsCode.append(generateTypedComponentCode(EInternalTemplate.SUBPROCESS_FOOTER,
                                    subprocess_footerArgument));
                        } else {
                            StringBuffer finallyPart = new StringBuffer();
                            componentsCode.append(generateTypedComponentCode(EInternalTemplate.SUBPROCESS_HEADER, subTree));
                            for (INode mergeNode : subTree.getMergeNodes()) {
                                componentsCode.append(generateComponentsCode(subTree, mergeNode, ECodePart.BEGIN, null));
                            }
                            List<INode> sortedMergeBranchStarts = subTree.getSortedMergeBranchStarts();
                            for (INode startNode : sortedMergeBranchStarts) {
                                componentsCode.append(generateComponentsCode(subTree, startNode, ECodePart.BEGIN, null));
                                componentsCode.append(generateComponentsCode(subTree, startNode, ECodePart.MAIN, null));

                                componentsCode.append(generateTypedComponentCode(EInternalTemplate.PART_ENDMAIN,
                                        subTree.getRootNode()));

                                componentsCode.append(generateComponentsCode(subTree, startNode, ECodePart.END, null));
                                finallyPart.append(generateComponentsCode(subTree, startNode, ECodePart.FINALLY, null));
                            }

                            for (INode mergeNode : subTree.getMergeNodes()) {
                                componentsCode.append(generateComponentsCode(subTree, mergeNode, ECodePart.END, null));
                                finallyPart.append(generateComponentsCode(subTree, mergeNode, ECodePart.FINALLY, null));
                            }
                            Vector subprocess_footerArgument = new Vector(2);
                            subprocess_footerArgument.add(subTree);
                            subprocess_footerArgument.add(finallyPart.toString());
                            componentsCode.append(generateTypedComponentCode(EInternalTemplate.SUBPROCESS_FOOTER,
                                    subprocess_footerArgument));
                        }
                    }
                }
            }
            // ####0005204: Cannot Call SubJob with RunJob Component
            Vector footerArgument = new Vector(2);
            footerArgument.add(process);
            footerArgument.add(processTree.getRootNodes());
            if (isCamel) {
                componentsCode.append(generateTypedComponentCode(EInternalTemplate.FOOTER_ROUTE, footerArgument));
            } else {
                componentsCode.append(generateTypedComponentCode(EInternalTemplate.FOOTER, footerArgument));
            }
            componentsCode.append(generateTypedComponentCode(EInternalTemplate.PROCESSINFO, componentsCode.length()));
            // ####
            return componentsCode.toString();
        }
    }

    /**
     * get the incomingName matching with inputId. Purpose: It will generate different parts for the merge node
     * according the different incomingName.
     * 
     * @param branchStartNode
     * @param mergeNode
     * @return
     */
    private String getIncomingNameForMerge(INode branchStartNode, INode mergeNode) {

        Map<INode, Integer> mergeInfo = branchStartNode.getLinkedMergeInfo();

        int inputId = ((Integer) mergeInfo.values().toArray()[0]).intValue();

        List<? extends IConnection> incomingConnections = mergeNode.getIncomingConnections();

        for (int i = 0; i < incomingConnections.size(); i++) {
            IConnection connec = incomingConnections.get(i);
            if (connec.isActivate()) {

                if (connec.getLineStyle().hasConnectionCategory(EConnectionType.MERGE)) {
                    // if find, then return
                    if (connec.getInputId() == inputId) {
                        return connec.getName();
                    }
                }
            }
        }

        return null;
    }

    /*
     * ADDED for TESB-7887 By GangLiu(non-Javadoc)
     * 
     * @see org.talend.designer.codegen.ICodeGenerator#generateSpringContent()
     */
    @Override
    public String generateSpringContent() throws CodeGeneratorException {
        if (process == null || !(process instanceof IProcess2)) {
            return null;
        }
        IProcess2 process2 = (IProcess2) process;
        if (!process2.needsSpring() || process2.getSpringContent() == null) {
            return null;
        }
        return process2.getSpringContent();
    }

    /**
     * Generate Code for a given Component.
     * 
     * @param type the internal component template
     * @param argument the bean
     * @return the generated code
     * @throws CodeGeneratorException if an error occurs during Code Generation
     */
    private StringBuffer generateTypedComponentCode(EInternalTemplate type, Object argument) throws CodeGeneratorException {
        return generateTypedComponentCode(type, argument, null);
    }

    /**
     * Generate Code Part for a given Component.
     * 
     * @param type the internal component template
     * @param argument the bean
     * @param part part of code to generate
     * @return the genrated code
     * @throws CodeGeneratorException if an error occurs during Code Generation
     */
    private StringBuffer generateTypedComponentCode(EInternalTemplate type, Object argument, ECodePart part)
            throws CodeGeneratorException {
        return generateTypedComponentCode(type, argument, part, null, null);
    }

    /**
     * Generate Code Part for a given Component.
     * 
     * @param type the internal component template
     * @param argument the bean
     * @param part part of code to generate
     * @param subProcess
     * @return the genrated code
     * @throws CodeGeneratorException if an error occurs during Code Generation
     */
    private StringBuffer generateTypedComponentCode(EInternalTemplate type, Object argument, ECodePart part, String incomingName,
            NodesSubTree subProcess) throws CodeGeneratorException {
        CodeGeneratorArgument codeGenArgument = new CodeGeneratorArgument();
        codeGenArgument.setNode(argument);
        if (subProcess != null) {
            codeGenArgument.setAllMainSubTreeConnections(subProcess.getAllMainSubTreeConnections());
            codeGenArgument.setSubTree(subProcess);
        }
        codeGenArgument.setCodePart(part);
        codeGenArgument.setStatistics(statistics);
        codeGenArgument.setTrace(trace);
        codeGenArgument.setInterpreterPath(interpreterPath);
        codeGenArgument.setLibPath(libPath);
        codeGenArgument.setRuntimeFilePath(runtimeFilePath);
        codeGenArgument.setCurrentProjectName(currentProjectName);
        codeGenArgument.setContextName(contextName);
        codeGenArgument.setJobName(jobName);

        codeGenArgument.setJobVersion(jobVersion);

        codeGenArgument.setCheckingSyntax(checkingSyntax);
        codeGenArgument.setIncomingName(incomingName);
        codeGenArgument.setIsRunInMultiThread(isRunInMultiThread());
        codeGenArgument.setPauseTime(CorePlugin.getDefault().getRunProcessService().getPauseTime());

        StringBuffer content = new StringBuffer();
        if (type == EInternalTemplate.HEADER_ADDITIONAL) {
            // loop over all HEADER_ADDITIONAL previously loaded
            List<BundleExtJetBean> bundleJetBeans = CodeGeneratorInternalTemplatesFactoryProvider.getInstance()
                    .getBundleExtJetBeans();
            for (BundleExtJetBean bean : bundleJetBeans) {
                if (bean.getName().contains(EInternalTemplate.HEADER_ADDITIONAL.getTemplateName())) {
                    try {
                        BundleExtJetBean clonedBean = bean.clone();
                        clonedBean.setArgument(codeGenArgument);
                        content.append(instantiateJetProxy(clonedBean));
                    } catch (CloneNotSupportedException e) {
                        //
                    }
                }
            }
        } else {
            // JetBean jetBean = initializeJetBean(codeGenArgument);
            // jetBean.setTemplateRelativeUri(TemplateUtil.RESOURCES_DIRECTORY + TemplateUtil.DIR_SEP + type +
            // TemplateUtil.EXT_SEP
            // + language.getExtension() + TemplateUtil.TEMPLATE_EXT);
            JetBean jetBean = getTemplateJetBean(type);
            if (jetBean != null) {
                jetBean.setArgument(codeGenArgument);
                content.append(instantiateJetProxy(jetBean));
            }
        }
        return content;
    }

    /**
     * Generate Code Parts for a Sub Process .
     * 
     * @param subProcess the suprocess
     * @param node the subprocess root
     * @param part the part of code to generate
     * @return the generated code
     * @throws CodeGeneratorException if an error occurs during Code Generation
     */

    private StringBuffer generateComponentsCode(NodesSubTree subProcess, INode node, ECodePart part, String incomingName)
            throws CodeGeneratorException {
        return generateComponentsCode(subProcess, node, part, incomingName, ETypeGen.ETL);
    }

    /**
     * Generate Code Parts for a Sub Process .
     * 
     * @param subProcess the suprocess
     * @param node the subprocess root
     * @param part the part of code to generate
     * @return the generated code
     * @throws CodeGeneratorException if an error occurs during Code Generation
     */

    private StringBuffer generateComponentsCode(NodesSubTree subProcess, INode node, ECodePart part, String incomingName,
            ETypeGen typeGen) throws CodeGeneratorException {
        StringBuffer codeComponent = new StringBuffer();
        Boolean isMarked = subProcess.isMarkedNode(node, part);
        boolean isIterate = isSpecifyInputNode(node, incomingName, EConnectionType.ITERATE);
        boolean isOnRowsEnd = isSpecifyInputNode(node, incomingName, EConnectionType.ON_ROWS_END);
        if ((isMarked != null) && (!isMarked)) {
            switch (part) {
            case BEGIN:
                // if (isIterate) {
                // codeComponent.append(generateComponentCode(node,
                // ECodePart.BEGIN, incomingName));
                // }
                codeComponent.append(generatesTreeCode(subProcess, node, ECodePart.BEGIN, typeGen));
                // if (!isIterate) {
                codeComponent.append(generateComponentCode(subProcess, node, ECodePart.BEGIN, incomingName, typeGen));
                // }
                break;
            case MAIN:
                if (isIterate) {
                    codeComponent.append(generateTypedComponentCode(EInternalTemplate.ITERATE_SUBPROCESS_HEADER, node,
                            ECodePart.BEGIN, incomingName, subProcess));
                    codeComponent.append(generatesTreeCode(subProcess, node, ECodePart.BEGIN, typeGen));
                    codeComponent.append(generateComponentCode(subProcess, node, ECodePart.BEGIN, incomingName, typeGen));

                    codeComponent.append(generateComponentCode(subProcess, node, ECodePart.MAIN, incomingName, typeGen));
                    codeComponent.append(generatesTreeCode(subProcess, node, ECodePart.MAIN, typeGen));

                    codeComponent.append(generateComponentCode(subProcess, node, ECodePart.END, incomingName, typeGen));
                    codeComponent.append(generatesTreeCode(subProcess, node, ECodePart.END, typeGen));

                    StringBuffer finallyPart = new StringBuffer();
                    // if iterate with parallel
                    finallyPart.append(generateComponentsCode(subProcess, node, ECodePart.FINALLY, incomingName, typeGen));
                    Vector iterate_Argument = new Vector(2);
                    iterate_Argument.add(node);
                    iterate_Argument.add(finallyPart.toString());

                    codeComponent.append(generateTypedComponentCode(EInternalTemplate.ITERATE_SUBPROCESS_FOOTER,
                            iterate_Argument, ECodePart.END, incomingName, subProcess));
                } else {
                    if (ETypeGen.CAMEL == typeGen) {
                        if (node.getIncomingConnections() != null && node.getIncomingConnections().size() > 0) {
                            if (!(node.getIncomingConnections().get(0).getLineStyle().equals(EConnectionType.ROUTE))) {
                                codeComponent.append(generateTypedComponentCode(EInternalTemplate.CAMEL_SPECIALLINKS, node));
                            }
                        }
                    }
                    codeComponent.append(generateComponentCode(subProcess, node, ECodePart.MAIN, incomingName, typeGen));
                    if (ETypeGen.CAMEL == typeGen) {
                        if (node.getIncomingConnections().size() < 1 && node.isStart()) {
                            // http://jira.talendforge.org/browse/TESB-4086 XiaopengLi
                            String label = null;
                            IElementParameter parameter = node.getElementParameter("LABEL");
                            if (parameter != null && !"__UNIQUE_NAME__".equals(parameter.getValue())) {
                                label = (String) parameter.getValue();
                            }

                            /*
                             * Fix https://jira.talendforge.org/browse/TESB-6685 label + uniqueName to make it unique
                             */
                            if (label == null) {
                                label = node.getUniqueName();
                            } else {
                                label += "_" + node.getUniqueName();
                            }
                            if (!"cErrorHandler".equals(node.getComponent().getName())) {
                                codeComponent.append(".routeId(\"" + label + "\")");
                            }
                        } else {
                            codeComponent.append(".id(\"" + node.getUniqueName() + "\")");
                        }
                    }
                    codeComponent.append(generatesTreeCode(subProcess, node, ECodePart.MAIN, typeGen));
                }
                break;
            case END:
                if (isOnRowsEnd) {

                    codeComponent.append(generatesTreeCode(subProcess, node, ECodePart.BEGIN, typeGen));
                    codeComponent.append(generateComponentCode(subProcess, node, ECodePart.BEGIN, incomingName, typeGen));

                    codeComponent.append(generateComponentCode(subProcess, node, ECodePart.MAIN, incomingName, typeGen));
                    codeComponent.append(generatesTreeCode(subProcess, node, ECodePart.MAIN, typeGen));

                    codeComponent.append(generateComponentCode(subProcess, node, ECodePart.END, incomingName, typeGen));
                    codeComponent.append(generatesTreeCode(subProcess, node, ECodePart.END, typeGen));

                } else {
                    // if (!isIterate) {
                    codeComponent.append(generateComponentCode(subProcess, node, ECodePart.END, incomingName, typeGen));
                    // }
                    codeComponent.append(generatesTreeCode(subProcess, node, part, typeGen));
                    // if (isIterate) {
                    // codeComponent.append(generateComponentCode(node,
                    // ECodePart.END, incomingName));
                    // }
                }
                break;
            case FINALLY:
                codeComponent.append(generateComponentCode(subProcess, node, ECodePart.FINALLY, incomingName, typeGen));
                codeComponent.append(generatesTreeCode(subProcess, node, ECodePart.FINALLY, typeGen));
                break;
            default:
                // do nothing
            }
            subProcess.markNode(node, part);
        }

        return codeComponent;
    }

    /**
     * Return Type of Node to correctly sort the encapsulated code.
     * 
     * @param node the node to check
     * @return true if the node is an iterate node
     */
    private boolean isSpecifyInputNode(INode node, String incomingName, EConnectionType connectionType) {
        // it means the first node without any income connection
        if (incomingName == null) {
            return false;
        }
        boolean result = false;
        if (node != null) {
            List<? extends IConnection> inComingIterateConnection = node.getIncomingConnections(connectionType);
            if ((inComingIterateConnection != null) && (inComingIterateConnection.size() > 0)) {
                for (IConnection connection : inComingIterateConnection) {
                    if (connection.getName().equals(incomingName)) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Generate this tree Code.
     * 
     * @param subProcess the tree
     * @param node the tree root
     * @param part the part of code to generate
     * @return the generated code
     * @throws CodeGeneratorException if an error occurs during Code Generation
     */
    private StringBuffer generatesTreeCode(NodesSubTree subProcess, INode node, ECodePart part, ETypeGen typeGen)
            throws CodeGeneratorException {
        StringBuffer code = new StringBuffer();
        if (node != null) {
            SubTreeArgument subTreeArgument = new SubTreeArgument();

            // Conditional Outputs
            boolean sourceHasConditionnalBranches = node.hasConditionalOutputs() && part == ECodePart.MAIN;
            subTreeArgument.setSourceComponentHasConditionnalOutputs(sourceHasConditionnalBranches);

            // Multiplying Output Rows
            if (part == ECodePart.MAIN) {
                subTreeArgument.setMultiplyingOutputComponents(node.isMultiplyingOutputs());
            }

            if (ETypeGen.ETL == typeGen) {
                for (IConnection connection : node.getOutgoingConnections()) {

                    if ((connection.getLineStyle() == EConnectionType.ITERATE) && (part != ECodePart.MAIN)
                            && (part != ECodePart.FINALLY)) {
                        continue;
                    }

                    if ((connection.getLineStyle() == EConnectionType.ITERATE)
                            && ("true".equals(ElementParameterParser.getValue(connection, "__ENABLE_PARALLEL__"))) //$NON-NLS-1$//$NON-NLS-2$
                            && part == ECodePart.FINALLY) {
                        continue;
                    }

                    if ((connection.getLineStyle() == EConnectionType.ON_ROWS_END) && (part != ECodePart.END)
                            && (part != ECodePart.FINALLY)) {
                        continue;
                    }

                    if (connection.getLineStyle().hasConnectionCategory(EConnectionType.DEPENDENCY)) {
                        continue;
                    }

                    if (connection.getLineStyle().hasConnectionCategory(EConnectionType.USE_HASH)) {
                        continue;
                    }

                    INode targetNode = connection.getTarget();
                    if ((targetNode != null) && (subProcess != null)) {

                        if (!connection.getLineStyle().hasConnectionCategory(IConnectionCategory.MERGE)) {
                            subTreeArgument.setInputSubtreeConnection(connection);
                            code.append(generateTypedComponentCode(EInternalTemplate.SUBTREE_BEGIN, subTreeArgument));
                            code.append(generateComponentsCode(subProcess, targetNode, part, connection.getName(), typeGen));
                            code.append(generateTypedComponentCode(EInternalTemplate.SUBTREE_END, subTreeArgument));
                        } else if (part == ECodePart.MAIN) {
                            subTreeArgument.setInputSubtreeConnection(connection);
                            code.append(generateTypedComponentCode(EInternalTemplate.SUBTREE_BEGIN, subTreeArgument));
                            code.append(generateComponentsCode(subProcess, targetNode, ECodePart.MAIN,
                                    getIncomingNameForMerge(node, targetNode), typeGen));
                            code.append(generateTypedComponentCode(EInternalTemplate.SUBTREE_END, subTreeArgument));
                        }

                    }
                }
            } else if (ETypeGen.CAMEL == typeGen) {
                for (IConnection connection : node.getOutgoingCamelSortedConnections()) {
                    INode targetNode = connection.getTarget();
                    if ((targetNode != null) && (subProcess != null)) {
                        subTreeArgument.setInputSubtreeConnection(connection);
                        code.append(CamelConnectionTagGenerator.getInstance().generateStartTag(node, connection));
                        code.append(generateComponentsCode(subProcess, targetNode, part, connection.getName(), typeGen));
                        code.append(CamelConnectionTagGenerator.getInstance().generateEndTag(node, connection));
                    }
                }
            }

            if (part == ECodePart.MAIN && node.getBlocksCodeToClose() != null) {
                CloseBlocksCodeArgument closeBlocksArgument = new CloseBlocksCodeArgument();
                closeBlocksArgument.setBlocksCodeToClose(node.getBlocksCodeToClose());
                code.append(generateTypedComponentCode(EInternalTemplate.CLOSE_BLOCKS_CODE, closeBlocksArgument));
            }
        }
        return code;
    }

    /**
     * Generate Part Code for a given Component.
     * 
     * @param node the component
     * @param part the component's part
     * @return the generated code
     * @throws CodeGeneratorException if an error occurs during Code Generation
     */
    public String generateComponentCode(NodesSubTree subProcess, INode node, ECodePart part, String incomingName, ETypeGen typeGen)
            throws CodeGeneratorException {
        CodeGeneratorArgument argument = new CodeGeneratorArgument();
        argument.setNode(node);
        argument.setAllMainSubTreeConnections(subProcess.getAllMainSubTreeConnections());
        argument.setCodePart(part);
        argument.setStatistics(statistics);
        argument.setTrace(trace);
        argument.setInterpreterPath(interpreterPath);
        argument.setLibPath(libPath);
        argument.setRuntimeFilePath(runtimeFilePath);
        argument.setCurrentProjectName(currentProjectName);
        argument.setContextName(contextName);
        argument.setJobName(jobName);
        argument.setJobVersion(jobVersion);

        argument.setCheckingSyntax(checkingSyntax);
        argument.setIncomingName(incomingName);
        argument.setIsRunInMultiThread(isRunInMultiThread());
        argument.setPauseTime(CorePlugin.getDefault().getRunProcessService().getPauseTime());

        StringBuffer content = new StringBuffer();

        if (typeGen == ETypeGen.ETL) {
            content.append(generateTypedComponentCode(EInternalTemplate.PART_HEADER, node, part, incomingName, subProcess));
        }

        content.append(instantiateJetProxy(getNodeJetBean(argument, part)));

        if (typeGen == ETypeGen.ETL) {
            content.append(generateTypedComponentCode(EInternalTemplate.PART_FOOTER, node, part, incomingName, subProcess));
        }

        return content.toString();
    }

    /**
     * Generate Part Code for a given Component.
     * 
     * @param node the component
     * @param part the component's part
     * @return the generated code
     * @throws CodeGeneratorException if an error occurs during Code Generation
     */
    @Override
    public String generateComponentCode(INode node, ECodePart part) throws CodeGeneratorException {
        CodeGeneratorArgument argument = new CodeGeneratorArgument();
        argument.setNode(node);
        argument.setCodePart(part);
        argument.setStatistics(statistics);
        argument.setTrace(trace);
        argument.setInterpreterPath(interpreterPath);
        argument.setLibPath(libPath);
        argument.setRuntimeFilePath(runtimeFilePath);
        argument.setCurrentProjectName(currentProjectName);
        argument.setContextName(contextName);
        argument.setJobName(jobName);

        argument.setJobVersion(jobVersion);

        argument.setCheckingSyntax(checkingSyntax);
        argument.setIsRunInMultiThread(isRunInMultiThread());
        argument.setPauseTime(CorePlugin.getDefault().getRunProcessService().getPauseTime());

        StringBuffer content = new StringBuffer();

        content.append(generateTypedComponentCode(EInternalTemplate.PART_HEADER, node, part));

        content.append(instantiateJetProxy(getNodeJetBean(argument, part)));

        content.append(generateTypedComponentCode(EInternalTemplate.PART_FOOTER, node, part));

        return content.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.codegen.ICodeGenerator#generateComponentCodeWithRows (java.lang.String,
     * java.lang.Object)
     */
    @Override
    public String generateComponentCodeWithRows(String nodeName, IAloneProcessNodeConfigurer nodeConfigurer) {
        StringBuffer componentsCode = new StringBuffer();

        if (process == null) {
            throw new NullPointerException();
        } else {
            INode nodeToConfigure = extractNodeFromProcess(nodeName);
            if (nodeToConfigure != null) {

                if (nodeConfigurer != null) {
                    nodeConfigurer.configure(nodeToConfigure);
                }

                Vector headerArgument = new Vector(2);
                headerArgument.add(process);
                headerArgument.add(VersionUtils.getVersion());
                headerArgument.add("false"); // not osgi export

                this.checkingSyntax = true;

                try {
                    componentsCode.append(generateTypedComponentCode(EInternalTemplate.HEADER, headerArgument));
                    componentsCode.append(generateTypedComponentCode(EInternalTemplate.HEADER_ADDITIONAL, headerArgument));
                    for (NodesSubTree subTree : processTree.getSubTrees()) {
                        INode subTreeNode = subTree.getNode(nodeName);

                        // if (subTreeNode != null && nodeConfigurer != null) {
                        // nodeConfigurer.configure(subTreeNode);
                        // }
                        componentsCode.append(generateTypedComponentCode(EInternalTemplate.SUBPROCESS_HEADER, subTree));
                        StringBuffer finallyPart = new StringBuffer();
                        if (subTreeNode != null) {

                            if (!subTree.isMergeSubTree()) {
                                // componentsCode.append(
                                // generateTypedComponentCode
                                // (EInternalTemplate.SUBPROCESS_HEADER,
                                // subTree));
                                componentsCode.append(generateComponentsCode(subTree, subTree.getRootNode(), ECodePart.BEGIN,
                                        null));
                                componentsCode
                                        .append(generateComponentsCode(subTree, subTree.getRootNode(), ECodePart.MAIN, null));
                                componentsCode.append(generateTypedComponentCode(EInternalTemplate.PART_ENDMAIN,
                                        subTree.getRootNode()));
                                componentsCode
                                        .append(generateComponentsCode(subTree, subTree.getRootNode(), ECodePart.END, null));
                                // componentsCode.append(
                                // generateTypedComponentCode
                                // (EInternalTemplate.SUBPROCESS_FOOTER,
                                // subTree));
                                finallyPart
                                        .append(generateComponentsCode(subTree, subTree.getRootNode(), ECodePart.FINALLY, null));
                            } else {
                                // componentsCode.append(
                                // generateTypedComponentCode
                                // (EInternalTemplate.SUBPROCESS_HEADER,
                                // subTree));
                                for (INode mergeNode : subTree.getMergeNodes()) {
                                    componentsCode.append(generateComponentsCode(subTree, mergeNode, ECodePart.BEGIN, null));
                                }
                                List<INode> sortedMergeBranchStarts = subTree.getSortedMergeBranchStarts();
                                for (INode startNode : sortedMergeBranchStarts) {
                                    componentsCode.append(generateComponentsCode(subTree, startNode, ECodePart.BEGIN, null));
                                    componentsCode.append(generateComponentsCode(subTree, startNode, ECodePart.MAIN, null));

                                    componentsCode.append(generateComponentsCode(subTree, startNode, ECodePart.END, null));

                                    finallyPart.append(generateComponentsCode(subTree, startNode, ECodePart.FINALLY, null));
                                }

                                componentsCode.append(generateTypedComponentCode(EInternalTemplate.PART_ENDMAIN,
                                        subTree.getRootNode()));

                                for (INode mergeNode : subTree.getMergeNodes()) {
                                    componentsCode.append(generateComponentsCode(subTree, mergeNode, ECodePart.END, null));
                                    finallyPart.append(generateComponentsCode(subTree, mergeNode, ECodePart.FINALLY, null));
                                }

                                // componentsCode.append(
                                // generateTypedComponentCode
                                // (EInternalTemplate.SUBPROCESS_FOOTER,
                                // subTree));
                            }
                        }
                        Vector subprocess_footerArgument = new Vector(2);
                        subprocess_footerArgument.add(subTree);
                        subprocess_footerArgument.add(finallyPart.toString());
                        componentsCode.append(generateTypedComponentCode(EInternalTemplate.SUBPROCESS_FOOTER,
                                subprocess_footerArgument));
                    }
                    Vector footerArgument = new Vector(2);
                    footerArgument.add(process);
                    footerArgument.add(processTree.getRootNodes());
                    componentsCode.append(generateTypedComponentCode(EInternalTemplate.FOOTER, footerArgument));
                    componentsCode.append(generateTypedComponentCode(EInternalTemplate.PROCESSINFO, componentsCode.length()));
                } catch (CodeGeneratorException ce) {
                    // ce.printStackTrace();
                    ExceptionHandler.process(ce);
                    componentsCode = new StringBuffer();
                }
            } else {
                throw new TypeNotPresentException(Messages.getString("CodeGenerator.Node.NotFound"), null); //$NON-NLS-1$
            }
        }

        return componentsCode.toString();
    }

    /**
     * DOC mhirt Comment method "extractNodeFromProcess".
     * 
     * @param nodeName
     * @return
     */
    private INode extractNodeFromProcess(String nodeName) {
        List<? extends INode> allProcessNodes = process.getGeneratingNodes();
        for (INode node : allProcessNodes) {
            if (node.getUniqueName().compareTo(nodeName) == 0) {
                return node;
            }
        }
        return null;
    }

    @Override
    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    /**
     * DOC xtan for debug
     * <p>
     * output the nodes info to console to check the intial input info from design level.
     * </p>
     */
    public void printForDebug() {
        // get unique name
        List<String> nameList = new ArrayList<String>(nodes.size());
        for (INode node : nodes) {
            nameList.add(node.getUniqueName());
        }

        // sort in nameList, in order to keep the intial node inder in nodes.
        Collections.sort(nameList);

        for (String string : nameList) {
            for (INode node : nodes) {
                if (string.equals(node.getUniqueName())) {
                    // output the node info
                    System.out.println(node);
                    break;
                }
            }
        }

        System.out.println(Messages.getString("CodeGenerator.newLine")); //$NON-NLS-1$
    }

}
