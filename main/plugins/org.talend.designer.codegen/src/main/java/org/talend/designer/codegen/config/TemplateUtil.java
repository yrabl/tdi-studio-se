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
package org.talend.designer.codegen.config;

import org.talend.designer.codegen.model.ICodegenConstants;

/**
 * CodeGenerator Templates Ressources Utils.
 * 
 * $Id$
 * 
 */
public class TemplateUtil {

    public static final String RESOURCES_DIRECTORY = ICodegenConstants.PATH_RESOURCES;

    public static final String JET_STUB_DIRECTORY = ICodegenConstants.PATH_JET_STUB;

    // TODO SML Replace by File.separator
    public static final String DIR_SEP = "/"; //$NON-NLS-1$

    public static final String EXT_SEP = "."; //$NON-NLS-1$

    public static final String TEMPLATE_EXT = "jet"; //$NON-NLS-1$

    private String resourceName = ""; //$NON-NLS-1$

    private String version = ""; //$NON-NLS-1$

    public static final String RESOURCES_DIRECTORY_GENERIC = "generic"; //$NON-NLS-1$

    /**
     * Constructor.
     * 
     * @param resourceName
     */
    public TemplateUtil(EInternalTemplate template) {
        this.resourceName = template.getTemplateName();
        this.version = template.getVersion();
    }

    public TemplateUtil(String resourceName, String version) {
        this.resourceName = resourceName;
        this.version = version;
    }

    /**
     * Getter for resourceName.
     * 
     * @return the resourceName
     */
    public String getResourceName() {
        return this.resourceName;
    }

    /**
     * Sets the resourceName.
     * 
     * @param resourceName the resourceName to set
     */
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * Getter for version.
     * 
     * @return the version
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Sets the version.
     * 
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object arg0) {
        if (arg0 != null) {
            try {
                TemplateUtil t = (TemplateUtil) arg0;
                if (t.getResourceName() != null) {
                    if ((t.getResourceName().equals(this.getResourceName())) & (t.getVersion().equals(this.getVersion()))) {
                        return true;
                    }
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}
