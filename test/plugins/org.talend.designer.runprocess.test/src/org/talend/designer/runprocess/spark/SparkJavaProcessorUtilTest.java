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
package org.talend.designer.runprocess.spark;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.talend.designer.runprocess.ui.ProcessManager;

/**
 * created by rdubois on 8 juin 2015 Detailled comment
 *
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ProcessManager.class })
public class SparkJavaProcessorUtilTest {

    @Test
    public void testIsStatisticsPMIsNull() {
        PowerMockito.mockStatic(ProcessManager.class);
        PowerMockito.when(ProcessManager.getInstance()).thenReturn(null);

        assertFalse(SparkJavaProcessorUtil.isStatistics());
    }

    /*
     * TODO: Missing 2 test cases:
     * 
     * 1 - Test when getStat() returns false.
     * 
     * 2 - Test when getStat() returns true.
     */
}
