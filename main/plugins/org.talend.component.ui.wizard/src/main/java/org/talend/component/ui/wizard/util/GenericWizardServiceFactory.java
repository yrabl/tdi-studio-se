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
package org.talend.component.ui.wizard.util;

import org.talend.component.ui.wizard.service.GenericWizardService;
import org.talend.core.runtime.services.IGenericWizardService;

/**
 * created by ycbai on 2015年9月16日 Detailled comment
 *
 */
public class GenericWizardServiceFactory {

    private static IGenericWizardService service = null;

    public static IGenericWizardService getGenericWizardService() {
        if (service == null) {
            service = new GenericWizardService();
        }
        return service;
    }

}
