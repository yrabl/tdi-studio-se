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
package org.talend.component.ui.wizard.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.component.core.utils.ComponentsUtils;
import org.talend.component.ui.model.genericMetadata.GenericConnection;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.ui.context.model.table.ConectionAdaptContextVariableModel;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties.Deserialized;
import org.talend.daikon.properties.Property;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.metadata.managment.ui.model.IConnParamName;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.metadata.managment.ui.utils.GenericConnParamName;

/**
 * created by ycbai on 2015年11月27日 Detailled comment
 *
 */
public class GenericContextUtil {

    public static List<IContextParameter> createContextParameters(String prefixName, Connection connection,
            Set<IConnParamName> paramSet) {
        List<IContextParameter> varList = new ArrayList<>();
        if (connection instanceof GenericConnection) {
            GenericConnection conn = (GenericConnection) connection;
            if (conn == null || prefixName == null || paramSet == null || paramSet.isEmpty()) {
                return Collections.emptyList();
            }
            String paramPrefix = prefixName + ConnectionContextHelper.LINE;
            String paramName = null;
            for (IConnParamName param : paramSet) {
                if (param instanceof GenericConnParamName) {
                    GenericConnParamName connParamName = (GenericConnParamName) param;
                    String name = connParamName.getName();
                    ComponentProperties componentProperties = getComponentProperties((GenericConnection) connection);
                    Object paramValue = ComponentsUtils.getGenericPropertyValue(componentProperties, name);
                    paramName = paramPrefix + connParamName.getContextVar();
                    ConnectionContextHelper.createParameters(varList, paramName, String.valueOf(paramValue));
                }
            }
        }
        return varList;
    }

    private static ComponentProperties getComponentProperties(GenericConnection connection) {
        String compPropertiesStr = connection.getCompProperties();
        if (compPropertiesStr != null) {
            Deserialized<ComponentProperties> fromSerialized = ComponentProperties.fromSerialized(compPropertiesStr,
                    ComponentProperties.class);
            if (fromSerialized != null) {
                return fromSerialized.properties;
            }
        }
        return null;
    }

    public static void setPropertiesForContextMode(String prefixName, Connection connection, Set<IConnParamName> paramSet) {
        if (connection == null) {
            return;
        }
        if (connection instanceof GenericConnection) {
            GenericConnection genericConn = (GenericConnection) connection;
            ComponentProperties componentProperties = getComponentProperties(genericConn);
            String originalVariableName = prefixName + ConnectionContextHelper.LINE;
            String genericVariableName = null;
            for (IConnParamName param : paramSet) {
                if (param instanceof GenericConnParamName) {
                    GenericConnParamName genericParam = (GenericConnParamName) param;
                    String paramVarName = genericParam.getContextVar();
                    genericVariableName = originalVariableName + paramVarName;
                    matchContextForAttribues(componentProperties, genericParam, genericVariableName);
                }
            }
            updateComponentProperties(genericConn, componentProperties);
        }
    }

    public static void setPropertiesForExistContextMode(Connection connection, Set<IConnParamName> paramSet,
            Map<ContextItem, List<ConectionAdaptContextVariableModel>> adaptMap) {
        if (connection == null) {
            return;
        }
        if (connection instanceof GenericConnection) {
            GenericConnection genericConn = (GenericConnection) connection;
            ComponentProperties componentProperties = getComponentProperties(genericConn);
            ContextItem currentContext = null;
            for (IConnParamName param : paramSet) {
                if (param instanceof GenericConnParamName) {
                    String genericVariableName = null;
                    GenericConnParamName genericParam = (GenericConnParamName) param;
                    if (adaptMap != null && adaptMap.size() > 0) {
                        for (Map.Entry<ContextItem, List<ConectionAdaptContextVariableModel>> entry : adaptMap.entrySet()) {
                            currentContext = entry.getKey();
                            List<ConectionAdaptContextVariableModel> modelList = entry.getValue();
                            for (ConectionAdaptContextVariableModel model : modelList) {
                                if (model.getValue().equals(genericParam.getName())) {
                                    genericVariableName = model.getName();
                                    break;
                                }
                            }
                        }
                    }
                    if (genericVariableName != null) {
                        genericVariableName = getCorrectVariableName(currentContext, genericVariableName, genericParam);
                        matchContextForAttribues(componentProperties, genericParam, genericVariableName);
                    }
                }

            }
            updateComponentProperties(genericConn, componentProperties);
        }
    }

    private static String getCorrectVariableName(ContextItem contextItem, String originalVariableName,
            GenericConnParamName genericParam) {
        Set<String> contextVarNames = ContextUtils.getContextVarNames(contextItem);
        if (contextVarNames != null && !contextVarNames.contains(originalVariableName)) {
            for (String varName : contextVarNames) {
                if (varName.endsWith(genericParam.getName())) {
                    return varName;
                }
            }
        }
        return originalVariableName;
    }

    private static void matchContextForAttribues(ComponentProperties componentProperties, IConnParamName param,
            String genericVariableName) {
        GenericConnParamName genericParam = (GenericConnParamName) param;
        String paramName = genericParam.getName();
        String paramValue = ContextParameterUtils.getNewScriptCode(genericVariableName, ECodeLanguage.JAVA);
        ComponentsUtils.setGenericPropertyValue(componentProperties, paramName, paramValue);
    }

    private static void updateComponentProperties(GenericConnection conn, ComponentProperties componentProperties) {
        String serializedProperties = componentProperties.toSerialized();
        conn.setCompProperties(serializedProperties);
    }

    public static void revertPropertiesForContextMode(Connection connection, ContextType contextType) {
        if (connection instanceof GenericConnection) {
            GenericConnection conn = (GenericConnection) connection;
            ComponentProperties componentProperties = getComponentProperties(conn);
            revertPropertiesValues(componentProperties, contextType);
            updateComponentProperties(conn, componentProperties);
        }
    }

    public static void revertPropertiesValues(ComponentProperties componentProperties, ContextType contextType) {
        List<NamedThing> props = componentProperties.getProperties();
        for (NamedThing namedThing : props) {
            if (namedThing instanceof ComponentProperties) {
                revertPropertiesValues((ComponentProperties) namedThing, contextType);
            } else if (namedThing instanceof Property) {
                Property property = (Property) namedThing;
                if (ComponentsUtils.isSupportContext(property)) {
                    String value = property.getStringValue();
                    if (value != null) {
                        String originalValue = ContextParameterUtils.getOriginalValue(contextType, value);
                        property.setValue(TalendQuoteUtils.removeQuotes(originalValue));
                    }
                }
            }
        }
    }

}
