/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.lookup;

import static org.junit.Assert.*;

import java.io.IOException;

import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
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
    ILookupRow<String> row = new LookupRow<>((String) null, null);
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
  public void testActive() {
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
    ILookupRow<Long> row = new LookupRow<>(cells, Long.class);
    assertEquals(Long.valueOf(0L), row.getKey());
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
    ILookupRow<Long> row = new LookupRow<>(cells, Long.class);
    assertEquals(Long.valueOf(0L), row.getKey());
    assertNull(row.getText());
    assertNull(row.getIconId());
  }

  /**
   * Tests, that the lookup row is still the same after serialization/deserialization.
   */
  @Test
  public void testSerializeDeserialize() throws IOException, ClassNotFoundException {
    ILookupRow<String> row = new LookupRow<>("key", "text");
    byte[] data = serialize(row);
    Object obj = deserialize(data);

    assertTrue(obj instanceof LookupRow);
    LookupRow deserializedRow = (LookupRow) obj;
    assertEquals("key", deserializedRow.getKey());
    assertEquals("text", deserializedRow.getText());
  }

  private Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
    return SerializationUtility.createObjectSerializer().deserialize(data, Object.class);
  }

  private byte[] serialize(ILookupRow<String> row) throws IOException {
    return SerializationUtility.createObjectSerializer().serialize(row);
  }
}
