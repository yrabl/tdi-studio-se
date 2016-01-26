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
package org.talend.component.core.model.migration;

import java.util.Arrays;
import java.util.ResourceBundle;

import org.talend.commons.i18n.MessagesCore;

/**
 * 
 * created by hcyi on Nov 19, 2015 Detailled comment
 *
 */
public class GenericParametersProvider extends MessagesCore {

    private static final String BUNDLE_NAME = "genericParameters"; //$NON-NLS-1$

    private static final String PLUGIN_ID = "org.talend.component.core"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Returns the i18n formatted message for <i>key</i> in the class bundle.
     * 
     * @param key - the key for the desired string
     * @return the string for the given key in the class resource bundle
     * @see MessagesCore#getString(String, ResourceBundle)
     */
    public static String getString(final String key) {
        return getString(key, PLUGIN_ID, RESOURCE_BUNDLE);
    }

    /**
     * Returns the i18n formatted message for <i>key</i> and <i>args</i> in the specified bundle.
     * 
     * @param key - the key for the desired string
     * @param args - arg to include in the string
     * @return the string for the given key in the given resource bundle
     * @see MessagesCore#getString(String, ResourceBundle, Object[])
     */
    public static String getString(final String key, final Object... args) {
        return getString(key, PLUGIN_ID, RESOURCE_BUNDLE, args);
    }

    /**
     * Returns the value corresponding to the specified key.
     */
    public static String getString(GenericParameters key) {
        return getString(key.getParamName(), PLUGIN_ID, RESOURCE_BUNDLE);
    }

    /**
     * Returns a sorted string array containing values corresponding to the specified key.
     */
    public static String[] getStrings(GenericParameters key) {
        String value = getString(key);
        String[] toReturn = value.split(","); //$NON-NLS-1$
        Arrays.sort(toReturn);
        return toReturn;
    }

    public enum GenericParameters {
        AUTHORIZED_LANGUAGE("param.authorizedlanguage"), //$NON-NLS-1$
        DEFAULT_PERL_INTERPRETER_WIN32("param.defaultPerlInterpreterPath.win32"), //$NON-NLS-1$
        DEFAULT_PERL_INTERPRETER_LINUX("param.defaultPerlInterpreterPath.linux"), //$NON-NLS-1$
        DEFAULT_PERL_INTERPRETER_EMBEDDED_SUFFIX_WIN32("param.defaultPerlInterpreterEmbeddedSuffix.win32"), //$NON-NLS-1$
        DEFAULT_JAVA_INTERPRETER_SUFFIX_WIN32("param.defaultJavaInterpreterSuffix.win32"), //$NON-NLS-1$
        DEFAULT_JAVA_INTERPRETER_SUFFIX_LINUX("param.defaultJavaInterpreterSuffix.linux"), //$NON-NLS-1$
        PROJECTS_EXCLUDED_FROM_EXPORT("param.projectsExcludedFromExport"); //$NON-NLS-1$

        private String paramName;

        GenericParameters(String paramName) {
            this.paramName = paramName;
        }

        public String getParamName() {
            return this.paramName;
        }
    }
}
