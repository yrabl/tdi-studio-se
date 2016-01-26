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
package org.talend.component.ui.wizard.ui;

import org.eclipse.core.runtime.IPath;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.component.core.constants.IGenericConstants;
import org.talend.component.ui.model.genericMetadata.GenericConnection;
import org.talend.component.ui.model.genericMetadata.GenericConnectionItem;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentProperties.Deserialized;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.metadata.managment.ui.wizard.PropertiesWizard;

/**
 * created by ycbai on 2015年11月3日 Detailled comment
 *
 */
public class GenericConnPropertiesWizard extends PropertiesWizard {

    public GenericConnPropertiesWizard(IRepositoryViewObject repositoryViewObject, IPath path, boolean useLastVersion) {
        super(repositoryViewObject, path, useLastVersion);
    }

    @Override
    public boolean performFinish() {
        boolean done = super.performFinish();
        if (done) {
            Property property = object.getProperty();
            GenericConnectionItem gcItem = (GenericConnectionItem) property.getItem();
            GenericConnection connection = (GenericConnection) gcItem.getConnection();
            String compPropertiesStr = connection.getCompProperties();
            if (compPropertiesStr != null) {
                Deserialized fromSerialized = ComponentProperties.fromSerialized(compPropertiesStr);
                if (fromSerialized != null) {
                    ComponentProperties componentProperties = fromSerialized.properties;
                    org.talend.components.api.properties.Property nameProperty = (org.talend.components.api.properties.Property) componentProperties
                            .getProperty(IGenericConstants.NAME_PROPERTY);
                    Object namePropertyVal = nameProperty.getValue();
                    String newName = property.getLabel();
                    if (newName != null && !newName.equals(namePropertyVal)) {
                        nameProperty.setValue(newName);
                        connection.setCompProperties(componentProperties.toSerialized());
                        try {
                            ProxyRepositoryFactory.getInstance().save(gcItem);
                        } catch (PersistenceException e) {
                            done = false;
                            ExceptionHandler.process(e);
                        }
                    }
                }
            }
        }
        return done;
    }

}
