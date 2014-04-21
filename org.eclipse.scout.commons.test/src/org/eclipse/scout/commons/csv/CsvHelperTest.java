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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.utility.TestUtility;
import org.junit.Test;

/**
 * JUnit tests for {@link CsvHelper}
 */
public class CsvHelperTest {

  @Test
  public void testExportData() throws ProcessingException, IOException {

    CsvHelper helper = new CsvHelper();

    Object[][] data = new Object[][]{
        {"a", "a", "a", "a", "a"},
        {"b", "b", "b", "b", "b"},
        {"c", "c", "c", "c", "c"},
        {"d", "d", "d", "d", "d"}
    };
    List<String> columnNames = Arrays.asList(new String[]{"col1", "col2", "col3", "col4", "col5"});

    File testFile = null;
    try {
      testFile = File.createTempFile("CSV_TEST", ".csv");

      helper.exportData(data, testFile, "UTF-8", columnNames, true, null, false);

      String content = IOUtility.getContent(new FileReader(testFile));
      String[] lines = content.split("\n");

      assertEquals(lines.length, 5);

      for (String line : lines) {
        String[] x = line.split(",");
        assertEquals(x.length, 5);
      }
    }
    finally {
      TestUtility.deleteTempFile(testFile);
    }
  }
}
