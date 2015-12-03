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
package org.eclipse.scout.rt.shared.services.lookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.junit.Test;

/**
 * Tests for {@link LookupRow}
 */
public class LookupRowTest {

  /**
   * Tests initialization of a lookup row with null values
   */
  @Test
  public void testCreateEmpty() {
    ILookupRow<String> row = new LookupRow<String>((String) null, (String) null);
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
    ILookupRow<String> row = new LookupRow<>("key", "text")
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

  @Test
  public void testActive() throws Exception {
    ILookupRow<String> row = new LookupRow<>("key", "text");
    row.withActive(true);
    row.withActive(false);
    row.withActive(true);
    assertTrue(row.isActive());
  }

  @Test
  public void testCreateWithArray() {
    Object[] cells = new Object[]{
        0L,
        "text",
        "testIcon"
    };
    ILookupRow<String> row = new LookupRow<>(cells, Long.class);
    assertEquals(0L, row.getKey());
    assertEquals("text", row.getText());
    assertEquals("testIcon", row.getIconId());
  }

  @Test
  public void testCreateWithNullValues() {
    Object[] cells = new Object[]{
        0L,
        null,
        null
    };
    ILookupRow<String> row = new LookupRow<>(cells, Long.class);
    assertEquals(0L, row.getKey());
    assertNull(row.getText());
    assertNull(row.getIconId());
  }

  /**
   * Tests, that the lookup row is still the same after serialization/deserialization.
   */
  @Test
  public void testSerializeDeserialize() throws IOException, ClassNotFoundException {
    ILookupRow<String> row = new LookupRow<String>("key", "text");
    byte[] data = serialize(row);
    Object obj = deserialize(data);

    assertTrue(obj instanceof LookupRow);
    LookupRow deserializedRow = (LookupRow) obj;
    assertEquals("key", deserializedRow.getKey());
    assertEquals("text", deserializedRow.getText());
  }

  private Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
    try (ByteArrayInputStream bin = new ByteArrayInputStream(data);
        ObjectInputStream oin = new ObjectInputStream(bin)) {
      return oin.readObject();
    }
  }

  private byte[] serialize(ILookupRow<String> row) throws IOException {
    try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout)) {
      oout.writeObject(row);
      return bout.toByteArray();
    }
  }

}
