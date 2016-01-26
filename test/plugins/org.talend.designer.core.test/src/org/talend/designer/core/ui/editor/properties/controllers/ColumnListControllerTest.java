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
package org.talend.designer.core.ui.editor.properties.controllers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.talend.core.model.process.IElementParameter;

/**
 * created by ycbai on 2014-1-10 Detailled comment
 * 
 */
@RunWith(PowerMockRunner.class)
public class ColumnListControllerTest {

    private static ColumnListController instance;

    @BeforeClass
    public static void setUp() throws Exception {
        instance = mock(ColumnListController.class);
    }

    @Test
    public void testFilterColumns() throws Exception {
        String TEST_METHOD = "filterColumns"; //$NON-NLS-1$
        String[] TEST_COL_ARRAY = new String[] { "col1", "col2", "col3", "col4" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        Object[] argVals = null;
        List<String[]> resCols = null;
        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, true, false, false }, "CUSTOM_COLUMNS:col1"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col2", "col3", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { false, false, false, false }, "CUSTOM_COLUMNS:col1"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), TEST_COL_ARRAY);
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, true, true, false }, "CUSTOM_COLUMNS:col1,col2"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col3", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, false, false, false }, "CUSTOM_COLUMNS:col1,col2"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col2", "col3", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, true, true, false }, "CUSTOM_COLUMNS:*"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col4" }); //$NON-NLS-1$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, true, true, false },
                "CUSTOM_COLUMNS:REGEXP:col[^3]$"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col1", "col2", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, true, false, false },
                "CUSTOM_COLUMNS:REGEXP:col[^3]$"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), TEST_COL_ARRAY);
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { false, false, false, false },
                "NONE_CUSTOM_COLUMNS:col1"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col2", "col3", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, false, false, false }, "NONE_CUSTOM_COLUMNS:col1"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), TEST_COL_ARRAY);
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { false, false, false, false },
                "NONE_CUSTOM_COLUMNS:col1,col2"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col3", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, false, false, false },
                "NONE_CUSTOM_COLUMNS:col1,col2"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col1", "col3", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { false, false, false, true }, "NONE_CUSTOM_COLUMNS:*"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col4" }); //$NON-NLS-1$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { false, false, false, false },
                "NONE_CUSTOM_COLUMNS:REGEXP:col[^3]$"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col1", "col2", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { false, false, true, false },
                "NONE_CUSTOM_COLUMNS:REGEXP:col[^3]$"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), TEST_COL_ARRAY);
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { false, false, false, false }, "col1"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col2", "col3", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, false, false, false }, "col1"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col2", "col3", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, false, false, false }, "col1,col2"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col3", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { true, false, false, false }, "*"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertTrue(resCols.get(0).length == 0);
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { false, false, false, false }, "REGEXP:col[^3]$"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col1", "col2", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));

        argVals = getFilterColumnsArgVals(TEST_COL_ARRAY, new Boolean[] { false, false, true, false }, "REGEXP:col[^3]$"); //$NON-NLS-1$
        resCols = Whitebox.invokeMethod(instance, TEST_METHOD, argVals);
        assertArrayEquals(resCols.get(0), new String[] { "col1", "col2", "col4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertArrayEquals(resCols.get(1), resCols.get(0));
    }

    private Object[] getFilterColumnsArgVals(String[] columnNames, Boolean[] isCustom, String filter) {
        Object[] args = new Object[4];

        IElementParameter param = mock(IElementParameter.class);
        when(param.getFilter()).thenReturn(filter);
        Map<String, Boolean> customColMap = new HashMap<String, Boolean>();
        String[] columnNameArray = new String[columnNames.length];
        String[] columnValueArray = new String[columnNameArray.length];
        for (int i = 0; i < columnNames.length; i++) {
            columnNameArray[i] = columnNames[i];
            columnValueArray[i] = columnNameArray[i];
            customColMap.put(columnNameArray[i], isCustom[i]);
        }
        args[0] = param;
        args[1] = columnNameArray;
        args[2] = columnValueArray;
        args[3] = customColMap;

        return args;
    }

}
