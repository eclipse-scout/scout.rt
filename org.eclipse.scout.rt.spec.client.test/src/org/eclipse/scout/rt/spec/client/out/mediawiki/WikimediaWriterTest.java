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
package org.eclipse.scout.rt.spec.client.out.mediawiki;

import java.io.IOException;
import java.io.StringWriter;

import org.eclipse.scout.rt.spec.client.out.DocTable;
import org.eclipse.scout.rt.spec.client.out.IDocTable;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link WikimediaFormWriter}
 */
public class WikimediaWriterTest {

  /**
   * Test for {@link WikimediaTableWriter#appendTable(IDocTable)}
   * 
   * @throws IOException
   */
  @Test
  public void appendTableTest() throws IOException {
    String expectedTable = getTestTableWiki();
    IDocTable testTable = getTestTable(true);
    verifyAppendTable(testTable, expectedTable);
  }

  /**
   * Verifies that a test table is written as expected with {@link MediaWikiTableWriter#appendTable(IDocTable)}
   * 
   * @param testTable
   *          test {@link IDocTable}
   * @param expectedTable
   *          Expected result in wikimedia format as {@link String}
   * @throws IOException
   */
  private void verifyAppendTable(IDocTable testTable, String expectedTable) throws IOException {
    StringWriter testWriter = new StringWriter();
    new MediawikiTableWriter(testWriter, null).appendTable(testTable);
    String actualTable = testWriter.toString();
    Assert.assertEquals(expectedTable, actualTable);
  }

  /**
   * Test for {@link MediaWikiTableWriter#appendTableTransposed(IDocTable)}
   * 
   * @throws IOException
   */
  @Test
  public void appendTableTransposedTest() throws IOException {
    IDocTable testTable = getTestTable(true);
    String expectedTable = getTestTableWikiTransposed(true);

    verifyAppendTableTransposed(testTable, expectedTable);
  }

  /**
   * Verifies that a test table is written as expected with
   * {@link MediaWikiTableWriter#appendTableTransposed(IDocTable)}
   * 
   * @param testTable
   *          test {@link IDocTable}
   * @param expectedTable
   *          Expected result in wikimedia format as {@link String}
   * @throws IOException
   */
  private void verifyAppendTableTransposed(IDocTable testTable, String expectedTable) throws IOException {
    StringWriter testWriter = new StringWriter();
    new MediawikiTableWriter(testWriter, null).appendTableTransposed(testTable);
    String actualTable = testWriter.toString();
    Assert.assertEquals(expectedTable, actualTable);
  }

  private IDocTable getTestTable(boolean headers) {
    String[] header = new String[]{"h1", "h2"};
    String[][] cells = new String[][]{{"a1", "a2"}, {"b1", "b2"}, {"c1", "c2"}};
    if (headers) {
      return new DocTable(header, cells);
    }
    return new DocTable(null, cells);
  }

  private IDocTable getTestTableNoHeaders() {
    String[][] cells = new String[][]{{"a1", "a2"}, {"b1", "b2"}, {"c1", "c2"}};
    return new DocTable(null, cells);
  }

  private String getTestTableWiki() {
    return ""
        + "{|" + MediawikiTableWriter.NEWLINE
        + "!h1" + MediawikiTableWriter.NEWLINE
        + "!h2" + MediawikiTableWriter.NEWLINE
        + "|-" + MediawikiTableWriter.NEWLINE
        + "| a1" + MediawikiTableWriter.NEWLINE
        + "| a2" + MediawikiTableWriter.NEWLINE
        + "|-" + MediawikiTableWriter.NEWLINE
        + "| b1" + MediawikiTableWriter.NEWLINE
        + "| b2" + MediawikiTableWriter.NEWLINE
        + "|-" + MediawikiTableWriter.NEWLINE
        + "| c1" + MediawikiTableWriter.NEWLINE
        + "| c2" + MediawikiTableWriter.NEWLINE
        + "|}" + MediawikiTableWriter.NEWLINE;
  }

  private String getTestTableWikiTransposed(boolean headers) {
    if (headers) {
      return ""
          + "{|" + MediawikiTableWriter.NEWLINE
          + "|-" + MediawikiTableWriter.NEWLINE
          + "!h1" + MediawikiTableWriter.NEWLINE
          + "| a1" + MediawikiTableWriter.NEWLINE
          + "| b1" + MediawikiTableWriter.NEWLINE
          + "| c1" + MediawikiTableWriter.NEWLINE
          + "|-" + MediawikiTableWriter.NEWLINE
          + "!h2" + MediawikiTableWriter.NEWLINE
          + "| a2" + MediawikiTableWriter.NEWLINE
          + "| b2" + MediawikiTableWriter.NEWLINE
          + "| c2" + MediawikiTableWriter.NEWLINE
          + "|}" + MediawikiTableWriter.NEWLINE;
    }
    return ""
        + "{|" + MediawikiTableWriter.NEWLINE
        + "|-" + MediawikiTableWriter.NEWLINE
        + "| a1" + MediawikiTableWriter.NEWLINE
        + "| b1" + MediawikiTableWriter.NEWLINE
        + "| c1" + MediawikiTableWriter.NEWLINE
        + "|-" + MediawikiTableWriter.NEWLINE
        + "| a2" + MediawikiTableWriter.NEWLINE
        + "| b2" + MediawikiTableWriter.NEWLINE
        + "| c2" + MediawikiTableWriter.NEWLINE
        + "|}" + MediawikiTableWriter.NEWLINE;
  }

}
