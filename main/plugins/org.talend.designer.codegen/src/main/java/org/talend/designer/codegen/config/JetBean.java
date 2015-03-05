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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.talend.commons.utils.StringUtils;

/**
 * Jet container for a particular component.
 * 
 * $Id$
 * 
 */
public class JetBean extends LightJetBean {

    private static final long serialVersionUID = -4453156746871080436L;

    private Object argument;

    private HashMap<String, String> classPath;

    private boolean forceOverwrite = true;

    private ClassLoader loader = null;

    private String codePart = null;

    private String family = "common"; //$NON-NLS-1$

    private String generationError;

    private static Map<String, String> pluginIdToBundle = new HashMap<String, String>();

    /**
     * Minimal Constructor.
     */
    public JetBean() {
    }

    /**
     * Full Constructor.
     * 
     * @param jetPluginRepository
     * @param classpathVariable
     * @param classpathParameter
     * @param templateRelativeUri
     */
    public JetBean(String jetPluginRepository, String templateRelativeUri, String className, String version, String language,
            String codePart) {
        super(jetPluginRepository, templateRelativeUri, EMPTY, EMPTY, version, language, 0);
        this.classPath = new HashMap<String, String>();

        String tmpClassName = ""; //$NON-NLS-1$
        if (className.lastIndexOf(".") > -1) { //$NON-NLS-1$
            tmpClassName = className.substring(className.lastIndexOf(".")); //$NON-NLS-1$
        } else {
            tmpClassName = className;
        }
        this.setClassName(StringUtils.capitalize(tmpClassName));
        this.setLanguage(StringUtils.capitalize(language));
        if ((codePart != null) && (codePart.length() != 0)) {
            this.codePart = StringUtils.capitalize(codePart);
        } else {
            this.codePart = ""; //$NON-NLS-1$
        }
    }

    /**
     * Getter for classPath.
     * 
     * @return the classPath
     */
    public HashMap<String, String> getClassPath() {
        return this.classPath;
    }

    /**
     * Sets the classPath.
     * 
     * @param classPath the classPath to set
     */
    public void setClassPath(HashMap<String, String> classPath) {
        this.classPath = classPath;
    }

    /**
     * add a variable to the classPath.
     * 
     * @param classpathVariable
     * @param classpathParameter
     */
    public void addClassPath(String classpathVariable, String classpathParameter) {
        this.classPath.put(classpathVariable, classpathParameter);
    }

    /**
     * Getter for argument.
     * 
     * @return the argument
     */
    public Object getArgument() {
        return argument;
    }

    /**
     * Sets the argument.
     * 
     * @param argument the argument to set
     */
    public void setArgument(Object argument) {
        this.argument = argument;
    }

    /**
     * Return this Bean Template Full URI.
     * 
     * @return
     */
    public String getTemplateFullUri() {
        return getUri(getJetPluginRepository(), getTemplateRelativeUri());
    }

    /**
     * Return uri for this plugin.
     * 
     * @param pluginId
     * @param relativeUri
     * @return
     */
    private String getUri(String pluginId, String relativeUri) {
        String base = null;
        if (pluginIdToBundle.containsKey(pluginId)) {
            base = pluginIdToBundle.get(pluginId);
        } else {
            base = Platform.getBundle(pluginId).getEntry("/").toString(); //$NON-NLS-1$
            pluginIdToBundle.put(pluginId, base);
        }
        String result = base + relativeUri;
        return result;
    }

    /**
     * Getter for forceOverwrite.
     * 
     * @return the forceOverwrite
     */
    public boolean isForceOverwrite() {
        return forceOverwrite;
    }

    /**
     * Sets the forceOverwrite.
     * 
     * @param forceOverwrite the forceOverwrite to set
     */
    public void setForceOverwrite(boolean forceOverwrite) {
        this.forceOverwrite = forceOverwrite;
    }

    /**
     * Getter for loader.
     * 
     * @return the loader
     */
    public ClassLoader getClassLoader() {
        return this.loader;
    }

    /**
     * Sets the loader.
     * 
     * @param loader the loader to set
     */
    public void setClassLoader(ClassLoader newloader) {
        this.loader = newloader;
    }

    /**
     * Getter for codePart.
     * 
     * @return the codePart
     */
    public String getCodePart() {
        return this.codePart;
    }

    /**
     * Sets the codePart.
     * 
     * @param codePart the codePart to set
     */
    public void setCodePart(String codePart) {
        this.codePart = codePart;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getGenerationError() {
        return generationError;
    }

    public void setGenerationError(String generationError) {
        this.generationError = generationError;
    }

    public LightJetBean createLightJetBean() {
        return new LightJetBean(getJetPluginRepository(), getTemplateRelativeUri(), getClassName(), getMethodName(),
                getVersion(), getLanguage(), getCrc());
    }
}
