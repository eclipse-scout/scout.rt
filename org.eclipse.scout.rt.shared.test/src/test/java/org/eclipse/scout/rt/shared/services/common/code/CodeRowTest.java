/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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
    ICodeRow<String> row = new CodeRow<>("key", "text")
        .withIconId("testIcon")
        .withTooltipText("testTooltip")
        .withCssClass("cssClass")
        .withForegroundColor("foreground")
        .withBackgroundColor("background")
        .withParentKey("parent")
        .withAdditionalTableRowData(bean)
        .withActive(false)
        .withEnabled(false);
    assertEquals("key", row.getKey());
    assertEquals("text", row.getText());
    assertEquals("testIcon", row.getIconId());
    assertEquals("testTooltip", row.getTooltipText());
    assertEquals("cssClass", row.getCssClass());
    assertEquals("foreground", row.getForegroundColor());
    assertEquals("background", row.getBackgroundColor());
    assertEquals("parent", row.getParentKey());
    assertEquals(bean, row.getAdditionalTableRowData());
    assertFalse(row.isActive());
    assertFalse(row.isEnabled());
  }
}
