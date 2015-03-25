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

import org.talend.commons.utils.StringUtils;
import org.talend.core.language.ECodeLanguage;

/**
 * DOC mhirt class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public class LightJetBean implements Serializable {

    private static final long serialVersionUID = 2549445027941196550L;

    protected static final String EMPTY = ""; //$NON-NLS-1$

    protected static final String DEFAULT_VERSION = "0.0.1"; //$NON-NLS-1$

    private String jetBundle;

    private String relativeUri = EMPTY;

    private String className = EMPTY;

    private String methodName = EMPTY;

    private String version = EMPTY;

    private final String language;

    private long crc = 0;

    public LightJetBean(String jetBundle, String relativeUri, String className, String methodName, String version, long crc) {
        this.jetBundle = jetBundle;
        this.relativeUri = relativeUri;
        this.className = className;
        this.methodName = methodName;
        this.version = version;
        this.crc = crc;
        this.language = StringUtils.capitalize(ECodeLanguage.JAVA.getName());
    }

    public LightJetBean(String relativeUri, String className, String methodName, String version, long crc) {
        this(null, relativeUri, className, methodName, version, crc);
    }

    public LightJetBean() {
        this(EMPTY, EMPTY, 0);
    }

    public LightJetBean(String relativeUri, String version, long crc) {
        this(relativeUri, EMPTY, EMPTY, version, EMPTY, crc);
    }

    /**
     * Getter for jetBundle.
     * 
     * @return the jetBundle
     */
    public String getJetBundle() {
        return jetBundle;
    }

    /**
     * Sets the jetBundle.
     * 
     * @param jetPluginRepository the jetBundle to set
     */
    public void setJetBundle(String jetBundle) {
        this.jetBundle = jetBundle;
    }

    /**
     * Getter for relativeUri.
     * 
     * @return the relativeUri
     */
    public String getRelativeUri() {
        return this.relativeUri;
    }

    /**
     * Sets the relativeUri.
     * 
     * @param templateRelativeUri the relativeUri to set
     */
    public void setRelativeUri(String relativeUri) {
        this.relativeUri = relativeUri;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.jetBundle == null) ? 0 : this.jetBundle.hashCode());
        result = prime * result + ((this.relativeUri == null) ? 0 : this.relativeUri.hashCode());
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
        if (this.jetBundle == null) {
            if (other.jetBundle != null) {
                return false;
            }
        } else if (!this.jetBundle.equals(other.jetBundle)) {
            return false;
        }
        if (this.relativeUri == null) {
            if (other.relativeUri != null) {
                return false;
            }
        } else if (!this.relativeUri.equals(other.relativeUri)) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [Bundle=" + this.jetBundle + ", Uri=" + this.relativeUri + "]";
    }

}
