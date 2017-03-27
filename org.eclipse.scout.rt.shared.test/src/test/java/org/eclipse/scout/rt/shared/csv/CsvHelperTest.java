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

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.BomInputStreamReader;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.mockito.ArgumentMatchers;

/**
 * JUnit tests for {@link CsvHelper}
 */
public class CsvHelperTest {

  @Rule
  public ErrorCollector m_collector = new ErrorCollector();

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

    IDataConsumer dataConsumer = mock(IDataConsumer.class);
    ProcessingException pe = new ProcessingException();
    doThrow(pe).when(dataConsumer).processRow(eq(2), ArgumentMatchers.anyList()); //Throw an exception in the 2nd line
    try (Reader reader = new FileReader(m_testFile)) {
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

    try (Reader reader = new FileReader(m_testFile)) {
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
  public void testSimpleColumnTypes() {
    List<String> columnTypes = Arrays.asList(new String[]{null, "string", "float", "date", "invalid", "Date"});
    m_csvHelper.setColumnTypes(columnTypes);
    assertEquals("string", m_csvHelper.getColumnTypes().get(0));
    assertEquals("string", m_csvHelper.getColumnTypes().get(1));
    assertEquals("float", m_csvHelper.getColumnTypes().get(2));
    assertEquals("date", m_csvHelper.getColumnTypes().get(3));
    assertEquals("string", m_csvHelper.getColumnTypes().get(4));
    assertEquals("Date", m_csvHelper.getColumnTypes().get(5));
  }

  @Test
  public void testCustomColumnType() {
    String formatTypePrefix = "date_";
    String formatPattern = "yyyy__MM__dd";
    String typeDesc = formatTypePrefix + formatPattern;
    List<String> columnTypes = Arrays.asList(new String[]{typeDesc});
    m_csvHelper.setColumnTypes(columnTypes);
    Format format = m_csvHelper.getColumnFormat(0);
    assertEquals(typeDesc, m_csvHelper.getColumnTypes().get(0));
    assertTrue(format instanceof SimpleDateFormat);
    assertEquals(formatPattern, ((SimpleDateFormat) format).toPattern());
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

    try (Reader in = new FileReader(m_testFile)) {
      Object[][] result = m_csvHelper.importData(in, 1, Arrays.asList(new String[]{"string", "string", "string", "string", "string"}), 4);
      assertArrayEquals(data, result);
    }
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

    String[] lines;
    try (Reader in = new FileReader(m_testFile)) {
      lines = IOUtility.readString(in).split("\n");
    }

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

  @Test
  public void testImportWithoutHeaderRowsAndUtfWithBomEncoding() throws IOException {
    List<Charset> unicodeCharsets = new ArrayList<>();
    unicodeCharsets.add(UTF_8);
    addIfSupported("UTF-16", unicodeCharsets);
    unicodeCharsets.add(UTF_16BE);
    unicodeCharsets.add(UTF_16LE);
    addIfSupported("UTF-32", unicodeCharsets);
    addIfSupported("UTF-32BE", unicodeCharsets);
    addIfSupported("UTF-32LE", unicodeCharsets);

    // all character sets
    List<Charset> allCharsets = new ArrayList<>();
    allCharsets.addAll(unicodeCharsets);
    allCharsets.add(US_ASCII);
    allCharsets.add(ISO_8859_1);

    for (Charset charset : allCharsets) {
      doTestImportData(false, charset);
      if (unicodeCharsets.contains(charset)) {
        doTestImportData(true, charset);
      }
    }
  }

  protected void addIfSupported(String charset, List<Charset> charsets) {
    if (Charset.isSupported(charset)) {
      charsets.add(Charset.forName(charset));
    }
  }

  protected void doTestImportData(boolean withBom, Charset charset) throws IOException {
    StringBuilder sb = new StringBuilder();
    if (withBom) {
      sb.append(BomInputStreamReader.BOM_CHAR);
    }
    sb.append('"').append("value1").append('"').append(',');
    sb.append('"').append("value2").append('"').append(',');
    sb.append('"').append("value3").append('"').append('\n');

    try (Reader reader = new BomInputStreamReader(new ByteArrayInputStream(sb.toString().getBytes(charset)), charset)) {
      Object[][] importedData = m_csvHelper.importData(reader, 0, null, 1);
      Object[][] expectedData = new Object[][]{{"value1", "value2", "value3"}};
      m_collector.checkThat(String.format("charset=%s, withBom=%b", charset.name(), withBom), importedData, CoreMatchers.equalTo(expectedData));
    }
  }
}
