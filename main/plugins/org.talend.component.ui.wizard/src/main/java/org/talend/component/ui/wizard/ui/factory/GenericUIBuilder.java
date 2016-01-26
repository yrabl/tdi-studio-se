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
package org.talend.component.ui.wizard.ui.factory;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.components.api.NamedThing;
import org.talend.components.api.properties.PresentationItem;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Widget;
import org.talend.components.api.properties.presentation.Widget.WidgetType;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.schema.SchemaElement.Type;

/**
 * created by ycbai on 2015年9月18日 Detailled comment
 *
 */
public class GenericUIBuilder {

    private Composite parentComp;

    private int rowNum = 0;

    private int colNum = 0;

    public GenericUIBuilder(Composite parentComp) {
        this.parentComp = parentComp;
    }

    public Composite build(Form form) {
        List<Widget> widgets = form.getWidgets();
        for (Widget widget : widgets) {
            int row = widget.getRow();
            if (rowNum < row) {
                rowNum = row;
            }
            int col = widget.getOrder();
            if (colNum < col) {
                colNum = col;
            }
            createWidgetUI(parentComp, widget);
        }
        GridLayout layout = new GridLayout(colNum, false);
        layout.marginWidth = layout.marginHeight = 0;
        parentComp.setLayout(layout);
        return parentComp;
    }

    private void createWidgetUI(Composite parent, Widget widget) {
        if (!WidgetType.DEFAULT.equals(widget.getWidgetType())) {
            createWidgetUIByWidgetType(parent, widget);
        } else {
            createWidgetUIByPropertyType(parent, widget);
        }
    }

    private void createWidgetUIByWidgetType(Composite parent, Widget widget) {
        NamedThing property = getProperty(widget);
        if (property == null) {
            return;
        }
        String displayName = property.getDisplayName();
        WidgetType widgetType = widget.getWidgetType();
        if (widgetType == null) {
            return;
        }
        switch (widgetType) {
        case BUTTON:
            Button btn = new Button(parent, SWT.PUSH);
            btn.setText(displayName);
            break;
        default:
            break;
        }
    }

    private void createWidgetUIByPropertyType(Composite parent, Widget widget) {
        NamedThing property = getProperty(widget);
        if (property == null) {
            return;
        }
        int hSpan = widget.getOrder();
        if (property instanceof PresentationItem) {
            PresentationItem item = (PresentationItem) property;
            createLabel(parent, item.getDisplayName(), hSpan);
            return;
        } else if (property instanceof SchemaElement) {
            SchemaElement element = (SchemaElement) property;
            String displayName = element.getDisplayName();
            Type type = element.getType();
            if (type == null) {
                createLabel(parent, displayName, hSpan);
                return;
            }
            switch (type) {
            case STRING:
                LabelledText text = new LabelledText(parent, displayName, hSpan);
                break;
            case BOOLEAN:
                break;
            default:
                new LabelledText(parent, element.getDisplayName(), hSpan);
                break;
            }
        }
    }

    private Label createLabel(Composite parent, String displayName, int hSpan) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(StringUtils.trimToEmpty(displayName));
        GridData labelGridData = new GridData(GridData.FILL_HORIZONTAL);
        labelGridData.horizontalSpan = hSpan;
        label.setLayoutData(labelGridData);
        return label;
    }

    private NamedThing getProperty(Widget widget) {
        NamedThing[] properties = widget.getProperties();
        NamedThing property = null;
        if (properties != null && properties.length > 0) {
            property = properties[0];
        }
        return property;
    }

}
