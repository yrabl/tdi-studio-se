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
package org.talend.designer.core.model.process;

import static org.junit.Assert.*;

import org.eclipse.draw2d.geometry.Point;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class ConnectionManagerTest {

    private static Connection connection;

    private static Connection newConnection;

    private static Node source;

    private static Node newSource;

    private static IComponent componentSource;

    private static IComponent componentTarget;

    private static Process sourceProcess;

    private static Process targetProcess;

    private static Node target;

    private static Node newTarget;

    private static Property createProperty() {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$

        return property;
    }

    /**
     * Changed by Marvin Wang on Feb.22 for bug TDI-19166.
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        componentSource = ComponentsFactoryProvider.getInstance().get("tFileInputDelimited",
                ComponentCategory.CATEGORY_4_DI.getName());
        componentTarget = ComponentsFactoryProvider.getInstance().get("tFileOutputDelimited",
                ComponentCategory.CATEGORY_4_DI.getName());

        sourceProcess = new Process(createProperty());
        targetProcess = new Process(createProperty());

        source = new Node(componentSource, sourceProcess);
        source.setLocation(new Point(1, 10));
        target = new Node(componentTarget, targetProcess);
        target.setLocation(new Point(100, 2));
        newTarget = new Node(componentTarget, targetProcess);
        newTarget.setLocation(new Point(150, 200));
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        if (connection != null) {
            connection.disconnect();
        }
        if (newConnection != null) {
            newConnection.disconnect();
        }
    }

    /**
     * Test method for
     * {@link org.talend.designer.core.model.process.ConnectionManager#canConnectToSource(org.talend.designer.core.ui.editor.nodes.Node, org.talend.designer.core.ui.editor.nodes.Node, org.talend.designer.core.ui.editor.nodes.Node, org.talend.core.model.process.EConnectionType, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testCanConnectToSource() {
        try {
            connection = new Connection(source, target, EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(), "test",
                    "test", "test", false);
            // Check the circle, certainly, you can try to test with some other conditions.
            boolean canConnect = ConnectionManager.canConnectToSource(source, target, target, EConnectionType.FLOW_MAIN,
                    EConnectionType.FLOW_MAIN.getName(), "test");
            // The right result is can't be connected.
            assertTrue(!canConnect);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test CanConnectToSource() method failure.");
        }
    }

    /**
     * Test method for
     * {@link org.talend.designer.core.model.process.ConnectionManager#canConnectToTarget(org.talend.designer.core.ui.editor.nodes.Node, org.talend.designer.core.ui.editor.nodes.Node, org.talend.designer.core.ui.editor.nodes.Node, org.talend.core.model.process.EConnectionType, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testCanConnectToTarget() {
        try {
            // First time, check the situation of creating the connection.
            boolean canConnect = ConnectionManager.canConnectToTarget(source, null, target, EConnectionType.FLOW_MAIN,
                    EConnectionType.FLOW_MAIN.getName(), "test");
            assertTrue(canConnect);
            // Second time, check the situation of reconnect the connection.
            connection = new Connection(source, target, EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(), "test",
                    "test", "test", false);
            canConnect = ConnectionManager.canConnectToTarget(source, target, newTarget, EConnectionType.FLOW_MAIN,
                    EConnectionType.FLOW_MAIN.getName(), "test");
            assertTrue(canConnect);
            newConnection = new Connection(target, newTarget, EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(),
                    "test2", "test2", "test2", false);
            canConnect = ConnectionManager.canConnectToTarget(target, newTarget, target, EConnectionType.FLOW_MAIN,
                    EConnectionType.FLOW_MAIN.getName(), "test2");
            assertTrue(!canConnect);
            canConnect = ConnectionManager.canConnectToTarget(target, newTarget, newTarget, EConnectionType.FLOW_MAIN,
                    EConnectionType.FLOW_MAIN.getName(), "test2");
            assertTrue(!canConnect);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test CanConnectToSource() method failure.");
        }
    }

    /**
     * Test method for
     * {@link org.talend.designer.core.model.process.ConnectionManager#canRename(org.talend.designer.core.ui.editor.nodes.Node, org.talend.designer.core.ui.editor.nodes.Node, org.talend.core.model.process.EConnectionType, java.lang.String)}
     * .
     */
    @Test
    public void testCanRename() {
        try {
            connection = new Connection(source, target, EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(), "test",
                    "test", "test", false);
            boolean canRename = ConnectionManager.canRename(source, target, EConnectionType.FLOW_MAIN, "test1");
            assertTrue(canRename);
            canRename = ConnectionManager.canRename(source, target, EConnectionType.FLOW_MAIN, "test");
            assertTrue(!canRename);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test CanConnectToSource() method failure.");
        }
    }

}
