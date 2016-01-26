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
package org.talend.component.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.talend.commons.exception.BusinessException;
import org.talend.component.core.constants.IComponentConstants;
import org.talend.component.core.model.Component;
import org.talend.component.core.model.GenericElementParameter;
import org.talend.components.api.NamedThing;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.NameAndLabel;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Widget;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.schema.SchemaElement.Type;
import org.talend.components.api.service.ComponentService;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.INode;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.ElementParameter;

/**
 * created by hcyi on Sep 11, 2015 Detailled comment
 *
 */
public class ComponentsUtils {

    private static List<IComponent> components = null;

    public static ComponentService getComponentService() {
        ComponentService compService = null;
        BundleContext bundleContext = FrameworkUtil.getBundle(ComponentsUtils.class).getBundleContext();
        ServiceReference<ComponentService> compServiceRef = bundleContext.getServiceReference(ComponentService.class);
        if (compServiceRef != null) {
            compService = bundleContext.getService(compServiceRef);
        }
        return compService;
    }

    public static ComponentProperties getComponentProperties(String compName) {
        return getComponentService().getComponentProperties(compName);
    }

    public static List<ComponentDefinition> getPossibleComponents(ComponentProperties properties) {
        List<ComponentDefinition> possibleComponents = new ArrayList<ComponentDefinition>();
        try {
            possibleComponents.addAll(getComponentService().getPossibleComponents(properties));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return possibleComponents;
    }

    public static void loadComponents(ComponentService service) {
        IComponentsFactory componentsFactory = null;
        if (componentsFactory == null) {
            componentsFactory = ComponentsFactoryProvider.getInstance();
        }
        Set<IComponent> componentsList = componentsFactory.getComponents();
        if (components == null) {
            components = new ArrayList<IComponent>();
        } else {
            componentsList.removeAll(components);
        }
        Map<String, IComponent> existComponents = new HashMap<String, IComponent>();

        for (IComponent component : componentsList) {
            existComponents.put(component.getName(), component);
        }

        // Load components from service
        Set<ComponentDefinition> componentDefinitions = service.getAllComponents();
        for (ComponentDefinition componentDefinition : componentDefinitions) {
            try {
                Component currentComponent = new Component(componentDefinition);
                componentsList.add(currentComponent);
            } catch (BusinessException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<ElementParameter> getParametersFromForm(IElement element, EComponentCategory category, Form form,
            Widget parentWidget, AtomicInteger lastRowNum) {
        return getParametersFromForm(element, category, null, null, form, parentWidget, lastRowNum);
    }

    /**
     * DOC ycbai Comment method "loadParametersFromForm".
     * <p>
     * Get element parameters of <code>element</code> from <code>form</code>.
     * 
     * @param node optional, used if there is a component setting up the properties
     * @param element
     * @param category
     * @param form
     * @return parameters list
     */
    public static List<ElementParameter> getParametersFromForm(IElement element, EComponentCategory category,
            ComponentProperties compProperties, String parentPropertiesPath, Form form, Widget parentWidget,
            AtomicInteger lastRowNum) {
        List<ElementParameter> elementParameters = new ArrayList<>();
        EComponentCategory compCategory = category;
        if (compCategory == null) {
            compCategory = EComponentCategory.BASIC;
        }
        AtomicInteger lastRN = lastRowNum;
        if (lastRN == null) {
            lastRN = new AtomicInteger();
        }
        if (form == null) {
            return elementParameters;
        }
        ComponentProperties componentProperties = compProperties;
        if (componentProperties == null) {
            componentProperties = form.getComponentProperties();
        }
        if (element instanceof INode) {
            INode node = (INode) element;
            // Set the properties only one time to get the top-level properties object
            if (node.getComponentProperties() == null) {
                node.setComponentProperties(componentProperties);
            }
        }

        // Have to initialize for the messages
        componentProperties.getProperties();
        List<Widget> formWidgets = form.getWidgets();
        for (Widget widget : formWidgets) {
            NamedThing[] widgetProperties = widget.getProperties();
            NamedThing widgetProperty = widgetProperties[0];

            String propertiesPath = getPropertiesPath(parentPropertiesPath, null);
            if (widgetProperty instanceof Form) {
                Form subForm = (Form) widgetProperty;
                ComponentProperties subProperties = subForm.getComponentProperties();
                // Reset properties path
                if (!isSameComponentProperties(componentProperties, widgetProperty)) {
                    propertiesPath = getPropertiesPath(parentPropertiesPath, subProperties.getName());
                }
                elementParameters.addAll(
                        getParametersFromForm(element, compCategory, subProperties, propertiesPath, subForm, widget, lastRN));
                continue;
            }

            GenericElementParameter param = new GenericElementParameter(element, componentProperties, widget,
                    getComponentService());
            String parameterName = propertiesPath.concat(param.getName());
            param.setName(parameterName);
            param.setCategory(compCategory);
            param.setRepositoryValue(parameterName);
            param.setShow(parentWidget == null ? widget.isVisible() : parentWidget.isVisible() && widget.isVisible());
            int rowNum = 0;
            if (widget.getOrder() != 1) {
                rowNum = lastRN.get();
            } else {
                rowNum = widget.getRow();
                if (parentWidget != null) {
                    rowNum += parentWidget.getRow();
                }
                rowNum = rowNum + lastRN.get();
            }
            param.setNumRow(rowNum);
            lastRN.set(rowNum);
            // handle form...

            SchemaElement se = null;

            if (widgetProperty instanceof SchemaElement) {
                se = (SchemaElement) widgetProperties[0];
                param.setContext(EConnectionType.FLOW_MAIN.getName());
            }

            EParameterFieldType fieldType = getFieldType(widget, widgetProperty, se);
            param.setFieldType(fieldType != null ? fieldType : EParameterFieldType.TEXT);
            if (se == null) {
                param.setValue(widgetProperty.getDisplayName());
            } else {
                Property property = componentProperties.getValuedProperty(se.getName());
                if (property != null) {
                    param.setRequired(property.isRequired());
                    param.setValue(property.getValue());
                    param.setSupportContext(isSupportContext(property));
                    List<?> values = property.getPossibleValues();
                    if (values != null) {
                        param.setPossibleValues(values);
                        List<String> possVals = new ArrayList<>();
                        List<String> possValsDisplay = new ArrayList<>();
                        for (Object obj : values) {
                            if (obj instanceof NameAndLabel) {
                                NameAndLabel nal = (NameAndLabel) obj;
                                possVals.add(nal.getName());
                                possValsDisplay.add(nal.getLabel());
                            } else {
                                possVals.add(String.valueOf(obj));
                                possValsDisplay.add(String.valueOf(obj));
                            }
                        }
                        param.setListItemsDisplayName(possValsDisplay.toArray(new String[0]));
                        param.setListItemsDisplayCodeName(possValsDisplay.toArray(new String[0]));
                        param.setListItemsValue(possVals.toArray(new String[0]));
                    }
                } // else a ComponentProperties so ignor
            }
            // if (widgetProperty instanceof PresentationItem) {
            // param.setValue(null);
            // }
            param.setReadOnly(false);
            param.setSerialized(true);
            // Set param context when multiple schema
            if (EParameterFieldType.SCHEMA_TYPE.equals(param.getFieldType())) {
                String propertyName = componentProperties.getName();
                if (IComponentConstants.SCHEMA_FLOW.equals(propertyName)) {
                    param.setContext(EConnectionType.FLOW_MAIN.getDefaultMenuName().toUpperCase());
                } else if (IComponentConstants.SCHEMA_REJECT.equals(propertyName)) {
                    param.setContext(EConnectionType.REJECT.getName());
                }
            }
            elementParameters.add(param);
        }
        return elementParameters;
    }

    private static String getPropertiesPath(String parentPropertiesPath, String currentPropertiesName) {
        String propertiesPath = ""; //$NON-NLS-1$
        if (StringUtils.isNotBlank(currentPropertiesName)) {
            propertiesPath = propertiesPath.concat(currentPropertiesName).concat(IComponentConstants.EXP_SEPARATOR);
        }
        if (StringUtils.isNotBlank(parentPropertiesPath)) {
            propertiesPath = parentPropertiesPath.concat(propertiesPath);
        }
        return propertiesPath;
    }

    /**
     * DOC ycbai Comment method "getFieldType".
     * 
     * @param widget
     * @param widgetProperty
     * @param param
     * @param se
     * @return
     */
    private static EParameterFieldType getFieldType(Widget widget, NamedThing widgetProperty, SchemaElement se) {
        EParameterFieldType fieldType = null;
        switch (widget.getWidgetType()) {
        case DEFAULT:
            if (se == null) {
                fieldType = EParameterFieldType.LABEL;
                break;
            }
            switch (se.getType()) {
            case BOOLEAN:
                fieldType = EParameterFieldType.CHECK;
                break;
            case BYTE_ARRAY:
                fieldType = EParameterFieldType.TEXT;
                break;
            case DATE:
                fieldType = EParameterFieldType.DATE;
                break;
            case DATETIME:
                fieldType = EParameterFieldType.DATE;
                break;
            case DECIMAL:
                fieldType = EParameterFieldType.TEXT;
                break;
            case DOUBLE:
                fieldType = EParameterFieldType.TEXT;
                break;
            case DYNAMIC:
                fieldType = EParameterFieldType.TEXT;
                break;
            case ENUM:
                fieldType = EParameterFieldType.CLOSED_LIST;
                break;
            case FLOAT:
                fieldType = EParameterFieldType.TEXT;
                break;
            case INT:
                fieldType = EParameterFieldType.TEXT;
                break;
            case SCHEMA:
                fieldType = EParameterFieldType.SCHEMA_TYPE;
                break;
            case STRING:
                fieldType = EParameterFieldType.TEXT;
                break;
            default:
                fieldType = EParameterFieldType.TEXT;
                break;
            }
            break;
        case BUTTON:
            fieldType = EParameterFieldType.BUTTON;
            break;
        case COMPONENT_REFERENCE:
            fieldType = EParameterFieldType.COMPONENT_REFERENCE;
            break;
        case NAME_SELECTION_AREA:
            fieldType = EParameterFieldType.NAME_SELECTION_AREA;
            break;
        case NAME_SELECTION_REFERENCE:
            fieldType = EParameterFieldType.NAME_SELECTION_REFERENCE;
            break;
        case SCHEMA_EDITOR:
            break;
        case SCHEMA_REFERENCE:
            fieldType = EParameterFieldType.SCHEMA_TYPE;
            break;
        default:
            break;
        }
        return fieldType;
    }

    public static ComponentProperties getCurrentComponentProperties(ComponentProperties componentProperties, String paramName) {
        if (componentProperties == null || paramName == null) {
            return null;
        }
        String compPropertiesPath = getPropertyPath(paramName);
        if (StringUtils.isEmpty(compPropertiesPath)) {
            return componentProperties;
        }
        NamedThing property = componentProperties.getProperty(compPropertiesPath);
        if (property == null) {
            return getCurrentComponentPropertiesSpecial(componentProperties, paramName);
        }
        if (property instanceof ComponentProperties) {
            return (ComponentProperties) property;
        }
        return null;
    }

    public static ComponentProperties getCurrentComponentPropertiesSpecial(ComponentProperties componentProperties,
            String paramName) {
        ComponentProperties currentComponentProperties = null;
        if (componentProperties == null || paramName == null) {
            return null;
        }
        List<NamedThing> allProps = componentProperties.getProperties();
        for (NamedThing namedThing : allProps) {
            if (paramName.equals(namedThing.getName())) {
                currentComponentProperties = componentProperties;
                break;
            }
            if (namedThing instanceof ComponentProperties) {
                ComponentProperties childComponentProperties = (ComponentProperties) namedThing;
                currentComponentProperties = getCurrentComponentProperties(childComponentProperties, paramName);
            }
            if (currentComponentProperties != null) {
                break;
            }
        }
        return currentComponentProperties;
    }

    public static NamedThing getGenericSchemaElement(ComponentProperties componentProperties, String paramName) {
        if (componentProperties == null || paramName == null) {
            return null;
        }
        return componentProperties.getProperty(paramName);
    }

    public static Object getGenericPropertyValue(ComponentProperties componentProperties, String paramName) {
        Object obj = null;
        if (componentProperties == null || paramName == null) {
            return null;
        }
        ComponentProperties currentComponentProperties = getCurrentComponentProperties(componentProperties, paramName);
        if (currentComponentProperties == null) {
            return null;
        }
        Property property = componentProperties.getValuedProperty(paramName);
        if (property != null) {
            obj = property.getValue();
        }
        return obj;
    }

    public static void setGenericPropertyValue(ComponentProperties componentProperties, String paramName, Object value) {
        if (componentProperties == null || paramName == null) {
            return;
        }
        ComponentProperties currentComponentProperties = getCurrentComponentProperties(componentProperties, paramName);
        if (currentComponentProperties == null) {
            return;
        }
        currentComponentProperties.setValue(getPropertyName(paramName), value);
    }

    /**
     * looks for all Property type properties of the given componenetProperties and all is nested CompoenetProperties.
     */
    public static List<Property> getAllValuedProperties(ComponentProperties componentProperties) {
        List<Property> allValuedProperties = new ArrayList<>();
        if (componentProperties == null) {
            return null;
        }
        List<NamedThing> namedThings = componentProperties.getProperties();
        for (NamedThing namedThing : namedThings) {
            if (namedThing instanceof ComponentProperties) {
                ComponentProperties childComponentProperties = (ComponentProperties) namedThing;
                allValuedProperties.addAll(getAllValuedProperties(childComponentProperties));
            } else if (namedThing instanceof Property) {
                allValuedProperties.add((Property) namedThing);
            } // else other type not yet handled
        }
        return allValuedProperties;
    }

    private static String getPropertyPath(String paramName) {
        String propertyPath = ""; //$NON-NLS-1$
        if (paramName.indexOf(IComponentConstants.EXP_SEPARATOR) != -1) {
            propertyPath = paramName.substring(0, paramName.lastIndexOf(IComponentConstants.EXP_SEPARATOR));
        }
        return propertyPath;
    }

    public static String getPropertyName(String paramName) {
        String propertyName = paramName;
        if (propertyName.indexOf(IComponentConstants.EXP_SEPARATOR) != -1) {
            propertyName = propertyName.substring(propertyName.lastIndexOf(IComponentConstants.EXP_SEPARATOR) + 1);
        }
        return propertyName;
    }

    public static boolean isSupportContext(SchemaElement schemaElement) {
        Type type = schemaElement.getType();
        switch (type) {
        case STRING:
        case INT:
        case DATE:
        case DATETIME:
        case DECIMAL:
        case FLOAT:
        case DOUBLE:
            return true;
        default:
            break;
        }
        return false;
    }

    public static boolean isSameComponentProperties(ComponentProperties componentProperties, NamedThing widgetProperty) {
        if (componentProperties != null && widgetProperty instanceof Form) {
            Form subForm = (Form) widgetProperty;
            if (subForm != null) {
                return componentProperties == subForm.getComponentProperties();
            }
        }
        return false;
    }
}
