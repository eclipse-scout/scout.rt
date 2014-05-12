/*******************************************************************************
 * Copyright (c) 2010, 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.csv;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
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
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.utility.TestUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for {@link CsvHelper}
 */
public class CsvHelperTest {

  private CsvHelper m_csvHelper;
  private File m_testFile;

  @Before
  public void setUp() throws Exception {
    m_csvHelper = new CsvHelper();
    m_testFile = File.createTempFile("CSV_TEST", ".csv");
  }

  @After
  public void tearDown() {
    TestUtility.deleteTempFile(m_testFile);
  }

  @Test
  public void testExceptionInDataConsumer() throws Exception {
    Object[][] m_data = new Object[][]{
        {"a", "a", 123.34, "a", "a"},
        {"b", "b", 123.34, "b", "b"},
        {"d", "d", 123.34, "d", "d"}
    };
    List<String> columnNames = Arrays.asList(new String[]{"col1", "col2", "col3", "col4", "col5"});

    m_csvHelper.exportData(m_data, m_testFile, "UTF-8", columnNames, true, null, false);

    Reader reader = new FileReader(m_testFile);
    IDataConsumer dataConsumer = mock(IDataConsumer.class);
    doThrow(ProcessingException.class).when(dataConsumer).processRow(eq(2), anyListOf(Object.class)); //Throw an exception in the 2nd line
    try {
      m_csvHelper.importData(dataConsumer, reader, true, true, 1);
      fail("No exception was thrown! Expected ProcessingException");
    }
    catch (ProcessingException e) {
      String message = e.getMessage();
      assertTrue(message.contains("lineNr=2"));
      assertTrue(!message.contains("colIndex="));
      assertTrue(!message.contains("cell="));
    }
  }

  @Test
  public void testExceptionInvalidFormat() throws Exception {
    Object[][] m_data = new Object[][]{
        {"a", "a", 123.34, "a", "a"},
        {"b", "b", 123.34, "b", "b"},
        {"c", "c", "d", "c", "c"},
        {"d", "d", 123.34, "d", "d"}
    };
    List<String> columnNames = Arrays.asList(new String[]{"col1", "col2", "col3", "col4", "col5"});

    m_csvHelper.exportData(m_data, m_testFile, "UTF-8", columnNames, true, null, false);

    Reader reader = new FileReader(m_testFile);
    try {
      m_csvHelper.importData(reader, 1, Arrays.asList(new String[]{"string", "string", "float", "string", "string"}), 4);
      fail("No exception was thrown! Expected ProcessingException");
    }
    catch (ProcessingException e) {
      String message = e.getMessage();
      assertTrue(message.contains("lineNr=3"));
      assertTrue(message.contains("colIndex=2"));
      assertTrue(message.contains("cell=d"));
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
    List<String> columnNames = Arrays.asList(new String[]{"col1", "col2", "col3", "col4", "col5"});

    m_csvHelper.exportData(data, m_testFile, "UTF-8", columnNames, true, null, false);

    Reader reader = new FileReader(m_testFile);
    Object[][] result = m_csvHelper.importData(reader, 1, Arrays.asList(new String[]{"string", "string", "string", "string", "string"}), 4);
    assertArrayEquals(data, result);
  }

  @Test
  public void testExportData() throws ProcessingException, IOException {
    Object[][] data = new Object[][]{
        {"a", "a", "a", "a", "a"},
        {"b", "b", "b", "b", "b"},
        {"c", "c", "c", "c", "c"},
        {"d", "d", "d", "d", "d"}
    };
    List<String> columnNames = Arrays.asList(new String[]{"col1", "col2", "col3", "col4", "col5"});

    m_csvHelper.exportData(data, m_testFile, "UTF-8", columnNames, true, null, false);

    String content = IOUtility.getContent(new FileReader(m_testFile));
    String[] lines = content.split("\n");

    assertEquals(lines.length, 5);

    for (String line : lines) {
      String[] x = line.split(",");
      assertEquals(x.length, 5);
    }
  }
}
