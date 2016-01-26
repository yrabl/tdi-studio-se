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
package org.talend.designer.dbmap.service;

import org.talend.core.model.process.IExternalNode;
import org.talend.core.service.IDbMapService;
import org.talend.designer.dbmap.DbMapComponent;

/**
 * DOC talend class global comment. Detailled comment
 */
public class DbMapService implements IDbMapService {

    @Override
    public boolean isDbMapComponent(IExternalNode node) {
        if (node instanceof DbMapComponent) {
            return true;
        }
        return false;
    }

}
