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
package org.talend.component.core.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.talend.commons.exception.BusinessException;
import org.talend.component.core.constants.IComponentConstants;
import org.talend.component.core.i18n.Messages;
import org.talend.component.core.utils.ComponentsUtils;
import org.talend.components.api.NamedThing;
import org.talend.components.api.component.ComponentConnector;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentProperties.Deserialized;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.service.ComponentService;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.param.ERepositoryCategoryType;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IElementParameterDefaultValue;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.temp.ECodePart;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.ui.services.IComponentsLocalProviderService;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.AbstractComponent;
import org.talend.designer.core.model.components.DummyComponent;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.MultiDefaultValuesUtils;
import org.talend.designer.core.model.components.NodeConnector;
import org.talend.designer.core.model.components.NodeReturn;
import org.talend.designer.core.model.utils.emf.component.ITEMSType;
import org.talend.designer.core.model.utils.emf.component.ITEMType;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;

/**
 * created by hcyi on Sep 10, 2015 Detailled comment
 *
 */
public class Component extends AbstractComponent {

    private static Logger log = Logger.getLogger(Component.class);

    private static final long serialVersionUID = 1L;

    private ComponentDefinition componentDefinition;

    private List<ModuleNeeded> componentImportNeedsList;

    private List<IMultipleComponentManager> multipleComponentManagers;

    private ComponentsProvider provider;

    private Map<String, String> translatedMap = new HashMap<String, String>();

    public Component(ComponentDefinition componentDefinition) throws BusinessException {
        this.componentDefinition = componentDefinition;
        // TODO
        this.setPaletteType("DI"); //$NON-NLS-1$
    }

    public ComponentDefinition getComponentDefinition() {
        return componentDefinition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getName()
     */
    @Override
    public String getName() {
        return componentDefinition.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getLongName()
     */
    @Override
    public String getLongName() {
        return componentDefinition.getTitle();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getOriginalFamilyName()
     */
    @Override
    public String getOriginalFamilyName() {
        String[] families = componentDefinition.getFamilies();
        StringBuffer sb = new StringBuffer();
        for (String familyName : families) {
            if (sb.length() > 0) {
                sb.append("|");//$NON-NLS-1$
            }
            sb.append(familyName);
        }
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getTranslatedFamilyName()
     */
    @Override
    public String getTranslatedFamilyName() {
        String[] families = componentDefinition.getFamilies();
        StringBuffer sb = new StringBuffer();
        for (String familyName : families) {
            if (sb.length() > 0) {
                sb.append("|");//$NON-NLS-1$
            }
            sb.append(familyName);
        }
        return sb.toString();
    }

    private String getTranslatedValue(final String nameValue) {
        String returnValue = nameValue;
        if (translatedMap.containsKey(nameValue)) {
            return translatedMap.get(nameValue);
        }
        return returnValue;
    }

    @Override
    public List<ElementParameter> createElementParameters(INode node) {
        List<ElementParameter> listParam;
        listParam = new ArrayList<ElementParameter>();
        addMainParameters(listParam, node);
        addPropertyParameters(listParam, node, NORMAL_PROPERTY);
        addPropertyParameters(listParam, node, ADVANCED_PROPERTY);
        initializePropertyParameters(listParam, node);
        checkSchemaParameter(listParam, node);
        addViewParameters(listParam, node);
        addDocParameters(listParam, node);
        addValidationRulesParameters(listParam, node);
        return listParam;
    }

    private void checkSchemaParameter(List<ElementParameter> listParam, INode node) {
        boolean acceptInputFlow = false;
        List<NodeConnector> connectors = createConnectors(node);
        for (NodeConnector connector : connectors) {
            if (connector.getName().equals(EConnectionType.FLOW_MAIN.getName())) {
                if (connector.getMaxLinkInput() != 0 && !connector.isBuiltIn()) {
                    acceptInputFlow = true;
                }
                break;
            }
        }

        boolean hasSchemaType = false;
        for (ElementParameter param : listParam) {
            if (param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                    || param.getFieldType().equals(EParameterFieldType.DCSCHEMA)) {
                hasSchemaType = true;
                break;
            }
        }

        if (acceptInputFlow && !hasSchemaType) {
            // increment the row number for each parameter
            for (ElementParameter param : listParam) {
                if (EComponentCategory.BASIC.equals(param.getCategory())) {
                    param.setNumRow(param.getNumRow() + 1);
                }
            }

            ElementParameter parentParam = new ElementParameter(node);
            parentParam.setName(EParameterName.NOT_SYNCHRONIZED_SCHEMA.getName());
            parentParam.setDisplayName(EParameterName.SCHEMA_TYPE.getDisplayName());
            parentParam.setFieldType(EParameterFieldType.SCHEMA_TYPE);
            parentParam.setCategory(EComponentCategory.BASIC);
            parentParam.setNumRow(1);
            parentParam.setReadOnly(false);
            parentParam.setShow(false);
            parentParam.setContext(IComponentConstants.CONTEXT);
            listParam.add(parentParam);

            ElementParameter newParam = new ElementParameter(node);
            newParam.setCategory(EComponentCategory.BASIC);
            newParam.setName(EParameterName.SCHEMA_TYPE.getName());
            newParam.setDisplayName(EParameterName.SCHEMA_TYPE.getDisplayName());
            newParam.setListItemsDisplayName(new String[] { TEXT_BUILTIN, TEXT_REPOSITORY, TNS_FILE });
            newParam.setListItemsDisplayCodeName(new String[] { BUILTIN, REPOSITORY, TNS_FILE });
            newParam.setListItemsValue(new String[] { BUILTIN, REPOSITORY, TNS_FILE });
            newParam.setValue(BUILTIN);
            newParam.setNumRow(1);
            newParam.setFieldType(EParameterFieldType.TECHNICAL);
            newParam.setShow(true);
            newParam.setReadOnly(true);

            newParam.setContext(IComponentConstants.CONTEXT);
            newParam.setParentParameter(parentParam);

            newParam = new ElementParameter(node);
            newParam.setCategory(EComponentCategory.BASIC);
            newParam.setName(EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
            newParam.setDisplayName(EParameterName.REPOSITORY_SCHEMA_TYPE.getDisplayName());
            newParam.setListItemsDisplayName(new String[] {});
            newParam.setListItemsValue(new String[] {});
            newParam.setNumRow(1);
            newParam.setFieldType(EParameterFieldType.TECHNICAL);
            newParam.setValue(""); //$NON-NLS-1$
            newParam.setShow(false);
            newParam.setRequired(true);
            newParam.setContext(IComponentConstants.CONTEXT);
            newParam.setParentParameter(parentParam);
        }

    }

    @Override
    public List<NodeReturn> createReturns() {
        List<NodeReturn> listReturn = new ArrayList<NodeReturn>();
        // ****************** add standard returns ******************
        NodeReturn nodeRet = new NodeReturn();
        nodeRet.setAvailability("AFTER"); //$NON-NLS-1$
        nodeRet.setType(STRING_TYPE);
        nodeRet.setVarName("ERROR_MESSAGE"); //$NON-NLS-1$
        nodeRet.setDisplayName("Error Message"); //$NON-NLS-1$
        nodeRet.setName("ERROR_MESSAGE"); //$NON-NLS-1$
        listReturn.add(nodeRet);
        return listReturn;
    }

    private void addDocParameters(final List<ElementParameter> listParam, INode node) {
        ElementParameter param;

        param = new ElementParameter(node);
        param.setName(EParameterName.INFORMATION.getName());
        param.setValue(new Boolean(false));
        param.setDisplayName(EParameterName.INFORMATION.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.DOC);
        param.setNumRow(1);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShow(true);
        param.setDefaultValue(param.getValue());
        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.COMMENT.getName());
        param.setValue(""); //$NON-NLS-1$
        param.setDisplayName(EParameterName.COMMENT.getDisplayName());
        param.setFieldType(EParameterFieldType.MEMO);
        param.setNbLines(10);
        param.setCategory(EComponentCategory.DOC);
        param.setNumRow(2);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShow(true);
        param.setDefaultValue(param.getValue());
        listParam.add(param);
    }

    private void addValidationRulesParameters(final List<ElementParameter> listParam, INode node) {
        ElementParameter param;

        param = new ElementParameter(node);
        param.setName(EParameterName.VALIDATION_RULES.getName());
        param.setValue(new Boolean(false));
        param.setDisplayName(EParameterName.VALIDATION_RULES.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.VALIDATION_RULES);
        param.setNumRow(1);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setDefaultValue(param.getValue());
        param.setShow(true);
        listParam.add(param);
        listParam.add(addValidationRuleType(node, 3));
    }

    private ElementParameter addValidationRuleType(INode node, int rowNb) {
        ElementParameter parentParam = new ElementParameter(node);
        parentParam.setName(EParameterName.VALIDATION_RULE_TYPE.getName());
        parentParam.setDisplayName(EParameterName.VALIDATION_RULE_TYPE.getDisplayName());
        parentParam.setFieldType(EParameterFieldType.VALIDATION_RULE_TYPE);
        parentParam.setCategory(EComponentCategory.VALIDATION_RULES);
        parentParam.setNumRow(rowNb);
        parentParam.setReadOnly(false);
        parentParam.setShow(true);
        parentParam.setShowIf(EParameterName.VALIDATION_RULES.getName() + " == 'true'"); //$NON-NLS-1$
        parentParam.setContext(IComponentConstants.CONTEXT);
        parentParam.setRepositoryValue(ERepositoryCategoryType.VALIDATIONRULES.getName());
        parentParam.setValue("");

        ElementParameter newParam = new ElementParameter(node);
        newParam.setCategory(EComponentCategory.VALIDATION_RULES);
        newParam.setName(EParameterName.VALIDATION_RULE_TYPE.getName());
        newParam.setDisplayName(EParameterName.VALIDATION_RULE_TYPE.getDisplayName());
        newParam.setListItemsDisplayName(new String[] { TEXT_BUILTIN, TEXT_REPOSITORY });
        newParam.setListItemsDisplayCodeName(new String[] { BUILTIN, REPOSITORY });
        newParam.setListItemsValue(new String[] { BUILTIN, REPOSITORY });
        newParam.setValue(BUILTIN);
        newParam.setNumRow(rowNb);
        newParam.setFieldType(EParameterFieldType.TECHNICAL);
        newParam.setShow(true);
        newParam.setShowIf(parentParam.getName() + " =='" + REPOSITORY + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        newParam.setReadOnly(false);
        newParam.setContext(IComponentConstants.CONTEXT);
        newParam.setParentParameter(parentParam);

        newParam = new ElementParameter(node);
        newParam.setCategory(EComponentCategory.VALIDATION_RULES);
        newParam.setName(EParameterName.REPOSITORY_VALIDATION_RULE_TYPE.getName());
        newParam.setDisplayName(EParameterName.REPOSITORY_VALIDATION_RULE_TYPE.getDisplayName());
        newParam.setListItemsDisplayName(new String[] {});
        newParam.setListItemsValue(new String[] {});
        newParam.setNumRow(rowNb);
        newParam.setFieldType(EParameterFieldType.TECHNICAL);
        newParam.setValue(""); //$NON-NLS-1$
        newParam.setShow(false);
        newParam.setRequired(true);
        newParam.setContext(IComponentConstants.CONTEXT);
        newParam.setParentParameter(parentParam);
        return parentParam;
    }

    private String getNodeFormatIdWithoutFormatType(String nodeLabel, String nodeFamily) {
        String ids = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IComponentsLocalProviderService.class)) {
            IComponentsLocalProviderService service = (IComponentsLocalProviderService) GlobalServiceRegister.getDefault()
                    .getService(IComponentsLocalProviderService.class);
            if (service != null) {
                ids = service.getPreferenceStore().getString(IComponentsLocalProviderService.FORMAT_IDS);
            }
        }
        String[] idArray = null;
        if (!StringUtils.isBlank(ids)) {
            idArray = ids.split(IComponentsLocalProviderService.IDS_SEPARATOR);

            String label = nodeLabel + IComponentsLocalProviderService.PALETTE_ENTRY_TYPE;
            for (String id : idArray) {
                if (id.contains(label)) {
                    return nodeLabel + IComponentsLocalProviderService.PALETTE_ENTRY_TYPE;
                }
            }

            label = nodeFamily + IComponentsLocalProviderService.PALETTE_CONTAINER_TYPE;
            for (String id : idArray) {
                if (id.contains(label)) {
                    return nodeFamily + IComponentsLocalProviderService.PALETTE_CONTAINER_TYPE;
                }
            }

            label = IComponentsLocalProviderService.PREFERENCE_TYPE_LABEL;
            for (String id : idArray) {
                if (id.contains(label)) {
                    return IComponentsLocalProviderService.PREFERENCE_TYPE_LABEL;
                }
            }

            label = IComponentsLocalProviderService.PREFERENCE_TYPE_HINT;
            for (String id : idArray) {
                if (id.contains(label)) {
                    return IComponentsLocalProviderService.PREFERENCE_TYPE_HINT;
                }
            }

            label = IComponentsLocalProviderService.PREFERENCE_TYPE_CONNECTION;
            for (String id : idArray) {
                if (id.contains(label)) {
                    return IComponentsLocalProviderService.PREFERENCE_TYPE_CONNECTION;
                }
            }

            if (nodeFamily.contains("/")) { //$NON-NLS-1$
                String rootFamily = nodeFamily.split("/")[0]; //$NON-NLS-1$
                label = rootFamily + IComponentsLocalProviderService.PALETTE_CONTAINER_TYPE;
                for (String id : idArray) {
                    if (id.contains(label)) {
                        return rootFamily + IComponentsLocalProviderService.PALETTE_CONTAINER_TYPE;
                    }
                }
            }
        }
        return null;
    }

    public void addViewParameters(final List<ElementParameter> listParam, INode node) {
        ElementParameter param;
        IPreferenceStore localComponentProviderStore = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IComponentsLocalProviderService.class)) {
            IComponentsLocalProviderService service = (IComponentsLocalProviderService) GlobalServiceRegister.getDefault()
                    .getService(IComponentsLocalProviderService.class);
            if (service != null) {
                localComponentProviderStore = service.getPreferenceStore();
            }
        }
        String formatId = getNodeFormatIdWithoutFormatType(getName(), getTranslatedFamilyName());
        param = new ElementParameter(node);
        param.setName(EParameterName.LABEL.getName());
        param.setDisplayName(EParameterName.LABEL.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.VIEW);
        param.setNumRow(1);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShow(true);
        if (formatId != null) {
            if (localComponentProviderStore != null) {
                String label = localComponentProviderStore.getString(IComponentsLocalProviderService.PREFERENCE_TYPE_LABEL);
                if (!"".equals(label)) { //$NON-NLS-1$
                    param.setValue(label);
                }
            }
        } else {
            // in case label/format is not set in the preferences.
            String label = DesignerPlugin.getDefault().getPreferenceStore().getString(TalendDesignerPrefConstants.DEFAULT_LABEL);
            if (!"".equals(label)) { //$NON-NLS-1$
                param.setValue(label);
            }
        }
        param.setDefaultValue(param.getValue());

        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.HINT.getName());
        param.setDisplayName(EParameterName.HINT.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.VIEW);
        param.setNumRow(2);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShow(true);
        if (formatId != null) {
            if (localComponentProviderStore != null) {
                String label = localComponentProviderStore.getString(IComponentsLocalProviderService.PREFERENCE_TYPE_HINT);
                if (!"".equals(label)) { //$NON-NLS-1$
                    param.setValue(label);
                }
            }
        } else {
            // in case hint/format is not set in the preferences.
            String label = DesignerPlugin.getDefault().getPreferenceStore().getString(TalendDesignerPrefConstants.DEFAULT_HINT);
            if (!"".equals(label)) { //$NON-NLS-1$
                param.setValue(label);
            }
        }
        param.setDefaultValue(param.getValue());

        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.CONNECTION_FORMAT.getName());
        param.setDisplayName(EParameterName.CONNECTION_FORMAT.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.VIEW);
        param.setNumRow(3);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShow(true);
        if (formatId != null) {
            if (localComponentProviderStore != null) {
                String label = localComponentProviderStore.getString(IComponentsLocalProviderService.PREFERENCE_TYPE_CONNECTION);
                if (!"".equals(label)) { //$NON-NLS-1$
                    param.setValue(label);
                }
            }
        }
        param.setDefaultValue(param.getValue());
        listParam.add(param);
    }

    public void addMainParameters(final List<ElementParameter> listParam, INode node) {
        ElementParameter param;
        param = new ElementParameter(node);
        param.setName(EParameterName.UNIQUE_NAME.getName());
        param.setValue(""); //$NON-NLS-1$
        param.setDisplayName(EParameterName.UNIQUE_NAME.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.ADVANCED);
        param.setNumRow(1);
        param.setReadOnly(true);
        param.setShow(false);
        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.COMPONENT_NAME.getName());
        param.setValue(getName());
        param.setDisplayName(EParameterName.COMPONENT_NAME.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setNumRow(1);
        param.setReadOnly(true);
        param.setShow(false);
        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.VERSION.getName());
        param.setValue(""); //$NON-NLS-1$ //TODO
        param.setDisplayName(EParameterName.VERSION.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setNumRow(2);
        param.setReadOnly(true);
        param.setRequired(false);
        param.setShow(false);
        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.FAMILY.getName());
        param.setValue(getOriginalFamilyName());
        param.setDisplayName(EParameterName.FAMILY.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setNumRow(3);
        param.setReadOnly(true);
        param.setRequired(false);
        param.setShow(false);
        listParam.add(param);

        if (componentDefinition.isStartable()) {
            param = new ElementParameter(node);
            param.setName(EParameterName.START.getName());
            param.setValue(new Boolean(false));
            param.setDisplayName(EParameterName.START.getDisplayName());
            param.setFieldType(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.TECHNICAL);
            param.setNumRow(5);
            param.setReadOnly(true);
            param.setRequired(false);
            param.setShow(false);
            listParam.add(param);
        }

        param = new ElementParameter(node);
        param.setName(EParameterName.STARTABLE.getName());
        param.setValue(new Boolean(canStart()));
        param.setDisplayName(EParameterName.STARTABLE.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setNumRow(5);
        param.setReadOnly(true);
        param.setRequired(false);
        param.setShow(false);
        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.SUBTREE_START.getName());
        param.setValue(new Boolean(canStart()));
        param.setDisplayName(EParameterName.SUBTREE_START.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setNumRow(5);
        param.setReadOnly(true);
        param.setRequired(false);
        param.setShow(false);
        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.END_OF_FLOW.getName());
        param.setValue(new Boolean(canStart()));
        param.setDisplayName(EParameterName.END_OF_FLOW.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setNumRow(5);
        param.setReadOnly(true);
        param.setRequired(false);
        param.setShow(false);
        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.ACTIVATE.getName());
        param.setValue(new Boolean(true));
        param.setDisplayName(EParameterName.ACTIVATE.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setNumRow(5);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setDefaultValue(param.getValue());
        param.setShow(true);
        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.DUMMY.getName());
        param.setValue(Boolean.FALSE);
        param.setDefaultValue(Boolean.FALSE);
        param.setDisplayName(EParameterName.DUMMY.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setNumRow(5);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShow(false);
        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.HELP.getName());
        param.setValue(getTranslatedValue(PROP_HELP));
        param.setDisplayName(EParameterName.HELP.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setNumRow(6);
        param.setReadOnly(true);
        param.setRequired(false);
        param.setShow(false);
        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.UPDATE_COMPONENTS.getName());
        param.setValue(new Boolean(false));
        param.setDisplayName(EParameterName.UPDATE_COMPONENTS.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setNumRow(5);
        param.setReadOnly(true);
        param.setRequired(false);
        param.setShow(false);
        listParam.add(param);

        //
        param = new ElementParameter(node);
        param.setName(EParameterName.IREPORT_PATH.getName());
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.DIRECTORY);
        param.setDisplayName(EParameterName.IREPORT_PATH.getDisplayName());
        param.setNumRow(99);
        param.setShow(false);
        param.setValue(CorePlugin.getDefault().getPluginPreferences().getString(ITalendCorePrefConstants.IREPORT_PATH));
        param.setReadOnly(true);
        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.SUBJOB_COLOR.getName());
        param.setValue("");//$NON-NLS-1$ // TODO
        param.setDisplayName(EParameterName.SUBJOB_COLOR.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.ADVANCED);
        param.setNumRow(99);
        param.setReadOnly(true);
        param.setShow(false);
        param.setDefaultValue(param.getValue());
        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.SUBJOB_TITLE_COLOR.getName());
        param.setValue("");//$NON-NLS-1$ // TODO
        param.setDisplayName(EParameterName.SUBJOB_TITLE_COLOR.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.ADVANCED);
        param.setNumRow(99);
        param.setReadOnly(true);
        param.setShow(false);
        param.setDefaultValue(param.getValue());
        listParam.add(param);
        //
        param = new ElementParameter(node);
        param.setName("PROPERTY");//$NON-NLS-1$
        param.setCategory(EComponentCategory.BASIC);
        param.setDisplayName(EParameterName.PROPERTY_TYPE.getDisplayName());
        param.setFieldType(EParameterFieldType.PROPERTY_TYPE);
        param.setRepositoryValue(getRepositoryType());
        param.setValue("");//$NON-NLS-1$
        param.setNumRow(2);

        ElementParameter newParam = new ElementParameter(node);
        newParam.setCategory(EComponentCategory.BASIC);
        newParam.setName(EParameterName.PROPERTY_TYPE.getName());
        newParam.setDisplayName(EParameterName.PROPERTY_TYPE.getDisplayName());
        newParam.setListItemsDisplayName(new String[] { TEXT_BUILTIN, TEXT_REPOSITORY });
        newParam.setListItemsDisplayCodeName(new String[] { BUILTIN, REPOSITORY });
        newParam.setListItemsValue(new String[] { BUILTIN, REPOSITORY });
        newParam.setValue(BUILTIN);
        newParam.setNumRow(param.getNumRow());
        newParam.setFieldType(EParameterFieldType.TECHNICAL);
        newParam.setShow(false);
        newParam.setShowIf(param.getName() + " =='" + REPOSITORY + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        newParam.setReadOnly(param.isReadOnly());
        newParam.setNotShowIf(param.getNotShowIf());
        newParam.setContext(IComponentConstants.CONTEXT);
        newParam.setSerialized(true);
        newParam.setParentParameter(param);

        newParam = new ElementParameter(node);
        newParam.setCategory(EComponentCategory.BASIC);
        newParam.setName(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
        newParam.setDisplayName(EParameterName.REPOSITORY_PROPERTY_TYPE.getDisplayName());
        newParam.setListItemsDisplayName(new String[] {});
        newParam.setListItemsValue(new String[] {});
        newParam.setNumRow(param.getNumRow());
        newParam.setFieldType(EParameterFieldType.TECHNICAL);
        newParam.setValue(""); //$NON-NLS-1$
        newParam.setShow(false);
        newParam.setRequired(true);
        newParam.setReadOnly(param.isReadOnly());
        newParam.setShowIf(param.getName() + " =='" + REPOSITORY + "'"); //$NON-NLS-1$//$NON-NLS-2$
        newParam.setNotShowIf(param.getNotShowIf());
        newParam.setContext(IComponentConstants.CONTEXT);
        newParam.setSerialized(true);
        newParam.setParentParameter(param);
        listParam.add(param);
    }

    private void addPropertyParameters(final List<ElementParameter> listParam, final INode node, boolean advanced) {
        EComponentCategory category = advanced ? EComponentCategory.ADVANCED : EComponentCategory.BASIC;
        ComponentProperties props = ComponentsUtils.getComponentProperties(getName());
        Form form = props.getForm(advanced ? IComponentConstants.FORM_ADVANCED : IComponentConstants.FORM_MAIN);
        if (node.getComponentProperties() != null) {
            props = node.getComponentProperties();
            form = props.getForm(advanced ? IComponentConstants.FORM_ADVANCED : IComponentConstants.FORM_MAIN);
        }
        listParam.addAll(
                ComponentsUtils.getParametersFromForm(node, category, node.getComponentProperties(), null, form, null, null));
    }

    private void initializePropertyParameters(List<ElementParameter> listParam, final INode node) {
        for (ElementParameter param : listParam) {
            if (param.getDefaultValues().size() > 0) {
                boolean isSet = false;
                if (param.getFieldType().equals(EParameterFieldType.COMMAND)) {
                    // convert the values of COMMMAND
                    param.setValue(MultiDefaultValuesUtils.convertDefaultValues(param));
                    continue;
                }
                for (IElementParameterDefaultValue defaultValue : param.getDefaultValues()) {
                    String conditionIf = defaultValue.getIfCondition();
                    String conditionNotIf = defaultValue.getNotIfCondition();

                    if (param.isCondition(conditionIf, conditionNotIf, listParam)) {
                        isSet = true;
                        if (param.getFieldType().equals(EParameterFieldType.RADIO)
                                || param.getFieldType().equals(EParameterFieldType.CHECK)
                                || param.getFieldType().equals(EParameterFieldType.AS400_CHECK)) {
                            param.setValue(new Boolean(defaultValue.getDefaultValue().toString()));
                        } else {
                            param.setValue(defaultValue.getDefaultValue());
                        }
                    }
                }
                if (!isSet) {
                    if (param.getFieldType().equals(EParameterFieldType.RADIO)
                            || param.getFieldType().equals(EParameterFieldType.CHECK)
                            || param.getFieldType().equals(EParameterFieldType.AS400_CHECK)) {
                        int index = this.computeIndex(listParam, param);
                        if (index >= 0) {
                            param.setValue(new Boolean(param.getDefaultValues().get(index).getDefaultValue().toString()));
                        }
                    } else {
                        int index = this.computeIndex(listParam, param);
                        if (index >= 0) {
                            Object defaultValue = param.getDefaultValues().get(index).getDefaultValue();
                            param.setValue(defaultValue);
                            if (param.getFieldType() == EParameterFieldType.ENCODING_TYPE) {
                                String encodingType = TalendTextUtils.removeQuotes((String) defaultValue);
                                IElementParameter elementParameter = param.getChildParameters()
                                        .get(EParameterName.ENCODING_TYPE.getName());
                                if (elementParameter != null) {
                                    elementParameter.setValue(encodingType);
                                }
                            }
                        }
                    }
                }
            }
        }
        initializePropertyParametersForSchema(listParam, node);
    }

    private int computeIndex(List<ElementParameter> listParam, ElementParameter param) {
        String[] types = null;
        int index = 0;
        boolean isDBTYPEANDMYSQL = false;
        List<IElementParameterDefaultValue> elementParameterDefaultValueList = param.getDefaultValues();

        if (EParameterFieldType.MAPPING_TYPE.equals(param.getFieldType())) {
            for (IElementParameterDefaultValue elementParameterDefaultValue : elementParameterDefaultValueList) {
                String ifCondition = elementParameterDefaultValue.getIfCondition();
                if (ifCondition != null) {
                    types = ifCondition.split(EQUALS);
                    if (types.length == 2) {
                        if (types[0] != null && types[1] != null) {
                            for (ElementParameter elementParameter : listParam) {
                                if (types[0].equals(elementParameter.getName())
                                        && types[1].substring(1, types[1].length() - 1).equals(elementParameter.getValue())) {
                                    isDBTYPEANDMYSQL = true;
                                    break;
                                }
                            }
                            if (isDBTYPEANDMYSQL) {
                                index = param.getDefaultValues().indexOf(elementParameterDefaultValue);
                                isDBTYPEANDMYSQL = false;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return index;
    }

    /**
     * Sometimes the property parameters of schema are base on other parameters,but they might be initialized after the
     * schema. So there need to initialize the schema's again.
     * 
     * @param listParam
     */
    private void initializePropertyParametersForSchema(List<ElementParameter> listParam, final INode node) {
        for (ElementParameter param : listParam) {
            if (param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                    || param.getFieldType().equals(EParameterFieldType.DCSCHEMA)) {
                ElementParameter newParam = new ElementParameter(node);
                newParam.setCategory(EComponentCategory.BASIC);
                newParam.setName(EParameterName.SCHEMA_TYPE.getName());
                newParam.setDisplayName(EParameterName.SCHEMA_TYPE.getDisplayName());
                newParam.setListItemsDisplayName(new String[] { TEXT_BUILTIN, TEXT_REPOSITORY });
                newParam.setListItemsDisplayCodeName(new String[] { BUILTIN, REPOSITORY });
                newParam.setListItemsValue(new String[] { BUILTIN, REPOSITORY });
                newParam.setValue(BUILTIN);
                newParam.setNumRow(param.getNumRow());
                newParam.setFieldType(EParameterFieldType.TECHNICAL);
                newParam.setShow(false);
                newParam.setShowIf(param.getName() + " =='" + REPOSITORY + "'"); //$NON-NLS-1$ //$NON-NLS-2$
                newParam.setReadOnly(param.isReadOnly());
                newParam.setNotShowIf(param.getNotShowIf());
                newParam.setContext(IComponentConstants.CONTEXT);
                newParam.setSerialized(true);
                newParam.setParentParameter(param);

                newParam = new ElementParameter(node);
                newParam.setCategory(EComponentCategory.BASIC);
                newParam.setName(EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
                newParam.setDisplayName(EParameterName.REPOSITORY_SCHEMA_TYPE.getDisplayName());
                newParam.setListItemsDisplayName(new String[] {});
                newParam.setListItemsValue(new String[] {});
                newParam.setNumRow(param.getNumRow());
                newParam.setFieldType(EParameterFieldType.TECHNICAL);
                newParam.setValue(""); //$NON-NLS-1$
                newParam.setShow(false);
                newParam.setRequired(true);
                newParam.setReadOnly(param.isReadOnly());
                newParam.setShowIf(param.getName() + " =='" + REPOSITORY + "'"); //$NON-NLS-1$//$NON-NLS-2$
                newParam.setNotShowIf(param.getNotShowIf());
                newParam.setContext(IComponentConstants.CONTEXT);
                newParam.setSerialized(true);
                newParam.setParentParameter(param);
            }
        }
    }

    public void addItemsPropertyParameters(String paramName, ITEMSType items, ElementParameter param, EParameterFieldType type,
            INode node) {
        ITEMType item;
        ElementParameter newParam;

        int nbItems = 0;
        if (items != null) {
            if (items.isSetBASEDONSCHEMA()) {
                param.setBasedOnSchema(items.isBASEDONSCHEMA());
            }
            if (items.isSetBASEDONSUBJOBSTARTS()) {
                param.setBasedOnSubjobStarts(items.isBASEDONSUBJOBSTARTS());
            }
            if (items.isSetCOLUMNSBASEDONSCHEMA()) {
                param.setColumnsBasedOnSchema(items.isCOLUMNSBASEDONSCHEMA());
            }
            nbItems = items.getITEM().size();
            if (param.isBasedOnSchema() || param.isBasedOnSubjobStarts()) {
                nbItems++;
            }
        }
        String[] listRepositoryItem = new String[nbItems];
        String[] listItemsDisplayValue = new String[nbItems];
        String[] listItemsDisplayCodeValue = new String[nbItems];
        Object[] listItemsValue = new Object[nbItems];
        String[] listItemsShowIf = new String[nbItems];
        String[] listItemsNotShowIf = new String[nbItems];
        String[] listField = new String[nbItems];
        String[] listReadonlyIf = new String[nbItems];
        String[] listNotReadonlyIf = new String[nbItems];

        for (int k = 0; k < nbItems; k++) {
            int currentItem = k;
            if (param.isBasedOnSchema() || param.isBasedOnSubjobStarts()) {
                if (k == 0) {
                    if (param.isBasedOnSchema()) {
                        listItemsDisplayCodeValue[k] = "SCHEMA_COLUMN"; //$NON-NLS-1$
                        listItemsDisplayValue[k] = "Column"; //$NON-NLS-1$
                        listField[k] = ""; //$NON-NLS-1$
                        listRepositoryItem[k] = ""; //$NON-NLS-1$
                        listItemsShowIf[k] = null;
                        listItemsNotShowIf[k] = null;
                        newParam = new ElementParameter(node);
                        newParam.setName("SCHEMA_COLUMN"); //$NON-NLS-1$
                        newParam.setDisplayName(""); //$NON-NLS-1$
                        newParam.setFieldType(EParameterFieldType.TEXT);
                        newParam.setValue(""); //$NON-NLS-1$
                        listItemsValue[k] = newParam;
                        continue;
                    } else { // based on subjobs starts
                        listItemsDisplayCodeValue[k] = "SUBJOB_START_COLUMN"; //$NON-NLS-1$
                        listItemsDisplayValue[k] = "Subjob Start"; //$NON-NLS-1$
                        listField[k] = ""; //$NON-NLS-1$
                        listRepositoryItem[k] = ""; //$NON-NLS-1$
                        listItemsShowIf[k] = null;
                        listItemsNotShowIf[k] = null;
                        newParam = new ElementParameter(node);
                        newParam.setName("SUBJOB_START_COLUMN"); //$NON-NLS-1$
                        newParam.setDisplayName(""); //$NON-NLS-1$
                        newParam.setFieldType(EParameterFieldType.TEXT);
                        newParam.setValue(""); //$NON-NLS-1$
                        listItemsValue[k] = newParam;
                        continue;
                    }
                } else {
                    currentItem = k - 1;
                }
            }
            item = (ITEMType) items.getITEM().get(currentItem);
            listItemsDisplayCodeValue[k] = item.getNAME();
            // wzhang modified for 10846
            boolean displayAsValue = item.isDISPLAYNAMEASVALUE();
            if (displayAsValue) {
                String value = item.getVALUE();
                if (value != null) {
                    listItemsDisplayValue[k] = value;
                } else {
                    listItemsDisplayValue[k] = getTranslatedValue(paramName + ".ITEM." + item.getNAME()); //$NON-NLS-1$
                }
            } else {
                listItemsDisplayValue[k] = getTranslatedValue(paramName + ".ITEM." + item.getNAME()); //$NON-NLS-1$
            }
            if (type == EParameterFieldType.ROUTE_COMPONENT_TYPE) {
                listItemsValue[k] = new String[] { item.getNAME(), item.getFILTER() };
                // {component name, attributes filter}
            } else if (type != EParameterFieldType.TABLE && type != EParameterFieldType.TREE_TABLE
                    && type != EParameterFieldType.TABLE_BY_ROW) {
                listItemsValue[k] = item.getVALUE();
            } else {
                EParameterFieldType currentField = EParameterFieldType.getFieldTypeByName(item.getFIELD());
                newParam = new ElementParameter(node);
                newParam.setName(item.getNAME());
                newParam.setFilter(item.getFILTER());
                newParam.setDisplayName(""); //$NON-NLS-1$
                newParam.setFieldType(currentField);
                newParam.setContext(item.getCONTEXT());
                newParam.setShowIf(item.getSHOWIF());
                newParam.setNotShowIf(item.getNOTSHOWIF());
                newParam.setReadOnlyIf(item.getREADONLYIF());
                newParam.setNotReadOnlyIf(item.getNOTREADONLYIF());
                newParam.setNoContextAssist(item.isNOCONTEXTASSIST());
                newParam.setRaw(item.isRAW());
                if (item.isSetREADONLY()) {
                    newParam.setReadOnly(item.isREADONLY());
                }
                switch (currentField) {
                case CLOSED_LIST:
                case OPENED_LIST:
                case COLUMN_LIST:
                case COMPONENT_LIST:
                case CONNECTION_LIST:
                case DBTYPE_LIST:
                case LOOKUP_COLUMN_LIST:
                case PREV_COLUMN_LIST:
                case CONTEXT_PARAM_NAME_LIST:
                case MODULE_LIST:
                    addItemsPropertyParameters(paramName + ".ITEM." + item.getNAME(), item.getITEMS(), newParam, currentField, //$NON-NLS-1$
                            node);
                    break;
                case COLOR:
                    newParam.setValue(DEFAULT_COLOR);
                    break;
                case CHECK:
                case RADIO:
                    if (item.getVALUE() == null || item.getVALUE().equals("")) { //$NON-NLS-1$
                        newParam.setValue(Boolean.FALSE);
                    } else {
                        newParam.setValue(new Boolean(item.getVALUE()));
                    }
                    break;
                case SCHEMA_TYPE:
                    newParam.setValue(""); //$NON-NLS-1$
                    break;
                case SAP_SCHEMA_TYPE:
                    newParam.setValue(""); //$NON-NLS-1$
                    break;
                case SCHEMA_XPATH_QUERYS:
                    newParam.setValue(""); //$NON-NLS-1$
                    break;
                case RULE_TYPE:
                    newParam.setFieldType(EParameterFieldType.RULE_TYPE);
                    break;
                // case VALIDATION_RULE_TYPE:
                // newParam.setFieldType(EParameterFieldType.VALIDATION_RULE_TYPE);
                // break;
                default: // TEXT by default
                    newParam.setFieldType(EParameterFieldType.TEXT);
                    if (item.getVALUE() == null || item.getVALUE().equals("")) { //$NON-NLS-1$
                        newParam.setValue(""); //$NON-NLS-1$
                    } else {
                        newParam.setValue(item.getVALUE());
                    }

                }
                listItemsValue[k] = newParam;
            }
            listField[k] = item.getFIELD();
            listRepositoryItem[k] = item.getREPOSITORYITEM();
            listItemsShowIf[k] = item.getSHOWIF();
            listItemsNotShowIf[k] = item.getNOTSHOWIF();
            listReadonlyIf[k] = item.getREADONLYIF();
            listNotReadonlyIf[k] = item.getNOTREADONLYIF();
        }

        param.setListItemsDisplayName(listItemsDisplayValue);
        param.setListItemsDisplayCodeName(listItemsDisplayCodeValue);
        param.setListItemsValue(listItemsValue);
        param.setListRepositoryItems(listRepositoryItem);
        param.setListItemsShowIf(listItemsShowIf);
        param.setListItemsNotShowIf(listItemsNotShowIf);
        param.setListItemsNotReadOnlyIf(listNotReadonlyIf);
        param.setListItemsReadOnlyIf(listReadonlyIf);
        if (type != EParameterFieldType.TABLE && type != EParameterFieldType.TREE_TABLE) {
            Object defaultValue = ""; //$NON-NLS-1$
            if (items != null) {
                if (items.getDEFAULT() != null) {
                    boolean found = false;
                    String temp = items.getDEFAULT();
                    for (int i = 0; i < listItemsDisplayCodeValue.length & !found; i++) {
                        if (listItemsDisplayCodeValue[i].equals(items.getDEFAULT())) {
                            found = true;
                            temp = (String) listItemsValue[i];
                        }
                    }
                    defaultValue = new String(temp);
                }
            }
            param.setDefaultClosedListValue(defaultValue);
            param.setValue(defaultValue);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#hasConditionalOutputs()
     */
    @Override
    public boolean hasConditionalOutputs() {
        return false;
    }

    @Override
    public String getShortName() {
        String originalComponentName = getName();
        String calculatedShortName = "";//$NON-NLS-1$
        char[] cars = new char[originalComponentName.length()];
        int nbChars = 0;

        for (int i = 0; i < originalComponentName.length(); i++) {
            for (char c = 'A'; c <= 'Z'; c++) {
                if (originalComponentName.charAt(i) == c) {
                    cars[nbChars] = originalComponentName.charAt(i);
                    nbChars++;
                }
            }
        }
        calculatedShortName = String.copyValueOf(cars, 0, nbChars);
        calculatedShortName = calculatedShortName.toLowerCase();
        return calculatedShortName;
    }

    public boolean canStart() {
        return componentDefinition.isStartable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.components.IComponent#createConnectors()
     */
    @Override
    public List<NodeConnector> createConnectors(INode parentNode) {
        NodeConnector nodeConnector;
        List<NodeConnector> listConnector = new ArrayList<NodeConnector>();
        ComponentConnector[] listConnectors = componentDefinition.getConnectors();
        for (ComponentConnector componentConnector : listConnectors) {
            String originalConnectorName = componentConnector.getType().name();
            String connectorName = originalConnectorName;
            boolean isFlow_Sub_ConnectorName = IComponentConstants.MAIN_CONNECTOR_NAME.equals(originalConnectorName)
                    || IComponentConstants.REJECT_CONNECTOR_NAME.equals(originalConnectorName);
            if (isFlow_Sub_ConnectorName) {
                connectorName = "FLOW";//$NON-NLS-1$
            }
            EConnectionType currentType = EConnectionType.getTypeFromName(connectorName);
            if (currentType == null || ("LOOKUP").equals(connectorName) || ("MERGE").equals(connectorName)) {//$NON-NLS-1$//$NON-NLS-2$
                if (currentType == null) {
                    log.warn(Messages.getString("Component.componentNotExist", this.getName() //$NON-NLS-1$
                    , connectorName));
                }
                continue;
            }
            nodeConnector = new NodeConnector(parentNode);
            nodeConnector.setDefaultConnectionType(currentType);
            // set the default values
            nodeConnector.setLinkName(currentType.getDefaultLinkName());
            nodeConnector.setMenuName(currentType.getDefaultMenuName());
            RGB rgb = currentType.getRGB();
            Integer lineStyle = currentType.getDefaultLineStyle();
            if (!isFlow_Sub_ConnectorName) {
                nodeConnector.setMaxLinkInput(componentConnector.getMaxInput());
            }
            if ("FLOW".equals(connectorName) || "ITERATE".equals(connectorName)) {//$NON-NLS-1$//$NON-NLS-2$
                nodeConnector.setMaxLinkOutput(componentConnector.getMaxOutput());
            }
            if (nodeConnector.getName() == null) {
                if (isFlow_Sub_ConnectorName) {
                    nodeConnector.setName(originalConnectorName);
                    nodeConnector.setBaseSchema(currentType.getName());
                    if (IComponentConstants.REJECT_CONNECTOR_NAME.equals(originalConnectorName)) {
                        nodeConnector.setMenuName("Reject"); //$NON-NLS-1$
                        nodeConnector.setLinkName("Reject"); //$NON-NLS-1$
                    }
                } else {
                    nodeConnector.setName(connectorName);
                    nodeConnector.setBaseSchema(currentType.getName());
                }
            }
            nodeConnector.addConnectionProperty(currentType, rgb, lineStyle);
            listConnector.add(nodeConnector);
            if ("FLOW".equals(connectorName)) { //$NON-NLS-1$
                // if kind is "flow" (main type), then add the same for the lookup and merge.
                currentType = EConnectionType.FLOW_REF;

                rgb = currentType.getRGB();
                lineStyle = currentType.getDefaultLineStyle();
                nodeConnector.addConnectionProperty(currentType, rgb, lineStyle);
                nodeConnector.getConnectionProperty(currentType).setRGB(rgb);
                currentType = EConnectionType.FLOW_MERGE;

                rgb = currentType.getRGB();
                lineStyle = currentType.getDefaultLineStyle();
                nodeConnector.addConnectionProperty(currentType, rgb, lineStyle);
                nodeConnector.getConnectionProperty(currentType).setRGB(rgb);
            }
        }

        for (int i = 0; i < EConnectionType.values().length; i++) {
            EConnectionType currentType = EConnectionType.values()[i];

            if ((currentType == EConnectionType.FLOW_REF) || (currentType == EConnectionType.FLOW_MERGE)) {
                continue;
            }
            boolean exists = false;
            for (INodeConnector curNodeConn : listConnector) {
                if (curNodeConn.getDefaultConnectionType().equals(currentType)) {
                    exists = true;
                }
            }
            if (!exists) { // will add by default all connectors not defined in
                nodeConnector = new NodeConnector(parentNode);
                nodeConnector.setDefaultConnectionType(currentType);
                nodeConnector.setName(currentType.getName());
                nodeConnector.setBaseSchema(currentType.getName());
                nodeConnector.addConnectionProperty(currentType, currentType.getRGB(), currentType.getDefaultLineStyle());
                nodeConnector.setLinkName(currentType.getDefaultLinkName());
                nodeConnector.setMenuName(currentType.getDefaultMenuName());
                if ((currentType == EConnectionType.PARALLELIZE) || (currentType == EConnectionType.SYNCHRONIZE)) {
                    nodeConnector.setMaxLinkInput(1);
                } else {
                    nodeConnector.setMaxLinkInput(0);
                }
                nodeConnector.setMaxLinkOutput(0);
                nodeConnector.setMinLinkInput(0);
                nodeConnector.setMinLinkOutput(0);
                if (currentType == EConnectionType.FLOW_MAIN) {
                    nodeConnector.addConnectionProperty(EConnectionType.FLOW_REF, EConnectionType.FLOW_REF.getRGB(),
                            EConnectionType.FLOW_REF.getDefaultLineStyle());
                    nodeConnector.addConnectionProperty(EConnectionType.FLOW_MERGE, EConnectionType.FLOW_MERGE.getRGB(),
                            EConnectionType.FLOW_MERGE.getDefaultLineStyle());
                }
                listConnector.add(nodeConnector);
            }
        }
        return listConnector;
    }

    @Override
    public String getPluginExtension() {
        return null;
    }

    @Override
    public boolean isSchemaAutoPropagated() {
        return componentDefinition.isSchemaAutoPropagate();
    }

    @Override
    public boolean isDataAutoPropagated() {
        return componentDefinition.isDataAutoPropagate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isVisible()
     */
    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public boolean isVisible(String family) {
        return true;
    }

    @Override
    public boolean isVisibleInComponentDefinition() {
        return false;
    }

    @Override
    public String getVersion() {
        return "";//$NON-NLS-1$
    }

    @Override
    public List<ModuleNeeded> getModulesNeeded() {
        if (componentImportNeedsList != null) {
            return componentImportNeedsList;
        } else {
            ComponentService componentService = ComponentsUtils.getComponentService();
            Set<String> mavenUriDependencies = componentService.getMavenUriDependencies(getName());
            componentImportNeedsList = new ArrayList<>(mavenUriDependencies.size());
            for (String mvnUri : mavenUriDependencies) {
                ModuleNeeded moduleNeeded = new ModuleNeeded(getName(), "", true, mvnUri);
                componentImportNeedsList.add(moduleNeeded);
            }
            return componentImportNeedsList;
        }
    }

    @Override
    public List<IMultipleComponentManager> getMultipleComponentManagers() {
        if (multipleComponentManagers == null) {
            multipleComponentManagers = createMultipleComponentManagerFromTemplates();
        } // else already exist so return it
        return multipleComponentManagers;
    }

    private ArrayList<IMultipleComponentManager> createMultipleComponentManagerFromTemplates() {
        // TODO
        ArrayList<IMultipleComponentManager> theMultipleComponentManagers = new ArrayList<IMultipleComponentManager>();
        return theMultipleComponentManagers;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isLoaded()
     */
    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public void setImageRegistry(Map<String, ImageDescriptor> imageRegistry) {
        this.imageRegistry = imageRegistry;
    }

    @Override
    public Map<String, ImageDescriptor> getImageRegistry() {
        return imageRegistry;
    }

    @Override
    public ImageDescriptor getIcon16() {
        InputStream imageStream = ComponentsUtils.getComponentService().getComponentPngImage(getName(),
                ComponentImageType.PALLETE_ICON_32X32);
        if (imageStream != null) {
            ImageData imageData = new ImageData(imageStream);
            return ImageDescriptor.createFromImageData(imageData.scaledTo(16, 16));
        }
        return new DummyComponent("dummy").getIcon16(); //$NON-NLS-1$
    }

    @Override
    public ImageDescriptor getIcon24() {
        InputStream imageStream = ComponentsUtils.getComponentService().getComponentPngImage(getName(),
                ComponentImageType.PALLETE_ICON_32X32);
        if (imageStream != null) {
            ImageData imageData = new ImageData(imageStream);
            return ImageDescriptor.createFromImageData(imageData.scaledTo(24, 24));
        }
        return new DummyComponent("dummy").getIcon24();//$NON-NLS-1$
    }

    /**
     * Getter for icon32.
     * 
     * @return the icon32
     */
    @Override
    public ImageDescriptor getIcon32() {
        InputStream imageStream = ComponentsUtils.getComponentService().getComponentPngImage(getName(),
                ComponentImageType.PALLETE_ICON_32X32);
        if (imageStream != null) {
            ImageData imageData = new ImageData(imageStream);
            return ImageDescriptor.createFromImageData(imageData);
        }
        return new DummyComponent("dummy").getIcon32();//$NON-NLS-1$
    }

    @Override
    public String getPathSource() {
        return null;
    }

    private ArrayList<ECodePart> createCodePartList() {
        ArrayList<ECodePart> theCodePartList = new ArrayList<ECodePart>();
        theCodePartList.add(ECodePart.BEGIN);
        theCodePartList.add(ECodePart.MAIN);
        theCodePartList.add(ECodePart.END);
        return theCodePartList;
    }

    @Override
    public List<ECodePart> getAvailableCodeParts() {
        if (codePartListX == null) {
            codePartListX = createCodePartList();
        }
        return codePartListX;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getPluginDependencies() {
        List<String> pluginDependencyList = new ArrayList<String>();
        return pluginDependencyList;
    }

    @Override
    public boolean useMerge() {
        return false;
    }

    public boolean useFlow() {
        return false;
    }

    public boolean useSchema() {
        return false;
    }

    @Override
    public boolean isMultiplyingOutputs() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getComponentType()
     */
    @Override
    public boolean isMultipleOutput() {
        return false;
    }

    public boolean isMultiSchemaOutput() {
        return false;
    }

    private boolean connectorUseInputLinkSelection(String name) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#useLookup()
     */
    @Override
    public boolean useLookup() {
        return false;
    }

    @Override
    public boolean useImport() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getComponentType()
     */
    @Override
    public EComponentType getComponentType() {
        return EComponentType.GENERIC;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isHashComponent()
     */
    @Override
    public boolean isHashComponent() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isTechnical()
     */
    @Override
    public boolean isTechnical() {
        return false;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isSingleton()
     */
    @Override
    public boolean isSingleton() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isMainCodeCalled()
     */
    @Override
    public boolean isMainCodeCalled() {
        return false;
    }

    /**
     * get this component's repository type <br>
     * see <PARAMETER NAME="PROPERTY" ...> in the component's xml definition.
     * 
     * @return
     */
    @Override
    public String getRepositoryType() {
        // FIXME - this is the name of the object stored in the repository, need to put this in the definition
        return "salesforce";
    }

    @Override
    public boolean canParallelize() {
        return false;
    }

    @Override
    public String getCombine() {
        return "";//$NON-NLS-1$
    }

    @Override
    public IProcess getProcess() {
        return null;
    }

    /**
     * Getter for type.
     * 
     * @return the type
     */
    @Override
    public String getType() {
        return "DI";//$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getInputType()
     */
    @Override
    public String getInputType() {
        return "";//$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getOutputType()
     */
    @Override
    public String getOutputType() {
        return "";//$NON-NLS-1$
    }

    /**
     * Getter for reduce.
     * 
     * @return the reduce
     */
    @Override
    public boolean isReduce() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isSparkAction()
     */
    @Override
    public boolean isSparkAction() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.components.AbstractComponent#setPaletteType(java.lang.String)
     */
    @Override
    public void setPaletteType(String paletteType) {
        super.setPaletteType(paletteType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getPartitioning()
     */
    @Override
    public String getPartitioning() {
        return "";//$NON-NLS-1$
    }

    /**
     * Getter for provider.
     * 
     * @return the provider
     */
    public ComponentsProvider getProvider() {
        return this.provider;
    }

    /**
     * Sets the provider.
     * 
     * @param provider the provider to set
     */
    public void setProvider(ComponentsProvider provider) {
        this.provider = provider;
    }

    @Override
    public boolean isSupportDbType() {
        return false;
    }

    public String getBundleName() {
        return IComponentsFactory.COMPONENTS_LOCATION;
    }

    public static class CodegenPropInfo {

        public String fieldName;

        public String className;

        public ComponentProperties props;
    }

    protected void processCodegenPropInfos(List<CodegenPropInfo> propList, ComponentProperties props, String fieldString) {
        for (NamedThing prop : props.getProperties()) {
            if (prop instanceof ComponentProperties) {
                CodegenPropInfo childPropInfo = new CodegenPropInfo();
                if (fieldString.equals("")) {//$NON-NLS-1$
                    childPropInfo.fieldName = "." + prop.getName();//$NON-NLS-1$
                } else {
                    childPropInfo.fieldName = fieldString + "." + prop.getName();//$NON-NLS-1$
                }
                childPropInfo.className = prop.getClass().getName();
                childPropInfo.props = (ComponentProperties) prop;
                propList.add(childPropInfo);
                processCodegenPropInfos(propList, childPropInfo.props, childPropInfo.fieldName);
            }
        }
    }

    public List<CodegenPropInfo> getCodegenPropInfos(ComponentProperties props) {
        List<CodegenPropInfo> propsList = new ArrayList<>();
        CodegenPropInfo propInfo = new CodegenPropInfo();
        propInfo.fieldName = "";//$NON-NLS-1$
        propInfo.className = props.getClass().getName();
        propInfo.props = props;
        propsList.add(propInfo);
        processCodegenPropInfos(propsList, props, propInfo.fieldName);
        return propsList;
    }

    public String getCodegenValue(SchemaElement property, String value) {
        if (property.getType() == SchemaElement.Type.ENUM) {
            return "\"" + value + "\"";//$NON-NLS-1$ //$NON-NLS-2$
        }
        if (property.getType() == SchemaElement.Type.SCHEMA) {
            // Handles embedded escaped quotes which might occur
            return "\"" + value.replace("\\\"", "\\\\\"").replace("\"", "\\\"") + "\"";//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        }
        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((this.getPaletteType() == null) ? 0 : this.getPaletteType().hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Component other = (Component) obj;
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        if (this.getPaletteType() == null) {
            if (other.getPaletteType() != null) {
                return false;
            }
        } else if (!this.getPaletteType().equals(other.getPaletteType())) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isLog4JEnabled()
     */
    @Override
    public boolean isLog4JEnabled() {
        return false;
    }

    public String getEquivalent() {
        return "";//$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getCONNECTORList()
     */
    @Override
    public EList getCONNECTORList() {
        return null;
    }

    @Override
    public void initNodePropertiesFromSerialized(INode node, String serialized) {
        if (node != null) {
            node.setComponentProperties(ComponentProperties.fromSerialized(serialized).properties);
        }
    }

    @Override
    public void initParamPropertiesFromSerialized(IElementParameter param, String serialized) {
        if (param instanceof GenericElementParameter) {
            Deserialized fromSerialized = ComponentProperties.fromSerialized(serialized);
            if (fromSerialized != null) {
                ComponentProperties componentProperties = fromSerialized.properties;
                ((GenericElementParameter) param).setComponentProperties(
                        ComponentsUtils.getCurrentComponentProperties(componentProperties, param.getName()));
            }
        }
    }

    @Override
    public Object getElementParameterValue(IElementParameter param) {
        if (param instanceof GenericElementParameter) {
            return ComponentsUtils.getGenericPropertyValue(((GenericElementParameter) param).getComponentProperties(),
                    param.getName());
        }
        return null;
    }

    @Override
    public Object genericFromSerialized(String serialized, String name) {
        Deserialized fromSerialized = ComponentProperties.fromSerialized(serialized);
        if (fromSerialized != null) {
            ComponentProperties componentProperties = fromSerialized.properties;
            return ComponentsUtils.getGenericPropertyValue(componentProperties, name);
        }
        return null;
    }

    @Override
    public String genericToSerialized(IElementParameter param) {
        if (param instanceof GenericElementParameter) {
            return ((Node) ((GenericElementParameter) param).getElement()).getComponentProperties().toSerialized();
        } else {
            ComponentProperties componentProperties = ComponentsUtils.getComponentProperties(getName());
            return componentProperties.toSerialized();
        }
    }
}
