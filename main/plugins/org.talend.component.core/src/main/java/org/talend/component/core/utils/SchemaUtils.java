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

import java.util.List;
import java.util.Map;

import org.talend.component.core.constants.IGenericConstants;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.daikon.properties.Properties.Deserialized;
import org.talend.daikon.schema.Schema;
import org.talend.daikon.schema.SchemaElement;
import org.talend.daikon.schema.SchemaElement.Type;
import org.talend.repository.model.IProxyRepositoryFactory;

import orgomg.cwm.objectmodel.core.CoreFactory;
import orgomg.cwm.objectmodel.core.TaggedValue;

/**
 * 
 * created by ycbai on 2015年12月2日 Detailled comment
 *
 */
public class SchemaUtils {

    public static MetadataTable createSchema(String name, String serializedProperties) {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        MetadataTable metadataTable = ConnectionFactory.eINSTANCE.createMetadataTable();
        metadataTable.setId(factory.getNextId());
        metadataTable.setName(name);
        metadataTable.setLabel(name);
        metadataTable.setSourceName(name);
        TaggedValue serializedProps = CoreFactory.eINSTANCE.createTaggedValue();
        metadataTable.getTaggedValue().add(serializedProps);
        serializedProps.setTag(IGenericConstants.COMPONENT_PROPERTIES_TAG);
        serializedProps.setValue(serializedProperties);
        return metadataTable;
    }

    public static void convertComponentSchemaIntoTalendSchema(Schema schema, MetadataTable metadataTable) {
        SchemaElement root = schema.getRoot();
        if (root != null) {
            List<SchemaElement> schemaElements = root.getChildren();
            for (SchemaElement schemaElement : schemaElements) {
                MetadataColumn metadataColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
                setupMetadataColumn(metadataColumn, schemaElement);
                metadataTable.getColumns().add(metadataColumn);
            }
        }
    }

    private static void setupMetadataColumn(MetadataColumn metadataColumn, SchemaElement schemaElement) {
        String talendType = JavaTypesManager.STRING.getId();
        Type type = schemaElement.getType();
        switch (type) {
        case BOOLEAN:
            talendType = JavaTypesManager.BOOLEAN.getId();
            break;
        case INT:
            talendType = JavaTypesManager.INTEGER.getId();
            break;
        case DATE:
            talendType = JavaTypesManager.DATE.getId();
            break;
        case DATETIME:
            talendType = JavaTypesManager.DATE.getId();
            break;
        case DOUBLE:
            talendType = JavaTypesManager.DOUBLE.getId();
            break;
        case DECIMAL:
            talendType = JavaTypesManager.BIGDECIMAL.getId();
            break;
        default:
            talendType = JavaTypesManager.STRING.getId();
            break;
        }
        metadataColumn.setTalendType(talendType);
        metadataColumn.setName(schemaElement.getName());
        metadataColumn.setLabel(metadataColumn.getName());
        metadataColumn.setPattern(schemaElement.getPattern());
        metadataColumn.setNullable(schemaElement.isNullable());
        metadataColumn.setLength(schemaElement.getSize());
        metadataColumn.setPrecision(schemaElement.getPrecision());
        metadataColumn.setDefaultValue(schemaElement.getDefaultValue());
    }

    public static ComponentProperties getCurrentComponentProperties(IMetadataTable table) {
        if (table != null) {
            String serializedProperties = null;
            if (table instanceof MetadataTableRepositoryObject) {
                MetadataTableRepositoryObject metaTableRepObj = (MetadataTableRepositoryObject) table;
                MetadataTable metadataTable = metaTableRepObj.getTable();
                if (metadataTable != null && metadataTable.getTaggedValue() != null) {
                    for (TaggedValue serializedProps : metadataTable.getTaggedValue()) {
                        if (IGenericConstants.COMPONENT_PROPERTIES_TAG.equals(serializedProps.getTag())) {
                            serializedProperties = serializedProps.getValue();
                            break;
                        }
                    }
                }
            } else if (table instanceof org.talend.core.model.metadata.MetadataTable) {
                org.talend.core.model.metadata.MetadataTable metaTable = (org.talend.core.model.metadata.MetadataTable) table;
                Map<String, String> additionalProperties = metaTable.getAdditionalProperties();
                serializedProperties = additionalProperties.get(IGenericConstants.COMPONENT_PROPERTIES_TAG);
            }
            if (serializedProperties != null) {
                Deserialized<ComponentProperties> fromSerializedProperties = ComponentProperties
                        .fromSerialized(serializedProperties, ComponentProperties.class);
                if (fromSerializedProperties != null) {
                    return fromSerializedProperties.properties;
                }
            }
        }
        return null;
    }
}
