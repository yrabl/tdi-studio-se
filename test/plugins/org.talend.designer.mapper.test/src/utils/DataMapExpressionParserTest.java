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
package utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;
import org.talend.designer.mapper.language.ILanguage;
import org.talend.designer.mapper.language.generation.GenerationManagerFactory;
import org.talend.designer.mapper.language.generation.PerlGenerationManager;
import org.talend.designer.mapper.language.perl.PerlLanguage;
import org.talend.designer.mapper.model.tableentry.TableEntryLocation;
import org.talend.designer.mapper.utils.DataMapExpressionParser;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 * $Id: DataMapExpressionParserTest.java 38013 2010-03-05 14:21:59Z mhirt $
 * 
 */
public class DataMapExpressionParserTest {

    /**
     * Test method for
     * {@link org.talend.designer.mapper.utils.DataMapExpressionParser#parseTableEntryLocations(java.lang.String)}.
     */
    @Test
    public void testParse() {
        ILanguage language = new PerlLanguage();
        DataMapExpressionParser expressionParser = new DataMapExpressionParser(language);
        PerlGenerationManager gen = (PerlGenerationManager) GenerationManagerFactory.getInstance().getGenerationManager(language);
        TableEntryLocation[] stringCouples = expressionParser.parseTableEntryLocations("abc * " //$NON-NLS-1$
                + gen.getTableColumnVariable("table1", "col1") + " + " + gen.getTableColumnVariable("ta_ble2", "co_l2") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                + " - " + gen.getTableColumnVariable("$table1", "col2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals(stringCouples[0].tableName, "table1"); //$NON-NLS-1$
        assertEquals(stringCouples[0].columnName, "col1"); //$NON-NLS-1$

        assertEquals(stringCouples[1].tableName, "ta_ble2"); //$NON-NLS-1$
        assertEquals(stringCouples[1].columnName, "co_l2"); //$NON-NLS-1$

        assertEquals(stringCouples[2].tableName, "table1"); //$NON-NLS-1$
        assertEquals(stringCouples[2].columnName, "col2"); //$NON-NLS-1$

    }

    // @Test
    // public void retrieveCurrentSource() {
    // String expression = "abc * $table1{col1} + $ta_ble2{co_l2} - $table1{col2}";
    // StringMetadataCouple metadataCouple = DataMapExpressionParser.retrieveCurrentSource(expression, 7); // "abc *
    // $^table1{col1} + $ta_ble2{co_l2} - $table1{col2}";
    // assertEquals(metadataCouple.tableName, "table1");
    // assertEquals(metadataCouple.columnName, "col1");
    //        
    // metadataCouple = DataMapExpressionParser.retrieveCurrentSource(expression, 7); // "abc * $^table1{col1} +
    // $ta_ble2{co_l2} - $table1{col2}";
    // assertEquals(metadataCouple.tableName, "ta_ble2");
    // assertEquals(metadataCouple.columnName, "co_l2");
    //        
    // metadataCouple = DataMapExpressionParser.retrieveCurrentSource(expression, 7); // "abc * $^table1{col1} +
    // $ta_ble2{co_l2} - $table1{col2}";
    // assertEquals(metadataCouple.tableName, "table1");
    // assertEquals(metadataCouple.columnName, "col2");
    //        
    // }

    /**
     * Test method for
     * {@link org.talend.designer.mapper.utils.DataMapExpressionParser#addTablePrefixToColumnName(String, TableEntryLocation[])}
     * .
     */
    @Test
    public void testaddTablePrefixToColumnNameString() {
        ILanguage language = new PerlLanguage();
        DataMapExpressionParser expressionParser = new DataMapExpressionParser(language);
        PerlGenerationManager gen = (PerlGenerationManager) GenerationManagerFactory.getInstance().getGenerationManager(language);

        TableEntryLocation[] locations = new TableEntryLocation[] { new TableEntryLocation("page", "content"), //$NON-NLS-1$ //$NON-NLS-2$
                new TableEntryLocation("book", "id_book"), }; //$NON-NLS-1$ //$NON-NLS-2$

        HashSet<TableEntryLocation> validLocations = new HashSet<TableEntryLocation>();
        validLocations.addAll(Arrays.asList(locations));

        String result = expressionParser.addTablePrefixToColumnName("UNIQUE_COMPONENT_NAME", "uc " //$NON-NLS-1$ //$NON-NLS-2$
                + gen.getTableColumnVariable("page", "content") + " + " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + gen.getTableColumnVariable("book", "id_book") + " - 2 * " + language.getPrefixTable() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + " book " + language.getSuffixTable() + language.getPrefixField() + "  id_book " //$NON-NLS-1$ //$NON-NLS-2$
                + language.getSuffixField(), locations, true, validLocations);
        assertEquals("uc " + gen.getTableColumnVariable("page", "page__content") + " + " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + gen.getTableColumnVariable("book", "book__id_book") + " - 2 * " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + gen.getTableColumnVariable("book", "book__id_book"), result); //$NON-NLS-1$ //$NON-NLS-2$

        locations = new TableEntryLocation[] { new TableEntryLocation("book", "id_book"), }; //$NON-NLS-1$ //$NON-NLS-2$

        // result = expressionParser.addTablePrefixToColumnName("uc $page{content} + $book{id_book} - 2 * $ book {
        // id_book }", locations);
        // assertEquals("uc $page{content} + $book{book__id_book} - 2 * $book{book__id_book}", result);

    }

    /**
     * Test method for
     * {@link org.talend.designer.mapper.utils.DataMapExpressionParser#testreplaceLocationString(String, TableEntryLocation, TableEntryLocation)}
     * .
     */
    @Test
    public void testreplaceLocationString() {
        ILanguage language = new PerlLanguage();
        DataMapExpressionParser expressionParser = new DataMapExpressionParser(language);
        // GenerationManager gen = new GenerationManager(language);

        String result = expressionParser.replaceLocation(" $ book [ id_book ] ", new TableEntryLocation("book", //$NON-NLS-1$ //$NON-NLS-2$
                "id_book"), new TableEntryLocation("book", "id_book_changed")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        System.out.println(result);
        assertEquals(" $ book [ id_book_changed ] ", result); //$NON-NLS-1$

        // String result = expressionParser.replaceLocation("uc "
        // + "$page[content] + $book[id_book] - 2 * $ book [ id_book ]", new TableEntryLocation("book", "id_book"),
        // new TableEntryLocation("book", "id_book_changed"));
        // assertEquals("uc $page[content] + $book[id_book_changed] - 2 * $book[id_book_changed]", result);
        //
        // result = expressionParser.replaceLocation("uc $page[content] + $book[id_book] - 2 * $ book [ id_book ]",
        // new TableEntryLocation("page", "content"),
        // new TableEntryLocation("page", "content_changed"));
        // assertEquals("uc $page[content_changed] + $book[id_book] - 2 * $book[id_book]", result);

    }

}
