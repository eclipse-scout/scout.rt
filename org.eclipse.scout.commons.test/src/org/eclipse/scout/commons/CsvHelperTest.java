/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.scout.commons.csv.CsvHelper;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.junit.Test;

public class CsvHelperTest {

  /**
   * ticket 89708
   */
  @Test
  public void testExportData() throws ProcessingException, IOException {

    CsvHelper helper = new CsvHelper();

    Object[][] data = new Object[][]{
        {"a", "a", "a", "a", "a"},
        {"b", "b", "b", "b", "b"},
        {"c", "c", "c", "c", "c"},
        {"d", "d", "d", "d", "d"}
    };

    List<String> columnNames = new ArrayList<String>();
    columnNames = Arrays.asList(new String[]{"col1", "col2", "col3", "col4", "col5"});

    File testFile = File.createTempFile("CSV_TEST", ".csv");

    helper.exportData(data, testFile, "UTF-8", columnNames, true, null, false);

    String content = IOUtility.getContent(new FileReader(testFile));
    String[] lines = content.split("\n");

    Assert.assertEquals(lines.length, 5);

    for (String line : lines) {
      String[] x = line.split(",");
      Assert.assertEquals(x.length, 5);
    }
  }
}
