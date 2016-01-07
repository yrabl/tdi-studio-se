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
package org.talend.designer.codegen.ui.token;

import java.util.List;

import org.talend.core.model.components.IComponent;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.ui.token.AbstractTokenCollector;
import org.talend.core.ui.token.TokenKey;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;

/**
 * ggu class global comment. Detailled comment
 * 
 * collect the user and exchange components
 */
public class UserComponentsTokenCollector extends AbstractTokenCollector {

    private static final TokenKey TOS_COUNT_USER_COMPONENTS = new TokenKey("tos.count.usercomponents"); //$NON-NLS-1$

    private static final TokenKey TOS_USER_COMPONENTS = new TokenKey("tos.user.components"); //$NON-NLS-1$

    /**
     * ggu UserComponentTokenCollector constructor comment.
     */
    public UserComponentsTokenCollector() {
        //
    }

    @Override
    protected void collectProperties(JSONObject propertiesObject) throws Exception {
        List<IComponent> customComponents = ComponentsFactoryProvider.getInstance().getCustomComponents();
        JSONArray customComponentsArray = new JSONArray();
        if (customComponents != null) {
            for (int i = 0; i < customComponents.size(); i++) {
                customComponentsArray.put(customComponents.get(i).getName());
                if (i > AbstractTokenCollector.TOP_USED_COMPONENTS_MAX) {
                    break;
                }
            }
        }
        propertiesObject.put(TOS_COUNT_USER_COMPONENTS.getKey(), customComponentsArray.length());
        propertiesObject.put(TOS_USER_COMPONENTS.getKey(), customComponentsArray);
    }

}
