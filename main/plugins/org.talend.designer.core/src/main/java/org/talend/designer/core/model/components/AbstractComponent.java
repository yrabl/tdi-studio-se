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
package org.talend.designer.core.model.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.temp.ECodePart;
import org.talend.designer.core.i18n.Messages;

/**
 * ggu class global comment. Detailled comment
 */
public abstract class AbstractComponent implements IComponent {

    protected static final String EQUALS = "=="; //$NON-NLS-1$

    protected static final String DEFAULT_COLOR = "255;255;255"; //$NON-NLS-1$

    protected static final String BUILTIN = "BUILT_IN"; //$NON-NLS-1$

    protected static final String REPOSITORY = "REPOSITORY"; //$NON-NLS-1$

    protected static final String TNS_FILE = "USE_TNS_FILE"; //$NON-NLS-1$

    protected static final String TEXT_BUILTIN = Messages.getString("EmfComponent.builtIn"); //$NON-NLS-1$

    protected static final String TEXT_REPOSITORY = Messages.getString("EmfComponent.repository"); //$NON-NLS-1$

    protected static final String TEXT_TNS_FILE = Messages.getString("EmfComponent.tnsfile"); //$NON-NLS-1$

    protected static final String TSTATCATCHER_NAME = "tStatCatcher"; //$NON-NLS-1$

    protected static final String ENCODING_TYPE_UTF_8 = "UTF-8"; //$NON-NLS-1$

    protected static final String ENCODING_TYPE_ISO_8859_15 = "ISO-8859-15"; //$NON-NLS-1$

    protected static final String ENCODING_TYPE_CUSTOM = "CUSTOM"; //$NON-NLS-1$

    protected static final String STRING_TYPE = "String"; //$NON-NLS-1$

    protected static final boolean ADVANCED_PROPERTY = true;

    protected static final boolean NORMAL_PROPERTY = false;

    protected List<ECodePart> codePartListX;

    protected Map<String, ImageDescriptor> imageRegistry = new HashMap<String, ImageDescriptor>();

    private String paletteType;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.getName() == null) ? 0 : this.getName().hashCode())
                + ((this.getPaletteType() == null) ? 0 : this.getPaletteType().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof IComponent)) {
            return false;
        }
        final IComponent other = (IComponent) obj;
        String thisName = this.getName();
        String otherName = other.getName();
        if (thisName == null) {
            if (otherName != null) {
                return false;
            }
        } else if (!thisName.equals(otherName)) {
            return false;
        } else if (paletteType == null) {
            if (other.getPaletteType() != null) {
                return false;
            }
        } else if (!paletteType.equals(other.getPaletteType())) {
            return false;
        }
        return true;
    }

    @Override
    public String getPaletteType() {
        return paletteType;
    }

    @Override
    public void setPaletteType(String paletteType) {
        this.paletteType = paletteType;
    }

    @Override
    public String getRepositoryType() {
        return null;
    }

    /**
     * This is just added in <code>HEADER</code>, it is used to present the component belongs to common process, M/R
     * process and etc. The {@link ComponentCategory#CATEGORY_4_DI} is as the default value. Added by Marvin Wang on Jan
     * 11, 2013.
     * 
     */
    @Override
    public String getType() {
        return ComponentCategory.CATEGORY_4_DI.getName();
    }

    @Override
    public boolean isReduce() {
        return false;
    }

    @Override
    public boolean isSparkAction() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getInputType()
     */
    @Override
    public String getInputType() {
        return "AUTO";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getOutputType()
     */
    @Override
    public String getOutputType() {
        return "AUTO";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getPartitioning()
     */
    @Override
    public String getPartitioning() {
        return "AUTO"; //$NON-NLS-1$
    }

    @Override
    public boolean isSupportDbType() {
        return false;
    }

    @Override
    public Map<String, ImageDescriptor> getImageRegistry() {
        return null;
    }

    @Override
    public boolean isLog4JEnabled() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getCONNECTORList()
     */
    @Override
    public EList getCONNECTORList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAllowedPropagated() {
        return true;
    }

    public void initNodePropertiesFromSerialized(INode node, String serialized) {

    }

    public void initParamPropertiesFromSerialized(IElementParameter param, String serialized) {
        // nothing by default
    }

    public Object getElementParameterValue(IElementParameter param) {
        return null;
    }

    public Object genericFromSerialized(String serialized, String name) {
        return null;
    }

    public String genericToSerialized(IElementParameter param) {
        return null;
    }

    public Object getElementParameterValueFromComponentProperties(INode iNode, IElementParameter param) {
        return null;
    }
}
