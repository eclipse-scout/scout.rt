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
package org.eclipse.scout.rt.spec.client.gen.extract.column;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.client.ui.basic.table.IHeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.junit.Test;

/**
 * Tests for default column extractor: {@link IDocTextExtractor}<
 * {@link org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn IColumn}>.
 */
public class ColumnExtractorTest {

  /**
   * Test for {@link ColumnSortIndexExtractor#getText(org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn)}
   */
  @Test
  public void testColumnSortIndexExtractor() {
    ColumnSortIndexExtractor sortColumnProperty = new ColumnSortIndexExtractor();
    final int TEST_SORT_INDEX = 333;
    AbstractColumn c = mock(AbstractColumn.class);
    when(c.getSortIndex()).thenReturn(TEST_SORT_INDEX);
    String text = sortColumnProperty.getText(c);
    assertEquals(String.valueOf(TEST_SORT_INDEX), text);
  }

  /**
   * Test for {@link ColumnSortIndexExtractor#getText(org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn)} for
   * the
   * default sort index
   */
  @Test
  public void testSortColumnPropertyDefault() {
    assertEmptyTextForDefaultColumn(new ColumnSortIndexExtractor());
  }

  /**
   * Test for {@link ColumnWidthExtractor#getText(org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn)}
   */
  @Test
  public void testColumnWidthExtractor() {
    ColumnWidthExtractor ex = new ColumnWidthExtractor();
    final int TEST_WIDTH = 10;
    AbstractColumn c = mock(AbstractColumn.class);
    when(c.getWidth()).thenReturn(TEST_WIDTH);
    String text = ex.getText(c);
    assertEquals(String.valueOf(TEST_WIDTH), text);
  }

  /**
   * Test for {@link ColumnWidthExtractor#getText(org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn)} for
   * the default case
   */
  @Test
  public void testColumnWidthExtractorDefault() {
    assertEmptyTextForDefaultColumn(new ColumnWidthExtractor());
  }

  /**
   * Test for {@link ColumnHeaderTooltipExtractor#getText(org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn)}
   */
  @Test
  public void testColumnHeaderTooltipExtractor() {
    ColumnHeaderTooltipExtractor ex = new ColumnHeaderTooltipExtractor();
    final String TEST_TOOLTIP = "TEST";
    AbstractColumn c = mock(AbstractColumn.class);
    IHeaderCell headerCell = mock(IHeaderCell.class);
    when(c.getHeaderCell()).thenReturn(headerCell);
    when(headerCell.getTooltipText()).thenReturn(TEST_TOOLTIP);
    String text = ex.getText(c);
    assertEquals(TEST_TOOLTIP, text);
  }

  /**
   * Test for {@link ColumnHeaderTooltipExtractor#getText(org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn)}
   * for
   * the default case
   */
  @Test
  public void testColumnHeaderTooltipExtractorDefault() {
    assertEmptyTextForDefaultColumn(new ColumnHeaderTooltipExtractor());
  }

  /**
   * Test for {@link ColumnHeaderTextExtractor#getText(org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn)}
   */
  @Test
  public void testColumnHeaderTextExtractor() {
    ColumnHeaderTextExtractor ex = new ColumnHeaderTextExtractor();
    final String TEST_TEXT = "TEST";
    AbstractColumn c = mock(AbstractColumn.class);
    IHeaderCell headerCell = mock(IHeaderCell.class);
    when(c.getHeaderCell()).thenReturn(headerCell);
    when(headerCell.getText()).thenReturn(TEST_TEXT);
    String text = ex.getText(c);
    assertEquals(TEST_TEXT, text);
  }

  /**
   * Test for {@link ColumnHeaderTextExtractor#getText(org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn)} for
   * the default case
   */
  @Test
  public void testColumnHeaderTextExtractorDefault() {
    assertEmptyTextForDefaultColumn(new ColumnHeaderTextExtractor());
  }

  private void assertEmptyTextForDefaultColumn(IDocTextExtractor<IColumn<?>> ex) {
    AbstractColumn c = mock(AbstractColumn.class);
    String text = ex.getText(c);
    assertEquals("", text);
  }

}
