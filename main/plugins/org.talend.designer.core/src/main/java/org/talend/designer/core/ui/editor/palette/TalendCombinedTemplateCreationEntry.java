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
package org.talend.designer.core.ui.editor.palette;

import java.util.Date;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TalendCombinedTemplateCreationEntry extends CombinedTemplateCreationEntry {

    protected Date timestemp;

    /**
     * DOC cmeng TalendCombinedTemplateCreationEntry constructor comment.
     * 
     * @param label
     * @param shortDesc
     * @param template
     * @param factory
     * @param iconSmall
     * @param iconLarge
     */
    public TalendCombinedTemplateCreationEntry(String label, String shortDesc, Object template, CreationFactory factory,
            ImageDescriptor iconSmall, ImageDescriptor iconLarge) {
        super(label, shortDesc, template, factory, iconSmall, iconLarge);
    }

    public Date getTimestemp() {
        return this.timestemp;
    }

    public void setTimestemp(Date timestemp) {
        this.timestemp = timestemp;
    }

}
