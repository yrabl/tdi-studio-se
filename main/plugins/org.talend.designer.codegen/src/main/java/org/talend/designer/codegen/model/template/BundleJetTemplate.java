// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.codegen.model.template;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Path;

/**
 * created by ggu on Sep 19, 2014 Detailled comment
 *
 */
public class BundleJetTemplate {

    private static final String DEFAULT_VERSION = "0.0.1"; //$NON-NLS-1$

    private final String bundleId;

    private final String relativePath;

    private String version = DEFAULT_VERSION;

    /**
     * key is name of lib for classpath, the value is name of dependence bundle.
     */
    private Map<String, String> jetTempalteDependences = new LinkedHashMap<String, String>();

    private ClassLoader jetTempalteClassLoader;

    public BundleJetTemplate(String bundleId, String relativePath) {
        this(bundleId, relativePath, DEFAULT_VERSION);

    }

    public BundleJetTemplate(String bundleId, String relativePath, String version) {
        super();
        Assert.isTrue(bundleId != null && bundleId.length() > 0);
        Assert.isTrue(relativePath != null && !relativePath.isEmpty());
        this.bundleId = bundleId;
        this.relativePath = relativePath;
        this.version = version;
    }

    public String getBundleId() {
        return this.bundleId;
    }

    public String getRelativePath() {
        return this.relativePath;
    }

    public String getVersion() {
        return this.version;
    }

    public Map<String, String> getJetTempalteDependences() {
        return this.jetTempalteDependences;
    }

    public ClassLoader getJetTempalteClassLoader() {
        return this.jetTempalteClassLoader;
    }

    public void setJetTempalteClassLoader(ClassLoader jetTempalteClassLoader) {
        this.jetTempalteClassLoader = jetTempalteClassLoader;
    }

    public String getName() {
        return new Path(getRelativePath()).removeFileExtension().lastSegment();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.bundleId == null) ? 0 : this.bundleId.hashCode());
        result = prime * result + ((this.relativePath == null) ? 0 : this.relativePath.hashCode());
        result = prime * result + ((this.version == null) ? 0 : this.version.hashCode());
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
        if (!(obj instanceof BundleJetTemplate)) {
            return false;
        }
        BundleJetTemplate other = (BundleJetTemplate) obj;
        if (this.bundleId == null) {
            if (other.bundleId != null) {
                return false;
            }
        } else if (!this.bundleId.equals(other.bundleId)) {
            return false;
        }
        if (this.relativePath == null) {
            if (other.relativePath != null) {
                return false;
            }
        } else if (!this.relativePath.equals(other.relativePath)) {
            return false;
        }
        if (this.version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!this.version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.bundleId + '\n' + this.relativePath;
    }

}
