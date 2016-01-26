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
package org.talend.component.ui.wizard.controller;

import java.beans.PropertyChangeEvent;

import org.eclipse.gef.commands.Command;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;

/**
 * 
 * created by ycbai on 2015年9月28日 Detailled comment
 *
 */
public class ButtonController extends AbstractElementPropertySectionController {

    public ButtonController(IDynamicProperty dp) {
        super(dp);
    }

    public Command createCommand(Button button) {
        IElementParameter parameter = (IElementParameter) button.getData();
        if (parameter != null) {
            parameter.setValue(null); // so as to invoke listeners to perform some actions.
        }
        return null;
    }

    @Override
    public Control createControl(Composite subComposite, IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        Button theBtn = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        if (param.getDisplayName().equals("")) { //$NON-NLS-1$
            theBtn.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));
        } else {
            theBtn.setText(param.getDisplayName());
        }
        FormData data = new FormData();
        if (lastControl != null) {
            reLayoutOtherControls(subComposite, numInRow, theBtn);
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.right = new FormAttachment(100, -10);
        }
        data.top = new FormAttachment(0, top);
        theBtn.setLayoutData(data);
        theBtn.setEnabled(!param.isReadOnly());
        theBtn.setData(param);
        theBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Command cmd = createCommand((Button) e.getSource());
                executeCommand(cmd);
            }
        });
        return theBtn;
    }

    private void reLayoutOtherControls(Composite parentComposite, int numInRow, Button currentBtn) {
        Control[] children = parentComposite.getChildren();
        if (children.length > 0) {
            int firstControlIndex = children.length - numInRow;
            if (firstControlIndex >= 0) {
                Control control = children[firstControlIndex];
                Object layoutData = control.getLayoutData();
                if (layoutData instanceof FormData) {
                    FormData controlData = (FormData) control.getLayoutData();
                    FormAttachment rightFormAttachment = controlData.right;
                    GC gc = new GC(parentComposite);
                    Point buttonSize = gc.stringExtent(currentBtn.getText());
                    int currentBtnWidth = buttonSize.x + 18;
                    gc.dispose();
                    rightFormAttachment.offset -= currentBtnWidth;
                }
            }
        }
    }

    @Override
    public void refresh(IElementParameter param, boolean check) {
        // TODO Auto-generated method stub
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub
    }

    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        return 0;
    }

}
