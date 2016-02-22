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
package org.talend.component.core.context;

import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.PropertyValueEvaluator;

/**
 * created by ycbai on 2016年2月6日 Detailled comment
 *
 */
public class ComponentContextPropertyValueEvaluator implements PropertyValueEvaluator {

    private INode node;

    public ComponentContextPropertyValueEvaluator(INode node) {
        this.node = node;
    }

    @Override
    public Object evaluate(Property property, Object storedValue) {
        if (storedValue == null) {
            return storedValue;
        }
        IContext context = null;
        if (node != null) {
            IProcess process = node.getProcess();
            if (process != null) {
                IContextManager cm = process.getContextManager();
                if (cm != null) {
                    context = cm.getDefaultContext();
                }
            }
        }
        if (context != null) {
            return ContextParameterUtils.parseScriptContextCode(String.valueOf(storedValue), context);
        }
        return storedValue;
    }

}
