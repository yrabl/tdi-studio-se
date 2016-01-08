package org.talend.designer.codegen.ui;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;
import org.talend.designer.codegen.CodeGeneratorActivator;

/**
 * The activator class controls the plug-in life cycle
 */
public class CodegenUiPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.talend.designer.codegen.ui"; //$NON-NLS-1$

    // The shared instance
    private static CodegenUiPlugin plugin;

    /**
     * The constructor
     */
    public CodegenUiPlugin() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static CodegenUiPlugin getDefault() {
        return plugin;
    }

    ScopedPreferenceStore prefStore = null;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#getPreferenceStore()
     */
    @Override
    public IPreferenceStore getPreferenceStore() {
        if (prefStore == null) {
            // use the same preference store as codegen plugin
            prefStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, CodeGeneratorActivator.PLUGIN_ID);
        }
        return prefStore;
    }

}
