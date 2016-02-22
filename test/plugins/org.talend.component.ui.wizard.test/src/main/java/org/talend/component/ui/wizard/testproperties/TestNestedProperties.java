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
package org.talend.component.ui.wizard.testproperties;

import static org.talend.daikon.properties.PropertyFactory.*;
import static org.talend.daikon.properties.presentation.Widget.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget.WidgetType;

/**
 * created by ycbai on 2016年2月17日 Detailled comment
 *
 */
public class TestNestedProperties extends ComponentProperties {

    public Property userName = (Property) newProperty("userName").setRequired(true); //$NON-NLS-1$

    public Property userPassword = (Property) newProperty("userPassword").setRequired(true); //$NON-NLS-1$

    public TestNestedProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN, "Nested Component"); //$NON-NLS-1$
        form.addRow(userName);
        form.addRow(widget(userPassword).setWidgetType(WidgetType.HIDDEN_TEXT));
    }

}