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
package org.talend.designer.rowgenerator.ui.editor;

import java.util.List;

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.designer.rowgenerator.data.Function;
import org.talend.designer.rowgenerator.data.Parameter;

/**
 * qzhang class global comment. Detailled comment <br/>
 * 
 * $Id: MetadataColumnExt.java,v 1.7 2007/02/02 07:47:00 pub Exp $
 * 
 */
public class MetadataColumnExt extends MetadataColumn {

    private boolean isChanged;

    /**
     * qzhang MetadataColumnExt constructor comment.
     */
    public MetadataColumnExt() {
        super();
    }

    /**
     * qzhang MetadataColumnExt constructor comment.
     */
    public MetadataColumnExt(MetadataColumn metadataColumn) {
        super(metadataColumn);
    }

    private Function function;

    public Function getFunction() {
        return this.function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public String getParameter() {
        String currentPara = ""; //$NON-NLS-1$
        if (this.function != null) {
            for (Parameter para : (List<Parameter>) function.getParameters()) {
                currentPara += para.getName() + "=>" + para.getValue() + " ; "; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return currentPara;
    }

    private String[] arrayFunctions;

    public String[] getArrayFunctions() {
        return this.arrayFunctions;
    }

    public void setArrayFunctions(String[] arrayFunctions) {
        this.arrayFunctions = arrayFunctions;
    }

    /*
     * (non-Java)
     * 
     * @see org.talend.core.model.metadata.MetadataColumn#clone()
     */
    @Override
    public IMetadataColumn clone() {
        MetadataColumnExt ext = new MetadataColumnExt((MetadataColumn) super.clone());
        if (getFunction() != null) {
            ext.setFunction((Function) getFunction().clone());
            ext.setArrayFunctions(getArrayFunctions());
        }
        return ext;
    }

    private String preview;

    /**
     * Getter for preview.
     * 
     * @return the preview
     */
    public String getPreview() {
        return this.preview;
    }

    /**
     * Sets the preview.
     * 
     * @param preview the preview to set
     */
    public void setPreview(String preview) {
        this.preview = preview;
        function.setPreview(preview);
    }

    public boolean sameMetacolumnAs2(IMetadataColumn metaColumn) {
        boolean b = super.sameMetacolumnAs(metaColumn);
        if (metaColumn instanceof MetadataColumnExt) {
            final MetadataColumnExt another = (MetadataColumnExt) metaColumn;
            if (this.function == null) {
                if (another.function != null) {
                    return false;
                }
            } else if (!this.function.sameFunctionAs(another.function)) {
                return false;
            }
        }
        return b;

    }

    public boolean isChanged() {
        return this.isChanged;
    }

    public void setChanged(boolean isChanged) {
        this.isChanged = isChanged;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.metadata.MetadataColumn#setLabel(java.lang.String)
     */
    @Override
    public void setLabel(String label) {
        super.setLabel(label);
        setOriginalDbColumnName(label);
    }

}
