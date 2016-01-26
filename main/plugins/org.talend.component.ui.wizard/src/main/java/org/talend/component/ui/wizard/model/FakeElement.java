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
package org.talend.component.ui.wizard.model;

import org.talend.core.model.process.Element;

/**
 * created by ycbai on 2015年9月23日 Detailled comment
 *
 */
public class FakeElement extends Element {

    private String name;

    public FakeElement(String name) {
        super();
        this.name = name;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        return;
    }

    @Override
    public String getElementName() {
        return name;
    }

}
