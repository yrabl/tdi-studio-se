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
package org.talend.designer.rowgenerator.data;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.core.i18n.Messages;
import org.talend.designer.rowgenerator.ui.editor.MetadataColumnExt;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class FunctionManagerExtTest {

    public static final String PURE_PERL_NAME = "..."; //$NON-NLS-1$

    public static final String PURE_PERL_DESC = Messages.getString("FunctionManager.PurePerl.Desc"); //$NON-NLS-1$

    public static final String PURE_PERL_PARAM = Messages.getString("FunctionManager.PurePerl.ParaName"); //$NON-NLS-1$

    private String value = "((String)globalMap.get(\"sTest\")).replaceAll(\"a\",\"e\").replaceAll(\"i\",\"o\")";

    /**
     * DOC Administrator Comment method "setUp".
     * 
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * DOC Administrator Comment method "tearDown".
     * 
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * 
     * DOC Administrator Comment method "testGetAvailableFunFromValue". test private method getAvailableFunFromValue(,,)
     * 
     * @throws Exception
     */
    @Test
    public void testGetAvailableFunFromValue() throws Exception {
        MetadataColumnExt baseConn = mock(MetadataColumnExt.class);
        when(baseConn.getTalendType()).thenReturn("id_String");

        AbstractFunctionParser parser = null;
        parser = new RoutineFunctionParser();
        parser.parse();
        List<TalendType> talendTypes = parser.getList();
        List<Function> funtions = new ArrayList<Function>();

        for (TalendType talendType : talendTypes) {
            if (talendType.getName().equals(baseConn.getTalendType())) {
                funtions.addAll(talendType.getFunctions());
            }
        }
        funtions.add(createCustomizeFunction());

        Method method = FunctionManagerExt.class.getDeclaredMethod("getAvailableFunFromValue", new Class[] {
                MetadataColumnExt.class, String.class, List.class });
        method.setAccessible(true);
        Function funtion = (Function) method.invoke(new FunctionManagerExt(), baseConn, value, funtions);
        Parameter parameter = null;
        for (Object stringParameter : funtion.getParameters()) {
            parameter = (Parameter) stringParameter;
        }
        assertEquals(value, parameter.getValue());
    }

    private Function createCustomizeFunction() {
        Function function = new Function();
        function.setName(PURE_PERL_NAME);
        function.setDescription(PURE_PERL_DESC);
        StringParameter param = new StringParameter();
        List<Parameter> params = new ArrayList<Parameter>();
        param.setName(PURE_PERL_PARAM);
        params.add(param);
        function.setParameters(params);
        return function;
    }

}
