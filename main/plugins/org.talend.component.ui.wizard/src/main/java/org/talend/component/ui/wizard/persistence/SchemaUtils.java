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

import java.util.ArrayList;
import java.util.List;

import org.talend.component.ui.model.genericMetadata.SubContainer;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.cwm.helper.PackageHelper;

/**
 * created by ycbai on 2015年10月28日 Detailled comment
 *
 */
public class SchemaUtils {

    public static List<MetadataTable> getMetadataTables(orgomg.cwm.objectmodel.core.Package parentPackage) {
        List<MetadataTable> metadataTables = new ArrayList<>();
        List<SubContainer> subContainers = PackageHelper.getOwnedElements(parentPackage, SubContainer.class);
        for (SubContainer subContainer : subContainers) {
            List<MetadataTable> tables = PackageHelper.getOwnedElements(subContainer, MetadataTable.class);
            if (tables.size() > 0) {
                metadataTables.addAll(tables);
            }
            metadataTables.addAll(getMetadataTables(subContainer));
        }
        return metadataTables;
    }

    public static MetadataTable getMetadataTable(Connection connection, String tabLabel) {
        List<MetadataTable> tables = getMetadataTables(connection);
        for (MetadataTable table : tables) {
            if (tabLabel != null && tabLabel.equals(table.getLabel())) {
                return table;
            }
        }
        return null;
    }

}
