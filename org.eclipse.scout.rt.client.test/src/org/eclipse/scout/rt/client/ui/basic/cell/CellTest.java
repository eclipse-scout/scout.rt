/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.cell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.junit.Test;

/**
 * JUnit tests for {@link Cell}
 */
public class CellTest {

  @Test
  public void testConstructor_default() {
    Cell c = new Cell();
    assertNull(c.getValue());
    assertNull(c.getText());
    assertNull(c.getIconId());
    assertNull(c.getTooltipText());
    assertEquals(-1, c.getHorizontalAlignment());
    assertNull(c.getBackgroundColor());
    assertNull(c.getForegroundColor());
    assertNull(c.getFont());
    assertTrue(c.isEnabled());
    assertNull(c.getObserver());
  }

  @Test
  public void testConstructor_copy() throws Exception {
    Object value = new Object();
    String text = "text";
    String iconId = "iconId";
    String tooltipText = "Tooltip";
    String bgColor = "eeeeee";
    String fgColor = "ff0000";
    FontSpec font = FontSpec.parse("Arial-bold-italic-16");
    ICellObserver observer = EasyMock.createMock(ICellObserver.class);
    EasyMock.replay(observer);

    Cell c = new Cell();
    c.setValue(value);
    c.setText(text);
    c.setIconId(iconId);
    c.setTooltipText(tooltipText);
    c.setHorizontalAlignment(100);
    c.setBackgroundColor(bgColor);
    c.setForegroundColor(fgColor);
    c.setFont(font);
    c.setEnabled(true);
    c.setObserver(observer);

    Cell copy = new Cell(c);
    assertSame(value, copy.getValue());
    assertEquals(text, copy.getText());
    assertEquals(iconId, copy.getIconId());
    assertEquals(tooltipText, copy.getTooltipText());
    assertEquals(100, c.getHorizontalAlignment());
    assertEquals(bgColor, c.getBackgroundColor());
    assertEquals(fgColor, c.getForegroundColor());
    assertEquals(font.toPattern(), c.getFont().toPattern());
    assertTrue(c.isEnabled());
    assertSame(observer, c.getObserver());
    EasyMock.verify(observer);
  }

  @Test
  public void testConstructor_cellObserver() {
    ICellObserver observer = EasyMock.createMock(ICellObserver.class);
    EasyMock.replay(observer);

    Cell c = new Cell(observer);
    assertNull(c.getValue());
    assertNull(c.getText());
    assertNull(c.getIconId());
    assertNull(c.getTooltipText());
    assertEquals(-1, c.getHorizontalAlignment());
    assertNull(c.getBackgroundColor());
    assertNull(c.getForegroundColor());
    assertNull(c.getFont());
    assertTrue(c.isEnabled());
    assertSame(observer, c.getObserver());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetValue_newValue() throws Exception {
    Cell c = new Cell();
    Object value = new Object();

    ICellObserver observer = EasyMock.createMock(ICellObserver.class);
    observer.cellChanged(c, ICell.VALUE_BIT);
    EasyMock.expectLastCall();
    EasyMock.expect(observer.validateValue(c, value)).andReturn(value);
    EasyMock.replay(observer);
    c.setObserver(observer);

    boolean changed = c.setValue(value);
    assertTrue(changed);
    assertSame(value, c.getValue());
  }

  @Test
  public void testSetValue_sameValue() throws Exception {
    Cell c = new Cell();
    Object value = new Object();

    ICellObserver observer = EasyMock.createMock(ICellObserver.class);
    observer.cellChanged(c, ICell.VALUE_BIT);
    EasyMock.expectLastCall();
    EasyMock.expect(observer.validateValue(c, value)).andReturn(value).times(2);
    EasyMock.replay(observer);
    c.setObserver(observer);

    boolean changed = c.setValue(value);
    assertTrue(changed);

    changed = c.setValue(value);
    assertFalse(changed);
    assertSame(value, c.getValue());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetValue_validateValidValue() throws Exception {
    ICellObserver observer = EasyMock.createMock(ICellObserver.class);
    Cell c = new Cell(observer);
    Object value = new Object();

    observer.cellChanged(c, ICell.VALUE_BIT);
    EasyMock.expectLastCall();
    EasyMock.expect(observer.validateValue(c, value)).andReturn(value);
    EasyMock.replay(observer);

    boolean changed = c.setValue(value);
    assertTrue(changed);
    assertSame(value, c.getValue());
    EasyMock.verify(observer);
  }

  @Test(expected = ProcessingException.class)
  public void testSetValue_validateInalidValue() throws Exception {
    ICellObserver observer = EasyMock.createMock(ICellObserver.class);
    Cell c = new Cell(observer);
    Object value = new Object();

    EasyMock.expect(observer.validateValue(c, value)).andThrow(new ProcessingException());
    EasyMock.replay(observer);

    boolean changed = c.setValue(value);
    assertTrue(changed);
    assertSame(value, c.getValue());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetText() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.TEXT_BIT);
    String text = "text";
    c.setText(text);
    assertEquals(text, c.getText());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetIconId() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.ICON_ID_BIT);
    String iconId = "iconId";
    c.setIconId(iconId);
    assertEquals(iconId, c.getIconId());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetTooltipText_notNull() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.TOOLTIP_BIT);
    String tooltip = "tooltip";
    c.setTooltipText(tooltip);
    assertEquals(tooltip, c.getTooltipText());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetTooltipText_null() {
    Cell c = new Cell();
    ICellObserver observer = EasyMock.createMock(ICellObserver.class);
    EasyMock.replay(observer);
    c.setObserver(observer);
    c.setTooltipText(null);
    assertNull(c.getTooltipText());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetHorizontalAlignment() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.H_ALIGN_BIT);
    int hAlignment = 100;
    c.setHorizontalAlignment(hAlignment);
    assertEquals(hAlignment, c.getHorizontalAlignment());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetBackgroundColor() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.BG_COLOR_BIT);
    String bgColor = "eeeeee";
    c.setBackgroundColor(bgColor);
    assertEquals(bgColor, c.getBackgroundColor());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetForegroundColor() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.FG_COLOR_BIT);
    String fgColor = "ff0000";
    c.setForegroundColor(fgColor);
    assertEquals(fgColor, c.getForegroundColor());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetFont() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.FONT_BIT);
    FontSpec font = FontSpec.parse("Arial-bold-italic-13");
    c.setFont(font);
    assertEquals(font.toPattern(), c.getFont().toPattern());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetEnabled() {
    Cell c = new Cell();
    ICellObserver observer = EasyMock.createMock(ICellObserver.class);
    observer.cellChanged(c, ICell.ENABLED_BIT);
    EasyMock.expectLastCall().times(2);
    EasyMock.replay(observer);
    c.setObserver(observer);
    c.setEnabled(true);
    assertTrue(c.isEnabled());
    c.setEnabled(false);
    assertFalse(c.isEnabled());
    c.setEnabled(true);
    assertTrue(c.isEnabled());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetObserver() {
    Cell c = new Cell();
    ICellObserver observer = EasyMock.createMock(ICellObserver.class);
    EasyMock.replay(observer);
    c.setObserver(observer);
    assertSame(observer, c.getObserver());
    EasyMock.verify(observer);
  }

  private ICellObserver installMockObserver(Cell c, int changedBit) {
    ICellObserver observer = EasyMock.createMock(ICellObserver.class);
    observer.cellChanged(c, changedBit);
    EasyMock.expectLastCall();
    EasyMock.replay(observer);
    c.setObserver(observer);
    return observer;
  }
}
