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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.component.core.model.GenericElementParameter;
import org.talend.components.api.NamedThing;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;

/**
 * 
 * created by ycbai on 2015年10月21日 Detailled comment
 *
 */
public class ComponentRefController extends AbstractElementPropertySectionController {

    public ComponentRefController(IDynamicProperty dp) {
        super(dp);
    }

    public Command createComboCommand(SelectionEvent event) {
        Set<String> elementsName;
        Control ctrl;
        elementsName = hashCurControls.keySet();
        for (String name : elementsName) {
            Object o = hashCurControls.get(name);
            if (o instanceof Control) {
                ctrl = (Control) o;
                CCombo combo = (CCombo) event.getSource();
                Object data = ctrl.getData(PARAMETER_NAME);
                if (!(ctrl instanceof CCombo)) {
                    continue;
                }
                boolean isDisposed = ((CCombo) ctrl).isDisposed() || combo.isDisposed();
                if (isDisposed) {
                    continue;
                }
                if (data != null && data.equals(combo.getData(PARAMETER_NAME))) {
                    if (!((CCombo) ctrl).getText().equals(elem.getPropertyValue(name))) {
                        String value = ""; //$NON-NLS-1$
                        List<? extends IElementParameter> params = elem.getElementParametersWithChildrens();
                        for (int i = 0; i < params.size(); i++) {
                            IElementParameter param = params.get(i);
                            if (param.getName().equals(name)) {
                                for (int j = 0; j < param.getListItemsValue().length; j++) {
                                    if (j < param.getListItemsDisplayName().length
                                            && ((CCombo) ctrl).getText().equals(param.getListItemsDisplayName()[j])) {
                                        value = (String) param.getListItemsValue()[j];
                                    }
                                }
                            }
                        }
                        if (StringUtils.isBlank(value)) {
                            value = ((INode) elem).getUniqueName();
                        }
                        if (value.equals(elem.getPropertyValue(name))) { // same value so no need to do anything
                            return null;
                        }
                        return new PropertyChangeCommand(elem, name, value);
                    }
                }
            }
        }
        return null;
    }

    IControlCreator cbCtrl = new IControlCreator() {

        @Override
        public Control createControl(final Composite parent, final int style) {
            CCombo cb = new CCombo(parent, style);
            return cb;
        }
    };

    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {

        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, cbCtrl);
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        if (param.isRepositoryValueUsed()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
            decoration.setDescription(Messages.getString("ComboController.valueFromRepository")); //$NON-NLS-1$
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.BOTTOM, false);
        }

        Control cLayout = dField.getLayoutControl();
        CCombo combo = (CCombo) dField.getControl();
        FormData data;
        combo.setEditable(false);
        cLayout.setBackground(subComposite.getBackground());
        combo.setEnabled(!param.isReadOnly());
        combo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                Command cmd = createCommand(event);
                executeCommand(cmd);
            }
        });
        combo.setData(PARAMETER_NAME, param.getName());
        int nbLines = param.getNbLines();
        if (nbLines > 5) {
            combo.setVisibleItemCount(nbLines);
        }
        if (elem instanceof Node) {
            combo.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }

        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // *********************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        // **********************
        hashCurControls.put(param.getName(), combo);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return cLayout;
    }

    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, cbCtrl);
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();

        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub
    }

    private Command createCommand(SelectionEvent event) {
        Command cmd = null;
        if (event.getSource() instanceof CCombo) {
            cmd = createComboCommand(event);
        }
        return cmd;
    }

    @Override
    public void refresh(IElementParameter param, boolean check) {
        String paramName = param.getName();
        CCombo combo = (CCombo) hashCurControls.get(paramName);
        if (combo == null || combo.isDisposed()) {
            return;
        }
        INode currentNode = (INode) elem;
        Object value = param.getValue();
        List<INode> refNodes = getRefNodes(param);
        List<String> itemsLabel = new ArrayList<>();
        List<String> itemsValue = new ArrayList<>();
        for (INode node : refNodes) {
            final String uniqueName = node.getUniqueName();
            if (uniqueName.equals(currentNode.getUniqueName())) {
                continue;
            }
            String displayName = (String) node.getElementParameter(EParameterName.LABEL.getName()).getValue();
            if (displayName == null) {
                displayName = uniqueName + " - " + displayName; //$NON-NLS-1$
            }
            if (displayName.indexOf("__UNIQUE_NAME__") != -1) { //$NON-NLS-1$
                displayName = displayName.replaceAll("__UNIQUE_NAME__", uniqueName); //$NON-NLS-1$
            } else {
                displayName = uniqueName + " - " + displayName; //$NON-NLS-1$
            }
            itemsLabel.add(displayName);
            itemsValue.add(uniqueName);
        }
        if (param instanceof GenericElementParameter) {
            GenericElementParameter gParam = (GenericElementParameter) param;
            NamedThing[] properties = gParam.getWidget().getProperties();
            if (properties != null && properties.length > 0) {
                NamedThing nt = properties[0];
                itemsLabel.add(nt.getDisplayName());
                itemsValue.add(currentNode.getUniqueName());
            }
        }
        param.setListItemsDisplayName(itemsLabel.toArray(new String[0]));
        param.setListItemsDisplayCodeName(itemsLabel.toArray(new String[0]));
        param.setListItemsValue(itemsValue.toArray(new String[0]));
        combo.setItems(itemsLabel.toArray(new String[0]));
        String iLabel = null;
        for (int i = 0; i < itemsValue.size(); i++) {
            String iValue = itemsValue.get(i);
            if ((value == null && iValue == null) || (value != null && value.equals(iValue))) {
                iLabel = itemsLabel.get(i);
                break;
            }
        }
        if (StringUtils.isNotEmpty(iLabel)) {
            combo.setText(iLabel);
        }
    }

    private List<INode> getRefNodes(IElementParameter param) {
        List<INode> refNodes = new ArrayList<>();
        if (param instanceof GenericElementParameter) {
            GenericElementParameter gParameter = (GenericElementParameter) param;
            refNodes = (List<INode>) part.getProcess().getNodesOfType(gParameter.getWidget().getReferencedComponentName());
        }
        return refNodes;
    }
}
