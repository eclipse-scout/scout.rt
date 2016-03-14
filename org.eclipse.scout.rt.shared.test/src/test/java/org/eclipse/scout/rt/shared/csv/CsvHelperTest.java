/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.csv;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for {@link CsvHelper}
 */
public class CsvHelperTest {

  private CsvHelper m_csvHelper;
  private File m_testFile;
  private List<String> m_columnNames = Arrays.asList(new String[]{"col1", "col2", "col3", "col4", "col5"});

  @Before
  public void setUp() throws Exception {
    m_csvHelper = new CsvHelper();
    m_testFile = File.createTempFile("CSV_TEST", ".csv");
  }

  @After
  public void tearDown() {
    IOUtility.deleteFile(m_testFile);
  }

  @Test
  public void testExceptionInDataConsumer() throws Exception {
    Object[][] data = new Object[][]{
        {"a", "a", 123.34, "a", "a"},
        {"b", "b", 123.34, "b", "b"},
        {"d", "d", 123.34, "d", "d"}
    };
    export(data);

    Reader reader = new FileReader(m_testFile);
    IDataConsumer dataConsumer = mock(IDataConsumer.class);
    ProcessingException pe = new ProcessingException();
    doThrow(pe).when(dataConsumer).processRow(eq(2), anyListOf(Object.class)); //Throw an exception in the 2nd line
    try {
      m_csvHelper.importData(dataConsumer, reader, true, true, 1);
      fail("No exception was thrown! Expected ProcessingException");
    }
    catch (ProcessingException e) {
      Set<String> contextInfos = CollectionUtility.hashSet(e.getContextInfos());
      assertTrue(contextInfos.remove("lineNr=2"));
      String fullMessage = e.getDisplayMessage() + " " + Arrays.asList(e.getStackTrace());
      assertEquals("expected a single context message: " + contextInfos + " full Exception Message : " + fullMessage, 0, contextInfos.size());
    }
  }

  @Test
  public void testExceptionInvalidFormat() throws Exception {
    Object[][] data = new Object[][]{
        {"a", "a", 123.34, "a", "a"},
        {"b", "b", 123.34, "b", "b"},
        {"c", "c", "d", "c", "c"},
        {"d", "d", 123.34, "d", "d"}
    };
    export(data);

    Reader reader = new FileReader(m_testFile);
    try {
      m_csvHelper.importData(reader, 1, Arrays.asList(new String[]{"string", "string", "float", "string", "string"}), 4);
      fail("No exception was thrown! Expected ProcessingException");
    }
    catch (ProcessingException e) {
      HashSet<String> contextInfos = CollectionUtility.hashSet(e.getContextInfos());
      assertTrue(contextInfos.contains("lineNr=3"));
      assertTrue(contextInfos.contains("colIndex=2"));
      assertTrue(contextInfos.contains("cell=d"));
    }
  }

  @Test
  public void testImportData() throws Exception {
    Object[][] data = new Object[][]{
        {"a", "a", "a", "a", "a"},
        {"b", "b", "b", "b", "b"},
        {"c", "c", "c", "c", "c"},
        {"d", "d", "d", "d", "d"}
    };

    export(data);

    Reader reader = new FileReader(m_testFile);
    Object[][] result = m_csvHelper.importData(reader, 1, Arrays.asList(new String[]{"string", "string", "string", "string", "string"}), 4);
    assertArrayEquals(data, result);
  }

  @Test
  public void testExportData() throws IOException {
    Object[][] data = new Object[][]{
        {"a", "a", "a", "a", "a"},
        {"b", "b", "b", "b", "b"},
        {"c", "c", "c", "c", "c"},
        {"d", "d", "d", "d", "d"}
    };

    export(data);

    String content = IOUtility.getContent(new FileReader(m_testFile));
    String[] lines = content.split("\n");

    assertEquals(lines.length, 5);

    for (String line : lines) {
      String[] x = line.split(",");
      assertEquals(x.length, 5);
    }
  }

  private void export(Object[][] data) {
    m_csvHelper.exportData(data, m_testFile, StandardCharsets.UTF_8.name(), m_columnNames, true, null, false);
  }

  /**
   * This test methods verifies the default settings of the csv helper regarding separator, text delimiter and line
   * separator. If in any case these defaults change, migration notes must be provided.
   */
  @Test
  public void testDefaultSettings() {
    // empty constructor
    CsvHelper csvHelper = new CsvHelper();
    assertEquals("Default separator character is wrong", (char) ',', csvHelper.getSeparatorChar());
    assertEquals("Default text delimiter character is wrong", (char) '"', csvHelper.getTextDelimiterChar());
    assertEquals("Default line separator is wrong", "\n", csvHelper.getLineSeparator());

    // non-empty constructor (different default value behavior: separator , vs. ;)
    csvHelper = new CsvHelper(null, null, null, null);
    assertEquals("Default separator character is wrong", (char) ';', csvHelper.getSeparatorChar());
    assertEquals("Default text delimiter character is wrong", (char) '"', csvHelper.getTextDelimiterChar());
    assertEquals("Default line separator is wrong", "\n", csvHelper.getLineSeparator());

    csvHelper = new CsvHelper(null, (char) 0, (char) 0, null);
    assertEquals("Default separator character is wrong", (char) ';', csvHelper.getSeparatorChar());
    assertEquals("Default text delimiter character is wrong", (char) '"', csvHelper.getTextDelimiterChar());
    assertEquals("Default line separator is wrong", "\n", csvHelper.getLineSeparator());
  }

  @Test
  public void testExportDataWithTextEncode() {
    StringWriter writer;

    // no special characters -> not encoded
    writer = new StringWriter();
    m_csvHelper.exportDataRow(new Object[]{"ab", "bc", "cd"}, writer, true);
    assertEquals("ab,bc,cd\n", writer.toString());

    // text containing a character that is neither separator, text delimiter nor line break -> not encode
    writer = new StringWriter();
    m_csvHelper.exportDataRow(new Object[]{";ab", "b;c", "cd;"}, writer, true);
    assertEquals(";ab,b;c,cd;\n", writer.toString());

    // text containing separator -> encoded
    writer = new StringWriter();
    m_csvHelper.exportDataRow(new Object[]{",ab", "b,c", "cd,"}, writer, true);
    assertEquals("\",ab\",\"b,c\",\"cd,\"\n", writer.toString());

    // text containing text delimiter -> encoded (text delimiter inside text is doubled)
    writer = new StringWriter();
    m_csvHelper.exportDataRow(new Object[]{"\"ab", "b\"c", "cd\""}, writer, true);
    assertEquals("\"\"\"ab\",\"b\"\"c\",\"cd\"\"\"\n", writer.toString());

    // text containing line separator -> not encoded (by default)
    writer = new StringWriter();
    m_csvHelper.exportDataRow(new Object[]{"\nab", "b\nc", "cd\n"}, writer, true);
    assertEquals("\nab,b\nc,cd\n\n", writer.toString());

    // text containing line separator with line separator encoding enabled -> encoded
    CsvHelper csvHelper = new CsvHelper();
    csvHelper.setEncodeLineSeparator(true);
    writer = new StringWriter();
    csvHelper.exportDataRow(new Object[]{"\nab", "b\nc", "cd\n"}, writer, true);
    assertEquals("\"\nab\",\"b\nc\",\"cd\n\"\n", writer.toString());
  }

  @Test
  public void testGetIgnoredColumns() {
    // ignored column is null -> returned ignored list is empty
    List<Boolean> ignoredColumns = m_csvHelper.getIgnoredColumns();
    assertTrue(ignoredColumns.isEmpty());

    // empty list
    m_csvHelper.setColumnNames(Collections.<String> emptyList());
    ignoredColumns = m_csvHelper.getIgnoredColumns();
    assertNotNull(ignoredColumns);
    assertTrue(ignoredColumns.isEmpty());

    // multiple without ignored columns
    m_csvHelper.setColumnNames(m_columnNames);
    ignoredColumns = m_csvHelper.getIgnoredColumns();
    assertEquals(m_columnNames.size(), ignoredColumns.size());
    for (Boolean column : ignoredColumns) {
      assertFalse(column);
    }

    // multiple with ignored columns
    List<String> columnNames = Arrays.asList("col1", CsvHelper.IGNORED_COLUMN_NAME, "col2", CsvHelper.IGNORED_COLUMN_NAME, CsvHelper.IGNORED_COLUMN_NAME, "col3");
    m_csvHelper.setColumnNames(columnNames);
    ignoredColumns = m_csvHelper.getIgnoredColumns();
    assertEquals(columnNames.size(), ignoredColumns.size());
    assertFalse(ignoredColumns.get(0));
    assertTrue(ignoredColumns.get(1));
    assertFalse(ignoredColumns.get(2));
    assertTrue(ignoredColumns.get(3));
    assertTrue(ignoredColumns.get(4));
    assertFalse(ignoredColumns.get(5));
  }
}
