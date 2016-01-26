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
package org.talend.component.ui.wizard.persistence;

import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.component.core.constants.IGenericConstants;
import org.talend.component.core.utils.SchemaUtils;
import org.talend.component.ui.model.genericMetadata.GenericConnection;
import org.talend.component.ui.model.genericMetadata.GenericConnectionItem;
import org.talend.component.ui.model.genericMetadata.GenericMetadataFactory;
import org.talend.component.ui.model.genericMetadata.SubContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.Repository;
import org.talend.components.api.schema.Schema;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.cwm.helper.PackageHelper;

/**
 * created by ycbai on 2015年9月29日 Detailled comment
 *
 */
public class GenericRepository implements Repository {

    @Override
    public String storeComponentProperties(ComponentProperties properties, String name, String repositoryLocation, Schema schema) {
        String serializedProperties = properties.toSerialized();
        if (repositoryLocation.contains(IGenericConstants.REPOSITORY_LOCATION_SEPARATOR)) {// nested properties to be
            GenericConnectionItem item = getGenericConnectionItem(repositoryLocation.substring(0,
                    repositoryLocation.indexOf(IGenericConstants.REPOSITORY_LOCATION_SEPARATOR)));
            if (item == null) {
                throw new RuntimeException("Failed to find the GenericConnectionItem for location:" + repositoryLocation); //$NON-NLS-1$
            }
            GenericConnection connection = (GenericConnection) item.getConnection();
            SubContainer subContainer = createContainer(name, serializedProperties);
            if (repositoryLocation.endsWith(IGenericConstants.REPOSITORY_LOCATION_SEPARATOR)) {// first nested property
                if (item != null) {
                    connection.getOwnedElement().add(subContainer);
                }
            } else {
                SubContainer parentContainer = getContainer(connection, repositoryLocation);
                parentContainer.getOwnedElement().add(subContainer);
            }
            // if there is a schema then creates a Schema element
            if (schema != null) {
                MetadataTable metadataTable = SchemaUtils.createSchema(name, serializedProperties);
                subContainer.getOwnedElement().add(metadataTable);
                SchemaUtils.convertComponentSchemaIntoTalendSchema(schema, metadataTable);
            }
            return repositoryLocation + IGenericConstants.REPOSITORY_LOCATION_SEPARATOR + name;
        } else {// simple properties to be set
            GenericConnectionItem item = getGenericConnectionItem(repositoryLocation);
            if (item != null) {
                GenericConnection connection = (GenericConnection) item.getConnection();
                connection.setCompProperties(serializedProperties);
                connection.getOwnedElement().clear();
                return repositoryLocation + IGenericConstants.REPOSITORY_LOCATION_SEPARATOR;
            } else {
                throw new RuntimeException("Failed to find the GenericConnectionItem for location:" + repositoryLocation); //$NON-NLS-1$
            }
        }
    }

    private SubContainer createContainer(String containerName, String serializedProperties) {
        SubContainer subContainer = GenericMetadataFactory.eINSTANCE.createSubContainer();
        subContainer.setName(containerName);
        subContainer.setCompProperties(serializedProperties);
        return subContainer;
    }

    private SubContainer getContainer(GenericConnection connection, String repositoryLocation) {
        SubContainer theContainer = null;
        String containers = repositoryLocation;
        if (containers.indexOf(IGenericConstants.REPOSITORY_LOCATION_SEPARATOR) != -1) {
            containers = containers.substring(repositoryLocation.indexOf(IGenericConstants.REPOSITORY_LOCATION_SEPARATOR) + 1);
            String[] containersArray = containers.split(IGenericConstants.REPOSITORY_LOCATION_SEPARATOR);
            for (String container : containersArray) {
                if (theContainer == null) {
                    theContainer = getTheContainer(connection, container);
                } else {
                    theContainer = getTheContainer(theContainer, container);
                    if (theContainer == null) {
                        throw new RuntimeException("Failed to find the SubContainer named:" + container); //$NON-NLS-1$
                    }
                }
            }
        }
        return theContainer;
    }

    private SubContainer getTheContainer(orgomg.cwm.objectmodel.core.Package parentPackage, String containerName) {
        List<SubContainer> subContainers = PackageHelper.getOwnedElements(parentPackage, SubContainer.class);
        for (SubContainer subContainer : subContainers) {
            if (containerName != null && containerName.equals(subContainer.getName())) {
                return subContainer;
            }
        }
        return null;
    }

    /**
     * DOC sgandon Comment method "getGenericConnectionItem".
     * 
     * @param repositoryLocation
     * @return
     */
    private GenericConnectionItem getGenericConnectionItem(String repositoryLocation) {
        GenericConnectionItem genItem = null;
        try {
            IRepositoryViewObject repViewObj = ProxyRepositoryFactory.getInstance().getLastVersion(repositoryLocation);
            if (repViewObj != null) {
                Property property = repViewObj.getProperty();
                if (property != null) {
                    Item item = property.getItem();
                    if (item instanceof GenericConnectionItem) {
                        genItem = (GenericConnectionItem) item;
                    }
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return genItem;
    }

    @Override
    public ComponentProperties getPropertiesForComponent(String componentId) {
        return null;// FIXME
    }

}
