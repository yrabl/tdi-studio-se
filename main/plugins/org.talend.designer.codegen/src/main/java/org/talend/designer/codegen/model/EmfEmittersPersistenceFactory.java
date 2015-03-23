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
package org.talend.designer.codegen.model;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.talend.core.language.ECodeLanguage;

/**
 * DOC mhirt class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public final class EmfEmittersPersistenceFactory {

    private static EmfEmittersPersistence singleton = null;

    private EmfEmittersPersistenceFactory() {
    }

    public static EmfEmittersPersistence getInstance() {
        if (singleton == null) {

            final IProject project = CodeGeneratorEmittersPoolFactory.initializeJetEmittersProject(new NullProgressMonitor());
            IFile jetPersistenceFile = null;
            if (project != null) {
                jetPersistenceFile = project.getFile("JetPersistence" + ECodeLanguage.JAVA); //$NON-NLS-1$
                File file = jetPersistenceFile.getLocation().toFile();
                singleton = new EmfEmittersPersistence(ECodeLanguage.JAVA, file);
            }
        }

        return singleton;
    }

}
