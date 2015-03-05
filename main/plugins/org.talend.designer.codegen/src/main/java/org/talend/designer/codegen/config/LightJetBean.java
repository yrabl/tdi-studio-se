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
package org.talend.designer.codegen.config;

import java.io.Serializable;

/**
 * DOC mhirt class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public class LightJetBean implements Serializable {

    private static final long serialVersionUID = 2549445027941196550L;

    protected static final String EMPTY = ""; //$NON-NLS-1$

    private String jetPluginRepository;

    private String templateRelativeUri = EMPTY;

    private String className = EMPTY;

    private String methodName = EMPTY;

    private String version = EMPTY;

    private String language = EMPTY;

    private long crc = 0;

    public LightJetBean(String jetPluginRepository, String templateRelativeUri, String className, String methodName,
            String version, String language, long crc) {
        this.jetPluginRepository = jetPluginRepository;
        this.templateRelativeUri = templateRelativeUri;
        this.className = className;
        this.methodName = methodName;
        this.version = version;
        this.language = language;
        this.crc = crc;
    }

    public LightJetBean(String templateRelativeUri, String className, String methodName, String version, String language, long crc) {
        this(null, templateRelativeUri, className, methodName, version, language, crc);
    }

    public LightJetBean() {
        this(EMPTY, EMPTY, 0);
    }

    public LightJetBean(String templateRelativeUri, String version, long crc) {
        this(templateRelativeUri, EMPTY, EMPTY, version, EMPTY, crc);
    }

    /**
     * Getter for jetPluginRepository.
     * 
     * @return the jetPluginRepository
     */
    public String getJetPluginRepository() {
        return jetPluginRepository;
    }

    /**
     * Sets the jetPluginRepository.
     * 
     * @param jetPluginRepository the jetPluginRepository to set
     */
    public void setJetPluginRepository(String jetPluginRepository) {
        this.jetPluginRepository = jetPluginRepository;
    }

    /**
     * Getter for templateRelativeUri.
     * 
     * @return the templateRelativeUri
     */
    public String getTemplateRelativeUri() {
        return this.templateRelativeUri;
    }

    /**
     * Sets the templateRelativeUri.
     * 
     * @param templateRelativeUri the templateRelativeUri to set
     */
    public void setTemplateRelativeUri(String templateRelativeUri) {
        this.templateRelativeUri = templateRelativeUri;
    }

    /**
     * Getter for className.
     * 
     * @return the className
     */
    public String getClassName() {
        return this.className;
    }

    /**
     * Sets the className.
     * 
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Getter for methodName.
     * 
     * @return the methodName
     */
    public String getMethodName() {
        return this.methodName;
    }

    /**
     * Sets the methodName.
     * 
     * @param methodName the methodName to set
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
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

    /**
     * Getter for crc.
     * 
     * @return the crc
     */
    public long getCrc() {
        return this.crc;
    }

    /**
     * Sets the crc.
     * 
     * @param crc the crc to set
     */
    public void setCrc(long crc) {
        this.crc = crc;
    }

    /**
     * Getter for language.
     * 
     * @return the language
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Sets the language.
     * 
     * @param language the language to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.jetPluginRepository == null) ? 0 : this.jetPluginRepository.hashCode());
        result = prime * result + ((this.templateRelativeUri == null) ? 0 : this.templateRelativeUri.hashCode());
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
        if (!(obj instanceof LightJetBean)) {
            return false;
        }
        LightJetBean other = (LightJetBean) obj;
        if (this.jetPluginRepository == null) {
            if (other.jetPluginRepository != null) {
                return false;
            }
        } else if (!this.jetPluginRepository.equals(other.jetPluginRepository)) {
            return false;
        }
        if (this.templateRelativeUri == null) {
            if (other.templateRelativeUri != null) {
                return false;
            }
        } else if (!this.templateRelativeUri.equals(other.templateRelativeUri)) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [Bundle=" + this.jetPluginRepository + ", Uri=" + this.templateRelativeUri
                + "]";
    }

}
