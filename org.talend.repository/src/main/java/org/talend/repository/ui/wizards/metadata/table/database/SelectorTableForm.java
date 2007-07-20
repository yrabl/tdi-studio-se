// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2007 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.repository.ui.wizards.metadata.table.database;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.UtilsButton;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator.LAYOUT_MODE;
import org.talend.commons.utils.data.text.IndiceHelper;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.TableHelper;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataFromDataBase;
import org.talend.core.model.metadata.editor.MetadataEmfTableEditor;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ui.swt.utils.AbstractForm;
import org.talend.repository.ui.utils.ManagerConnection;

/**
 * @author cantoine
 * 
 */
public class SelectorTableForm extends AbstractForm {

    /**
     * FormTable Settings.
     */
    private static final int WIDTH_GRIDDATA_PIXEL = 700;

    /**
     * FormTable Var.
     */
    private ManagerConnection managerConnection;

    private List<String> itemTableName;

    private IMetadataConnection iMetadataConnection = null;

    private MetadataTable metadataTable;

    private MetadataEmfTableEditor metadataEditor;

    private UtilsButton selectAllTablesButton;

    private UtilsButton selectNoneTablesButton;

    private UtilsButton checkConnectionButton;

    /**
     * Anothers Fields.
     */
    private ConnectionItem connectionItem;

    // private DatabaseConnection connection;

    protected Table table;

    private Collection<TableItem> tableItems;

    private int count = 0;

    private WizardPage parentWizardPage;

    /**
     * TableForm Constructor to use by RCP Wizard.
     * 
     * @param parent
     * @param page
     * @param connection
     * @param page
     * @param metadataTable
     */
    public SelectorTableForm(Composite parent, ConnectionItem connectionItem, WizardPage page) {
        super(parent, SWT.NONE);
        managerConnection = new ManagerConnection();
        this.connectionItem = connectionItem;
        this.parentWizardPage = page;
        setupForm();
    }

    /**
     * 
     * Initialize value, forceFocus first field for right Click (new Table).
     * 
     */
    public void initialize() {
    }

    public void initializeForm() {
        initExistingNames();
        selectAllTablesButton.setEnabled(true);
        count = 0;
    }

    protected void addFields() {
        int leftCompositeWidth = 80;
        int rightCompositeWidth = WIDTH_GRIDDATA_PIXEL - leftCompositeWidth;
        int headerCompositeHeight = 60;
        int tableSettingsCompositeHeight = 90;
        int tableCompositeHeight = 200;

        int height = headerCompositeHeight + tableSettingsCompositeHeight + tableCompositeHeight;

        // Main Composite : 2 columns
        Composite mainComposite = Form.startNewDimensionnedGridLayout(this, 1, leftCompositeWidth + rightCompositeWidth, height);
        mainComposite.setLayout(new GridLayout(1, false));
        GridData gridData = new GridData(GridData.FILL_BOTH);
        mainComposite.setLayoutData(gridData);

        Composite rightComposite = Form.startNewDimensionnedGridLayout(mainComposite, 1, rightCompositeWidth, height);

        // Group Table Settings
        Group groupTableSettings = Form.createGroup(rightComposite, 1,
                Messages.getString("SelectorTableForm.groupTableSettings"), tableSettingsCompositeHeight); //$NON-NLS-1$

        // Composite TableSettings
        Composite compositeTableSettings = Form.startNewDimensionnedGridLayout(groupTableSettings, 1, rightCompositeWidth,
                tableSettingsCompositeHeight);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.widthHint = rightCompositeWidth;
        gridData.horizontalSpan = 3;

        ScrolledComposite scrolledCompositeFileViewer = new ScrolledComposite(compositeTableSettings, SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.NONE);
        scrolledCompositeFileViewer.setExpandHorizontal(true);
        scrolledCompositeFileViewer.setExpandVertical(true);
        GridData gridData1 = new GridData(GridData.FILL_BOTH);
        gridData1.widthHint = WIDTH_GRIDDATA_PIXEL;
        gridData1.heightHint = 325;
        gridData1.horizontalSpan = 2;
        scrolledCompositeFileViewer.setLayoutData(gridData1);

        // List Table
        TableViewerCreator tableViewerCreator = new TableViewerCreator(scrolledCompositeFileViewer);
        tableViewerCreator.setColumnsResizableByDefault(true);
        tableViewerCreator.setBorderVisible(true);
        tableViewerCreator.setLayoutMode(LAYOUT_MODE.FILL_HORIZONTAL);
        tableViewerCreator.setCheckboxInFirstColumn(true);
        // tableViewerCreator.setAdjustWidthValue(-15);
        tableViewerCreator.setFirstColumnMasked(true);

        table = tableViewerCreator.createTable();
        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        // table = new Table(scrolledCompositeFileViewer, SWT.CHECK | SWT.BORDER);
        // table.setBackground(table.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        // table.setHeaderVisible(true);

        // table.setHeaderVisible(true);
        TableColumn tableName = new TableColumn(table, SWT.NONE);
        tableName.setText(Messages.getString("SelectorTableForm.TableName")); //$NON-NLS-1$
        tableName.setWidth(300);

        TableColumn tableType = new TableColumn(table, SWT.NONE);
        tableType.setText(Messages.getString("SelectorTableForm.TableType")); //$NON-NLS-1$
        tableType.setWidth(140);

        TableColumn nbColumns = new TableColumn(table, SWT.RIGHT);
        nbColumns.setText(Messages.getString("SelectorTableForm.ColumnNumber")); //$NON-NLS-1$
        nbColumns.setWidth(125);

        TableColumn creationStatus = new TableColumn(table, SWT.RIGHT);
        creationStatus.setText(Messages.getString("SelectorTableForm.CreationStatus")); //$NON-NLS-1$
        creationStatus.setWidth(140);

        scrolledCompositeFileViewer.setContent(table);
        scrolledCompositeFileViewer.setMinSize(table.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        // Composite retreiveSchema
        Composite compositeRetreiveSchemaButton = Form.startNewGridLayout(compositeTableSettings, 3, false, SWT.CENTER,
                SWT.BOTTOM);

        // Button Create Table
        selectAllTablesButton = new UtilsButton(compositeRetreiveSchemaButton, Messages
                .getString("SelectorTableForm.selectAllTables"), WIDTH_BUTTON_PIXEL, HEIGHT_BUTTON_PIXEL); //$NON-NLS-1$

        selectNoneTablesButton = new UtilsButton(compositeRetreiveSchemaButton, Messages
                .getString("SelectorTableForm.selectNoneTables"), WIDTH_BUTTON_PIXEL, HEIGHT_BUTTON_PIXEL); //$NON-NLS-1$

        // Button Check Connection
        checkConnectionButton = new UtilsButton(compositeRetreiveSchemaButton, Messages
                .getString("DatabaseTableForm.checkConnection"), WIDTH_BUTTON_PIXEL, HEIGHT_BUTTON_PIXEL); //$NON-NLS-1$

        metadataEditor = new MetadataEmfTableEditor(Messages.getString("DatabaseTableForm.metadataDescription")); //$NON-NLS-1$
        addUtilsButtonListeners();
    }

    /**
     * addButtonControls.
     * 
     */
    protected void addUtilsButtonListeners() {

        // Event CheckConnection Button
        checkConnectionButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(final SelectionEvent e) {
                selectAllTablesButton.setEnabled(true);
                count = 0;
                if (!checkConnectionButton.getEnabled()) {
                    checkConnectionButton.setEnabled(true);
                    checkConnection(true);
                } else {
                    checkConnectionButton.setEnabled(false);
                }
            }
        });

        selectAllTablesButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(final SelectionEvent e) {
                updateStatus(IStatus.ERROR, null);
                selectAllTablesButton.setEnabled(false);
                selectNoneTablesButton.setEnabled(false);
                checkConnectionButton.setEnabled(false);
                if (!table.getEnabled()) {
                    TableItem[] tableItems = table.getItems();
                    int size = tableItems.length;
                    for (int i = 0; i < tableItems.length; i++) {
                        TableItem tableItem = tableItems[i];
                        table.setEnabled(true);
                        if (!tableItem.getChecked()) {
                            tableItem.setText(3, Messages.getString("SelectorTableForm.Pending")); //$NON-NLS-1$
                            parentWizardPage.setPageComplete(false);
                            refreshTable(tableItem, size);
                        } else {
                            updateStatus(IStatus.OK, null);
                            selectNoneTablesButton.setEnabled(true);
                            checkConnectionButton.setEnabled(true);
                        }
                        tableItem.setChecked(true);
                    }
                } else {
                    table.setEnabled(false);
                }
            }
        });

        selectNoneTablesButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(final SelectionEvent e) {
                selectAllTablesButton.setEnabled(true);
                count = 0;
                if (!table.getEnabled()) {
                    TableItem[] tableItems = table.getItems();
                    for (int i = 0; i < tableItems.length; i++) {
                        TableItem tableItem = tableItems[i];
                        table.setEnabled(true);
                        if (tableItem.getChecked()) {
                            deleteTable(tableItem);
                            tableItem.setText(2, ""); //$NON-NLS-1$
                            tableItem.setText(3, ""); //$NON-NLS-1$
                        }
                        tableItem.setChecked(false);
                    }
                } else {
                    table.setEnabled(false);
                }
            }
        });

        // Event checkBox action
        table.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(final SelectionEvent e) {
                if (!table.getEnabled()) {
                    table.setEnabled(true);
                    if (e.detail == SWT.CHECK) {
                        TableItem tableItem = (TableItem) e.item;
                        boolean promptNeeded = tableItem.getChecked();
                        if (promptNeeded) {
                            tableItems.remove(tableItem);
                            tableItems.add(tableItem);
                            tableItem.setText(3, Messages.getString("SelectorTableForm.Pending")); //$NON-NLS-1$
                            parentWizardPage.setPageComplete(false);
                            refreshTable(tableItem, -1);
                        } else {
                            tableItems.remove(tableItem);
                            deleteTable(tableItem);
                            tableItem.setText(2, ""); //$NON-NLS-1$
                            tableItem.setText(3, ""); //$NON-NLS-1$
                        }
                    }
                } else {
                    table.setEnabled(false);
                }
            }
        });
    }

    /**
     * checkConnection.
     * 
     * @param displayMessageBox
     */
    protected void checkConnection(final boolean displayMessageBox) {
        try {
            if (table.getItemCount() > 0) {
                table.removeAll();
            }
            parentWizardPage.getWizard().getContainer().run(true, true, new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(Messages.getString("CreateTableAction.action.createTitle"), IProgressMonitor.UNKNOWN);

                    tableItems = new ArrayList<TableItem>();

                    iMetadataConnection = ConvertionHelper.convert(getConnection());
                    managerConnection.check(iMetadataConnection);

                    if (managerConnection.getIsValide()) {
                        itemTableName = ExtractMetaDataFromDataBase.returnTablesFormConnection(iMetadataConnection);
                        if (itemTableName.size() <= 0) {
                            // connection is done but any table exist
                            if (displayMessageBox) {
                                openInfoDialogInUIThread(getShell(),
                                        Messages.getString("DatabaseTableForm.checkConnection"), Messages //$NON-NLS-1$
                                                .getString("DatabaseTableForm.tableNoExist"), true);//$NON-NLS-1$
                            }
                        } else {
                            Display.getDefault().asyncExec(new Runnable() {

                                public void run() {
                                    // connection is done and tables exist
                                    if (itemTableName != null && !itemTableName.isEmpty()) {
                                        // fill the combo
                                        Iterator<String> iterate = itemTableName.iterator();
                                        while (iterate.hasNext()) {
                                            String nameTable = iterate.next();
                                            TableItem item = new TableItem(table, SWT.NONE);
                                            item.setText(0, nameTable);
                                            item.setText(1, ExtractMetaDataFromDataBase.getTableTypeByTableName(nameTable));
                                        }
                                    }
                                    if (displayMessageBox) {
                                        String msg = Messages.getString("DatabaseTableForm.connectionIsDone"); //$NON-NLS-1$
                                        openInfoDialogInUIThread(getShell(), Messages
                                                .getString("DatabaseTableForm.checkConnection"), msg, false);
                                    }
                                }
                            });
                        }
                    } else if (displayMessageBox) {
                        // connection failure
                        getShell().getDisplay().asyncExec(new Runnable() {

                            public void run() {
                                new ErrorDialogWidthDetailArea(getShell(), PID, Messages
                                        .getString("DatabaseTableForm.connectionFailureTip"), //$NON-NLS-1$
                                        managerConnection.getMessageException());
                            }
                        });
                    }
                    monitor.done();
                }
            });
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

    }

    public static void openInfoDialogInUIThread(final Shell shell, final String title, final String msg, boolean ifUseRunnable) {
        if (ifUseRunnable) {
            shell.getDisplay().asyncExec(new Runnable() {

                public void run() {
                    MessageDialog.openInformation(shell, title, msg);
                }
            });
        } else {
            MessageDialog.openInformation(shell, title, msg);
        }
    }

    /**
     * createTable.
     * 
     * @param tableItem
     */
    protected void createTable(TableItem tableItem) {
        String tableString = tableItem.getText(0);

        IMetadataConnection iMetadataConnection = ConvertionHelper.convert(getConnection());
        boolean checkConnectionIsDone = managerConnection.check(iMetadataConnection);
        if (!checkConnectionIsDone) {
            updateStatus(IStatus.WARNING, Messages.getString("DatabaseTableForm.connectionFailure")); //$NON-NLS-1$
            new ErrorDialogWidthDetailArea(getShell(), PID, Messages.getString("DatabaseTableForm.connectionFailure"), //$NON-NLS-1$
                    managerConnection.getMessageException());
        } else {
            List<MetadataColumn> metadataColumns = new ArrayList<MetadataColumn>();
            metadataColumns = ExtractMetaDataFromDataBase.returnMetadataColumnsFormTable(iMetadataConnection, tableItem
                    .getText(0));

            tableItem.setText(2, "" + metadataColumns.size()); //$NON-NLS-1$
            tableItem.setText(3, Messages.getString("SelectorTableForm.Success")); //$NON-NLS-1$

            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

            metadataTable = ConnectionFactory.eINSTANCE.createMetadataTable();

            initExistingNames();
            metadataTable.setLabel(IndiceHelper.getIndexedLabel(tableString, existingNames));
            metadataTable.setSourceName(tableItem.getText(0));
            metadataTable.setId(factory.getNextId());
            metadataTable.setTableType(ExtractMetaDataFromDataBase.getTableTypeByTableName(tableString));

            List<MetadataColumn> metadataColumnsValid = new ArrayList<MetadataColumn>();
            Iterator iterate = metadataColumns.iterator();

            while (iterate.hasNext()) {
                MetadataColumn metadataColumn = (MetadataColumn) iterate.next();

                // Check the label and add it to the table
                metadataColumnsValid.add(metadataColumn);
                metadataTable.getColumns().add(metadataColumn);
            }
            getConnection().getTables().add(metadataTable);
        }
    }

    /**
     * deleteTable.
     * 
     * @param tableItem
     */
    protected void deleteTable(TableItem tableItem) {

        if (itemTableName != null && !itemTableName.isEmpty()) {
            // fill the combo
            Collection tables = new ArrayList();
            Iterator<MetadataTable> iterate = getConnection().getTables().iterator();
            while (iterate.hasNext()) {
                MetadataTable metadata = iterate.next();
                if (metadata.getLabel().equals(tableItem.getText(0))) {
                    tables.add(metadata);
                }
            }
            getConnection().getTables().removeAll(tables);
        }
    }

    /**
     * refreshTable. This Methos execute the CreateTable in a Thread task.
     * 
     * @param tableItem
     * @param size
     */
    private void refreshTable(final TableItem tableItem, final int size) {
        getDisplay().asyncExec(new Runnable() {

            public void run() {
                createTable(tableItem);
                count++;
                if (count == size) {
                    updateStatus(IStatus.OK, null);
                    selectNoneTablesButton.setEnabled(true);
                    checkConnectionButton.setEnabled(true);
                }
                parentWizardPage.setPageComplete(nextEnable());
            }

            private boolean nextEnable() {
                TableItem[] items = table.getItems();
                for (TableItem item : items) {
                    String s = item.getText(3);
                    if (Messages.getString("SelectorTableForm.Pending").equals(s)) {
                        return false;
                    }
                }
                return true;
            }
        });
    }


    /**
     * DOC ocarbone Comment method "initExistingNames".
     * 
     * @param connection
     * @param metadataTable
     */
    private void initExistingNames() {
        String[] exisNames;
        if (metadataTable != null) {
            exisNames = TableHelper.getTableNames(getConnection(), metadataTable.getLabel());
        } else {
            exisNames = TableHelper.getTableNames(getConnection());
        }
        this.existingNames = existingNames == null ? Collections.EMPTY_LIST : Arrays.asList(exisNames);
    }

    /**
     * Main Fields addControls.
     */
    protected void addFieldsListeners() {

    }

    /**
     * Ensures that fields are set. Update checkEnable / use to checkTableSetting().
     */
    protected boolean checkFieldsValue() {
        updateStatus(IStatus.OK, null);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.swt.AbstractForm#adaptFormToReadOnly()
     */
    protected void adaptFormToReadOnly() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.widgets.Control#setVisible(boolean)
     * 
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            initializeForm();
        }
        checkConnection(false);
    }

    protected DatabaseConnection getConnection() {
        return (DatabaseConnection) connectionItem.getConnection();
    }

    public Table getTable() {
        return this.table;
    }
}
