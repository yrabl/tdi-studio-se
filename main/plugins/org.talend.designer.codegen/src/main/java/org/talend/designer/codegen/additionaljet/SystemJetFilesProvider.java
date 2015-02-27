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
package org.talend.designer.codegen.additionaljet;

import java.io.File;

import org.talend.core.language.ECodeLanguage;
import org.talend.designer.codegen.config.EInternalTemplate;
import org.talend.designer.codegen.config.TemplateUtil;

/**
 * created by ggu on Sep 22, 2014 Detailled comment
 *
 */
public class SystemJetFilesProvider extends AbstractJetFileProvider {

    // @Override
    // public String getBundleId() {
    // return CodeGeneratorActivator.PLUGIN_ID;
    // }

    @Override
    protected boolean validResource(File res) {
        // only deal with the tempalte for internal??
        for (EInternalTemplate utilTemplate : EInternalTemplate.values()) {
            // only process the javajet, and ignore the perljet.
            // TODO, but need check the *.skeleton or not?
            if (res.getName().equals(
                    utilTemplate.getTemplateName() + TemplateUtil.EXT_SEP + ECodeLanguage.JAVA.getExtension()
                            + TemplateUtil.TEMPLATE_EXT)) {
                return true;
            }
        }
        return false;
    }

}
