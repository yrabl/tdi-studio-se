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

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.components.api.properties.NameAndLabel;
import org.talend.core.ui.composite.ElementsSelectionComposite;

/**
 * created by ycbai on 2015年10月12日 Detailled comment
 *
 */
public class NameAndLabelsDialog extends Dialog {

    private List<NameAndLabel> nameAndLabels;

    private String result;

    private ElementsSelectionComposite<NameAndLabel> selectionComposite;

    public NameAndLabelsDialog(Shell parentShell, List<NameAndLabel> nameAndLabels) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MIN | SWT.APPLICATION_MODAL);
        this.nameAndLabels = nameAndLabels;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setSize(450, 550);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        Composite comp = new Composite(composite, SWT.NONE);
        comp.setLayoutData(new GridData(GridData.FILL_BOTH));
        comp.setLayout(new GridLayout());

        selectionComposite = new ElementsSelectionComposite<NameAndLabel>(comp, false) {

            @Override
            protected IBaseLabelProvider getLabelProvider() {
                return new LabelProvider() {

                    @Override
                    public String getText(Object obj) {
                        NameAndLabel nal = (NameAndLabel) obj;
                        return nal.getLabel();
                    }

                    @Override
                    public Image getImage(Object obj) {
                        return null;
                    }
                };
            };
        };
        selectionComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        selectionComposite.setViewerData(nameAndLabels);

        return composite;
    }

    @Override
    protected void initializeBounds() {
        super.initializeBounds();
        Point size = getShell().getSize();
        Point location = getInitialLocation(size);
        getShell().setBounds(getConstrainedShellBounds(new Rectangle(location.x, location.y, size.x, size.y)));
    }

    @Override
    protected void okPressed() {
        result = getSelectedName();
        super.okPressed();
    }

    private String getSelectedName() {
        List<NameAndLabel> selectedElements = selectionComposite.getSelectedElements();
        if (selectedElements.size() > 0) {
            return selectedElements.get(0).getName();
        }
        return ""; //$NON-NLS-1$
    }

    public String getResult() {
        return this.result;
    }

}
