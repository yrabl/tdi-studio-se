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
package org.talend.component.ui.wizard.ui.dnd;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.component.core.constants.IComponentConstants;
import org.talend.component.core.dnd.AbstractComponentDragAndDropHandler;
import org.talend.component.core.model.GenericElementParameter;
import org.talend.component.core.utils.ComponentsUtils;
import org.talend.component.core.utils.SchemaUtils;
import org.talend.component.ui.model.genericMetadata.GenericConnection;
import org.talend.component.ui.model.genericMetadata.GenericConnectionItem;
import org.talend.components.api.NamedThing;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentProperties.Deserialized;
import org.talend.components.api.properties.Property;
import org.talend.components.api.schema.SchemaElement.Type;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsService;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.IComponentName;
import org.talend.core.repository.RepositoryComponentSetting;
import org.talend.repository.model.RepositoryNode;

/**
 * 
 * created by hcyi on Oct 26, 2015 Detailled comment
 *
 */
public class GenericDragAndDropHandler extends AbstractComponentDragAndDropHandler {

    private static final String SALESFORCE = "salesforce"; //$NON-NLS-1$

    public static final String COMPONENT_T_SALSEFORCE_CONNECTION = "tSalesforceConnectionNew"; //$NON-NLS-1$

    public static final String COMPONENT_T_SALSEFORCE_WAVE_BULK_EXEC = "tSalesforceWaveBulkExecNew"; //$NON-NLS-1$

    public static final String COMPONENT_T_SALSEFORCE_WAVE_OUTPUT_BULK_EXEC = "tSalesforceWaveOutputBulkExecNew"; //$NON-NLS-1$

    public static final String COMPONENT_T_SALSEFORCE_INPUT = "tSalesforceInputNew"; //$NON-NLS-1$

    public static final String COMPONENT_T_SALSEFORCE_OUTPUT = "tSalesforceOutputNew"; //$NON-NLS-1$

    @Override
    public boolean canHandle(Connection connection) {
        return connection instanceof GenericConnection;
    }

    @Override
    public Object getComponentValue(Connection connection, String value, IMetadataTable table, String targetComponent) {
        if (value != null && canHandle(connection)) {
            return getGenericRepositoryValue((GenericConnection) connection, value, table);
        }
        return null;
    }

    private Object getGenericRepositoryValue(GenericConnection connection, String value, IMetadataTable table) {
        if (connection == null) {
            return null;
        }
        String serialized = connection.getCompProperties();
        if (serialized != null) {
            Deserialized fromSerialized = ComponentProperties.fromSerialized(serialized);
            if (fromSerialized != null) {
                ComponentProperties componentProperties = fromSerialized.properties;
                String paramName = ComponentsUtils.getPropertyName(value);
                if (value != null && value.startsWith(componentProperties.getName())) {
                    if (value.indexOf(IComponentConstants.EXP_SEPARATOR) != -1) {
                        value = value.substring(componentProperties.getName().length() + 1, value.length());
                    }
                }
                if (IComponentConstants.SCHEMA.equalsIgnoreCase(paramName)) {
                    ComponentProperties componentModuleProperties = SchemaUtils.getCurrentComponentProperties(table);
                    if (componentModuleProperties != null) {
                        NamedThing namedThing = componentModuleProperties.getProperty(paramName);
                        if (namedThing != null && namedThing instanceof ComponentProperties) {
                            return ComponentsUtils.getGenericPropertyValue(componentModuleProperties,
                                    namedThing.getName() + IComponentConstants.EXP_SEPARATOR + paramName);
                        }
                    }
                } else if (IComponentConstants.MODULENAME.equalsIgnoreCase(paramName)) {
                    ComponentProperties componentModuleProperties = SchemaUtils.getCurrentComponentProperties(table);
                    if (componentModuleProperties != null) {
                        Property property = componentModuleProperties.getValuedProperty(paramName);
                        if (property != null) {
                            if (Type.STRING.equals(property.getType())) {
                                return getRepositoryValueOfStringType(connection, StringUtils.trimToNull(table.getLabel()));
                            } else {
                                return table.getLabel();
                            }
                        }
                    }
                } else if (IComponentConstants.QUERYMODE.equalsIgnoreCase(paramName)) {
                    if (ComponentsUtils.getGenericPropertyValue(componentProperties, value) != null) {
                        return ComponentsUtils.getGenericPropertyValue(componentProperties, value);
                    } else {
                        return IComponentConstants.QUERY_QUERY;
                    }
                } else if (IComponentConstants.OUTPUTACTION.equalsIgnoreCase(paramName)) {
                    if (ComponentsUtils.getGenericPropertyValue(componentProperties, value) != null) {
                        return ComponentsUtils.getGenericPropertyValue(componentProperties, value);
                    } else {
                        return IComponentConstants.ACTION_INSERT;
                    }
                } else if (IComponentConstants.LOGIN_TYPE.equalsIgnoreCase(paramName)) {
                    return ComponentsUtils.getGenericPropertyValue(componentProperties, value);
                } else {
                    Object object = ComponentsUtils.getGenericPropertyValue(componentProperties, value);
                    if (object != null && object instanceof String) {
                        return getRepositoryValueOfStringType(connection, StringUtils.trimToNull(object.toString()));
                    }
                    return ComponentsUtils.getGenericPropertyValue(componentProperties, value);
                }
            }
        }
        return null;
    }

    @Override
    public List<IComponent> filterNeededComponents(Item item, RepositoryNode seletetedNode, ERepositoryObjectType type) {
        List<IComponent> neededComponents = new ArrayList<IComponent>();
        if (!(item instanceof GenericConnectionItem)) {
            return neededComponents;
        }
        IComponentsService service = (IComponentsService) GlobalServiceRegister.getDefault().getService(IComponentsService.class);
        Set<IComponent> components = service.getComponentsFactory().getComponents();
        for (IComponent component : components) {
            if (EComponentType.GENERIC.equals(component.getComponentType())) {
                if (isValid(item, type, seletetedNode, component, type.getType()) && !neededComponents.contains(component)) {
                    neededComponents.add(component);
                }
            }
        }
        return neededComponents;
    }

    private boolean isValid(Item item, ERepositoryObjectType type, RepositoryNode seletetedNode, IComponent component,
            String repositoryType) {
        if (component == null || repositoryType == null) {
            return false;
        }
        String componentProductname = component.getRepositoryType();
        String typeName = ""; //$NON-NLS-1$
        if (item instanceof GenericConnectionItem) {
            typeName = ((GenericConnectionItem) item).getTypeName();
        }
        if (componentProductname != null && componentProductname.contains(repositoryType)
                || componentProductname.contains(typeName)) {
            String componentName = component.getName();
            if (ERepositoryObjectType.METADATA_SALESFORCE_MODULE == type || ERepositoryObjectType.METADATA_CON_TABLE == type
                    || ERepositoryObjectType.METADATA_CON_COLUMN == type) {
                if (COMPONENT_T_SALSEFORCE_INPUT.equals(componentName) || COMPONENT_T_SALSEFORCE_OUTPUT.equals(componentName)) {
                    return true;
                }
            } else {
                if (COMPONENT_T_SALSEFORCE_CONNECTION.equals(componentName)
                        || COMPONENT_T_SALSEFORCE_WAVE_BULK_EXEC.equals(componentName)
                        || COMPONENT_T_SALSEFORCE_WAVE_OUTPUT_BULK_EXEC.equals(componentName)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public IComponentName getCorrespondingComponentName(Item item, ERepositoryObjectType type) {
        RepositoryComponentSetting setting = null;
        if (item instanceof GenericConnectionItem) {
            setting = new RepositoryComponentSetting();
            setting.setName(SALESFORCE);
            setting.setRepositoryType(SALESFORCE);
            setting.setWithSchema(true);
            setting.setInputComponent(COMPONENT_T_SALSEFORCE_INPUT);
            setting.setOutputComponent(COMPONENT_T_SALSEFORCE_OUTPUT);
            List<Class<Item>> list = new ArrayList<Class<Item>>();
            Class clazz = null;
            try {
                clazz = Class.forName(GenericConnectionItem.class.getName());
            } catch (ClassNotFoundException e) {
                ExceptionHandler.process(e);
            }
            list.add(clazz);
            setting.setClasses(list.toArray(new Class[0]));
        }
        return setting;
    }

    private void setGenericRepositoryValue(GenericConnection connection, INode node, IElementParameter param) {
        if (connection != null && param != null && param instanceof GenericElementParameter) {
            if (connection.getCompProperties() == null) {
                GenericElementParameter genericParam = (GenericElementParameter) param;
                connection.setCompProperties(genericParam.getComponentProperties().toSerialized());
            }
        }
    }

    @Override
    public ERepositoryObjectType getType(String repositoryType) {
        if (SALESFORCE.equals(repositoryType)) {
            return ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA;
        }
        return null;
    }

    @Override
    public void handleTableRelevantParameters(Connection connection, IElement ele, IMetadataTable metadataTable) {
        // TODO
        if (ele == null || metadataTable == null) {
            return;
        }
    }

    @Override
    public void setComponentValue(Connection connection, INode node, IElementParameter param) {
        if (node != null && canHandle(connection)) {
            setGenericRepositoryValue((GenericConnection) connection, node, param);
        }
    }
}
