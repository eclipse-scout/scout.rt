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

import org.easymock.EasyMock;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link Cell}
 */
public class CellTest {

  @Test
  public void testConstructor_default() {
    Cell c = new Cell();
    Assert.assertNull(c.getValue());
    Assert.assertNull(c.getText());
    Assert.assertNull(c.getIconId());
    Assert.assertNull(c.getTooltipText());
    Assert.assertEquals(-1, c.getHorizontalAlignment());
    Assert.assertNull(c.getBackgroundColor());
    Assert.assertNull(c.getForegroundColor());
    Assert.assertNull(c.getFont());
    Assert.assertTrue(c.isEnabled());
    Assert.assertNull(c.getObserver());
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
    Assert.assertSame(value, copy.getValue());
    Assert.assertEquals(text, copy.getText());
    Assert.assertEquals(iconId, copy.getIconId());
    Assert.assertEquals(tooltipText, copy.getTooltipText());
    Assert.assertEquals(100, c.getHorizontalAlignment());
    Assert.assertEquals(bgColor, c.getBackgroundColor());
    Assert.assertEquals(fgColor, c.getForegroundColor());
    Assert.assertEquals(font.toPattern(), c.getFont().toPattern());
    Assert.assertTrue(c.isEnabled());
    Assert.assertSame(observer, c.getObserver());
    EasyMock.verify(observer);
  }

  @Test
  public void testConstructor_cellObserver() {
    ICellObserver observer = EasyMock.createMock(ICellObserver.class);
    EasyMock.replay(observer);

    Cell c = new Cell(observer);
    Assert.assertNull(c.getValue());
    Assert.assertNull(c.getText());
    Assert.assertNull(c.getIconId());
    Assert.assertNull(c.getTooltipText());
    Assert.assertEquals(-1, c.getHorizontalAlignment());
    Assert.assertNull(c.getBackgroundColor());
    Assert.assertNull(c.getForegroundColor());
    Assert.assertNull(c.getFont());
    Assert.assertTrue(c.isEnabled());
    Assert.assertSame(observer, c.getObserver());
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
    Assert.assertTrue(changed);
    Assert.assertSame(value, c.getValue());
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
    Assert.assertTrue(changed);

    changed = c.setValue(value);
    Assert.assertFalse(changed);
    Assert.assertSame(value, c.getValue());
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
    Assert.assertTrue(changed);
    Assert.assertSame(value, c.getValue());
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
    Assert.assertTrue(changed);
    Assert.assertSame(value, c.getValue());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetText() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.TEXT_BIT);
    String text = "text";
    c.setText(text);
    Assert.assertEquals(text, c.getText());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetIconId() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.ICON_ID_BIT);
    String iconId = "iconId";
    c.setIconId(iconId);
    Assert.assertEquals(iconId, c.getIconId());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetTooltipText_notNull() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.TOOLTIP_BIT);
    String tooltip = "tooltip";
    c.setTooltipText(tooltip);
    Assert.assertEquals(tooltip, c.getTooltipText());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetTooltipText_null() {
    Cell c = new Cell();
    ICellObserver observer = EasyMock.createMock(ICellObserver.class);
    EasyMock.replay(observer);
    c.setObserver(observer);
    c.setTooltipText(null);
    Assert.assertNull(c.getTooltipText());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetHorizontalAlignment() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.H_ALIGN_BIT);
    int hAlignment = 100;
    c.setHorizontalAlignment(hAlignment);
    Assert.assertEquals(hAlignment, c.getHorizontalAlignment());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetBackgroundColor() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.BG_COLOR_BIT);
    String bgColor = "eeeeee";
    c.setBackgroundColor(bgColor);
    Assert.assertEquals(bgColor, c.getBackgroundColor());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetForegroundColor() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.FG_COLOR_BIT);
    String fgColor = "ff0000";
    c.setForegroundColor(fgColor);
    Assert.assertEquals(fgColor, c.getForegroundColor());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetFont() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.FONT_BIT);
    FontSpec font = FontSpec.parse("Arial-bold-italic-13");
    c.setFont(font);
    Assert.assertEquals(font.toPattern(), c.getFont().toPattern());
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
    Assert.assertTrue(c.isEnabled());
    c.setEnabled(false);
    Assert.assertFalse(c.isEnabled());
    c.setEnabled(true);
    Assert.assertTrue(c.isEnabled());
    EasyMock.verify(observer);
  }

  @Test
  public void testSetObserver() {
    Cell c = new Cell();
    ICellObserver observer = EasyMock.createMock(ICellObserver.class);
    EasyMock.replay(observer);
    c.setObserver(observer);
    Assert.assertSame(observer, c.getObserver());
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
