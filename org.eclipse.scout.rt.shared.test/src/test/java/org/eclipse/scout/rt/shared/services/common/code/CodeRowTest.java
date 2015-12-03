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
package org.eclipse.scout.rt.shared.services.common.code;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.junit.Test;

/**
 * Tests for {@link CodeRow}
 */
public class CodeRowTest {

  private static final long ONE = 1L;
  private static final double ONE_D = 1.0;
  private static final String EXT = "ext";
  private static final String PARENT = "parent";
  private static final String BACKGROUND = "background";
  private static final String FOREGROUND = "foreground";
  private static final String CSS_CLASS = "cssClass";
  private static final String TEST_TOOLTIP = "testTooltip";
  private static final String TEST_ICON = "testIcon";
  private static final String TEXT = "text";
  private static final String KEY = "key";
  private Object[] OBJECT_ARRAY = new Object[]{KEY, TEXT, TEST_ICON, TEST_TOOLTIP, BACKGROUND, FOREGROUND,
      "font", ONE, PARENT, EXT, ONE, ONE, ONE, ONE_D};

  /**
   * Tests initialization of a lookup row with null values
   */
  @Test
  public void testCreateEmpty() {
    ICodeRow<String> row = new CodeRow<String>((String) null, (String) null);
    assertNull(row.getKey());
    assertNull(row.getText());
    assertNull(row.getTooltipText());
    assertNull(row.getFont());
    assertNull(row.getIconId());
    assertNull(row.getForegroundColor());
    assertNull(row.getBackgroundColor());
    assertNull(row.getAdditionalTableRowData());
    assertNull(row.getParentKey());
    assertNull(row.getCssClass());
    //
    assertTrue(row.isActive());
    assertTrue(row.isEnabled());
  }

  /**
   * Tests initialization of a lookup row with non-null values
   */
  @Test
  public void testCreateNonEmpty() {
    AbstractTableRowData bean = new AbstractTableRowData() {
      private static final long serialVersionUID = 1L;
    };
    ICodeRow<String> row = new CodeRow<>(KEY, TEXT)
        .withIconId(TEST_ICON)
        .withTooltipText(TEST_TOOLTIP)
        .withCssClass(CSS_CLASS)
        .withForegroundColor(FOREGROUND)
        .withBackgroundColor(BACKGROUND)
        .withParentKey(PARENT)
        .withAdditionalTableRowData(bean)
        .withActive(false)
        .withEnabled(false);
    assertEquals(KEY, row.getKey());
    assertEquals(TEXT, row.getText());
    assertEquals(TEST_ICON, row.getIconId());
    assertEquals(TEST_TOOLTIP, row.getTooltipText());
    assertEquals(CSS_CLASS, row.getCssClass());
    assertEquals(FOREGROUND, row.getForegroundColor());
    assertEquals(BACKGROUND, row.getBackgroundColor());
    assertEquals(PARENT, row.getParentKey());
    assertEquals(bean, row.getAdditionalTableRowData());
    assertFalse(row.isActive());
    assertFalse(row.isEnabled());
  }

  @Test
  public void testCreateWithArray() throws Exception {
    ICodeRow<String> row = new CodeRow<String>(OBJECT_ARRAY, String.class);
    assertCodeRowCreatedWithObjectArray(row);

  }

  private void assertCodeRowCreatedWithObjectArray(ICodeRow<String> row) {
    assertEquals(KEY, row.getKey());
    assertEquals(TEXT, row.getText());
    assertEquals(TEST_ICON, row.getIconId());
    assertEquals(TEST_TOOLTIP, row.getTooltipText());
    assertEquals(FOREGROUND, row.getForegroundColor());
    assertEquals(BACKGROUND, row.getBackgroundColor());
    assertTrue(row.isActive());
    assertEquals(PARENT, row.getParentKey());
    assertEquals(EXT, row.getExtKey());
    assertEquals(ONE, row.getValue());
    assertTrue(row.isEnabled());
    assertEquals(ONE, row.getPartitionId());
  }

  @Test
  public void testCreateWithSubType() throws Exception {
    ICodeRow<String> row = new StringCodeRow(OBJECT_ARRAY);
    assertCodeRowCreatedWithObjectArray(row);
  }

  private static class StringCodeRow extends CodeRow<String> {
    private static final long serialVersionUID = 1L;

    public StringCodeRow(Object[] cells) {
      super(cells);
    }
  }

}
