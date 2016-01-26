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
package org.talend.designer.mapper.ui.automap;

import java.util.HashMap;
import java.util.List;

import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.mapper.language.ILanguage;
import org.talend.designer.mapper.language.LanguageProvider;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.model.table.InputTable;
import org.talend.designer.mapper.model.table.OutputTable;
import org.talend.designer.mapper.ui.visualmap.table.DataMapTableView;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public class AutoMapper {

    private MapperManager mapperManager;

    /**
     * Map all empty output expression cells if possible.
     * 
     * @param manager
     */
    public AutoMapper(MapperManager mapperManager) {
        this.mapperManager = mapperManager;
    }

    /**
     * DOC amaumont Comment method "map".
     */
    public void map() {
        List<InputTable> inputTables = mapperManager.getInputTables();
        List<OutputTable> outputTables = mapperManager.getOutputTables();

        ILanguage currentLanguage = LanguageProvider.getCurrentLanguage();

        HashMap<String, InputTable> nameToInputTable = new HashMap<String, InputTable>(inputTables.size());

        for (InputTable inputTable : inputTables) {
            nameToInputTable.put(inputTable.getName(), inputTable);
        }

        // output tables are the references
        for (OutputTable outputTable : outputTables) {

            List<IColumnEntry> outputEntries = outputTable.getColumnEntries();
            boolean mapFound = false;
            for (IColumnEntry outputEntry : outputEntries) {

                mapFound = false;

                if (mapperManager.checkEntryHasEmptyExpression(outputEntry)) {

                    String outputColumnName = outputEntry.getName();

                    for (InputTable inputTable : inputTables) {

                        List<IColumnEntry> inputColumnEntries = inputTable.getColumnEntries();
                        for (IColumnEntry inputEntry : inputColumnEntries) {
                            if (inputEntry.getName().equalsIgnoreCase(outputColumnName)) {
                                outputEntry.setExpression(currentLanguage.getLocation(inputTable.getName(), inputEntry
                                        .getName()));
                                mapFound = true;
                                break;
                            }
                        }
                        if (mapFound) {
                            break;
                        }

                    }

                }

            }
            DataMapTableView view = mapperManager.retrieveAbstractDataMapTableView(outputTable);
            view.getTableViewerCreatorForColumns().getTableViewer().refresh();

        }

        mapperManager.getProblemsManager().checkProblems();

        List<DataMapTableView> outputsTablesView = mapperManager.getUiManager().getOutputsTablesView();
        for (DataMapTableView view : outputsTablesView) {
            mapperManager.getUiManager().parseAllExpressions(view, true);
            mapperManager.getProblemsManager().checkProblemsForAllEntries(view, true);
        }
        mapperManager.getUiManager().refreshBackground(true, false);

    }

}
