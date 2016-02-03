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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

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
    assertFalse(c.isHtmlEnabled());
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
    ICellObserver observer = Mockito.mock(ICellObserver.class);

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
    c.setHtmlEnabled(true);
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
    assertTrue(copy.isHtmlEnabled());
    assertSame(observer, c.getObserver());
    Mockito.verifyZeroInteractions(observer);
  }

  @Test
  public void testConstructor_cellObserver() {
    ICellObserver observer = Mockito.mock(ICellObserver.class);

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
    assertFalse(c.isHtmlEnabled());
    assertSame(observer, c.getObserver());
    Mockito.verifyZeroInteractions(observer);
  }

  @Test
  public void testSetValue_newValue() throws Exception {
    Cell c = new Cell();
    Object value = new Object();

    ICellObserver observer = Mockito.mock(ICellObserver.class);
    Mockito.when(observer.validateValue(c, value)).thenReturn(value);

    c.setObserver(observer);

    boolean changed = c.setValue(value);
    assertTrue(changed);
    assertSame(value, c.getValue());
    Mockito.verify(observer).cellChanged(c, ICell.VALUE_BIT);
  }

  @Test
  public void testSetValue_sameValue() throws Exception {
    Cell c = new Cell();
    Object value = new Object();

    ICellObserver observer = Mockito.mock(ICellObserver.class);
    Mockito.when(observer.validateValue(c, value)).thenReturn(value);

    c.setObserver(observer);

    boolean changed = c.setValue(value);
    assertTrue(changed);

    changed = c.setValue(value);
    assertFalse(changed);
    assertSame(value, c.getValue());

    Mockito.verify(observer).cellChanged(c, ICell.VALUE_BIT);
    Mockito.verify(observer, Mockito.times(2)).validateValue(c, value);
  }

  @Test
  public void testSetValue_validateValidValue() throws Exception {
    ICellObserver observer = Mockito.mock(ICellObserver.class);
    Cell c = new Cell(observer);
    Object value = new Object();

    Mockito.when(observer.validateValue(c, value)).thenReturn(value);

    boolean changed = c.setValue(value);
    assertTrue(changed);
    assertSame(value, c.getValue());

    Mockito.verify(observer).cellChanged(c, ICell.VALUE_BIT);
    Mockito.verify(observer).validateValue(c, value);
  }

  @Test(expected = ProcessingException.class)
  public void testSetValue_validateInalidValue() throws Exception {
    ICellObserver observer = Mockito.mock(ICellObserver.class);
    Cell c = new Cell(observer);
    Object value = new Object();

    Mockito.when(observer.validateValue(c, value)).thenThrow(new ProcessingException());

    boolean changed = c.setValue(value);
    assertTrue(changed);
    assertSame(value, c.getValue());
    Mockito.verifyZeroInteractions(observer);
  }

  @Test
  public void testSetText() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.TEXT_BIT);
    String text = "text";
    c.setText(text);
    assertEquals(text, c.getText());
    Mockito.verify(observer).cellChanged(c, ICell.TEXT_BIT);
  }

  @Test
  public void testSetIconId() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.ICON_ID_BIT);
    String iconId = "iconId";
    c.setIconId(iconId);
    assertEquals(iconId, c.getIconId());
    Mockito.verify(observer).cellChanged(c, ICell.ICON_ID_BIT);
  }

  @Test
  public void testSetTooltipText_notNull() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.TOOLTIP_BIT);
    String tooltip = "tooltip";
    c.setTooltipText(tooltip);
    assertEquals(tooltip, c.getTooltipText());
    Mockito.verify(observer).cellChanged(c, ICell.TOOLTIP_BIT);
  }

  @Test
  public void testSetTooltipText_null() {
    Cell c = new Cell();
    ICellObserver observer = Mockito.mock(ICellObserver.class);
    c.setObserver(observer);
    c.setTooltipText(null);
    assertNull(c.getTooltipText());
    Mockito.verifyZeroInteractions(observer);
  }

  @Test
  public void testSetHorizontalAlignment() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.H_ALIGN_BIT);
    int hAlignment = 100;
    c.setHorizontalAlignment(hAlignment);
    assertEquals(hAlignment, c.getHorizontalAlignment());
    Mockito.verify(observer).cellChanged(c, ICell.H_ALIGN_BIT);
  }

  @Test
  public void testSetBackgroundColor() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.BG_COLOR_BIT);
    String bgColor = "eeeeee";
    c.setBackgroundColor(bgColor);
    assertEquals(bgColor, c.getBackgroundColor());
    Mockito.verify(observer).cellChanged(c, ICell.BG_COLOR_BIT);
  }

  @Test
  public void testSetForegroundColor() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.FG_COLOR_BIT);
    String fgColor = "ff0000";
    c.setForegroundColor(fgColor);
    assertEquals(fgColor, c.getForegroundColor());
    Mockito.verify(observer).cellChanged(c, ICell.FG_COLOR_BIT);
  }

  @Test
  public void testSetFont() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c, ICell.FONT_BIT);
    FontSpec font = FontSpec.parse("Arial-bold-italic-13");
    c.setFont(font);
    assertEquals(font.toPattern(), c.getFont().toPattern());
    Mockito.verify(observer).cellChanged(c, ICell.FONT_BIT);
  }

  @Test
  public void testSetEnabled() {
    Cell c = new Cell();
    ICellObserver observer = Mockito.mock(ICellObserver.class);
    c.setObserver(observer);
    c.setEnabled(true);
    assertTrue(c.isEnabled());
    c.setEnabled(false);
    assertFalse(c.isEnabled());
    c.setEnabled(true);
    assertTrue(c.isEnabled());
    Mockito.verify(observer, Mockito.times(2)).cellChanged(c, ICell.ENABLED_BIT);
  }

  @Test
  public void testSetHtmlEnabled() {
    Cell c = new Cell();
    ICellObserver observer = Mockito.mock(ICellObserver.class);
    c.setObserver(observer);
    c.setHtmlEnabled(true);
    assertTrue(c.isHtmlEnabled());
    Mockito.verify(observer).cellChanged(c, ICell.HTML_ENABLED_BIT);
  }

  @Test
  public void testSetObserver() {
    Cell c = new Cell();
    ICellObserver observer = Mockito.mock(ICellObserver.class);
    c.setObserver(observer);
    assertSame(observer, c.getObserver());
    Mockito.verifyZeroInteractions(observer);
  }

  private ICellObserver installMockObserver(Cell c, int changedBit) {
    ICellObserver observer = Mockito.mock(ICellObserver.class);
    c.setObserver(observer);
    return observer;
  }

  /**
   * When creating a cell. The errorstatus should not be set.
   * {@link Cell#setErrorStatus(org.eclipse.scout.commons.exception.IProcessingStatus)}
   */
  @Test
  public void testInitialErrorStatus() {
    Cell c = new Cell();
    Assert.assertNull(c.getErrorStatus());
  }

  /**
   * {@link Cell#setErrorStatus(org.eclipse.scout.commons.exception.IProcessingStatus)}
   */
  @Test
  public void testSetErrorStatus() {
    Cell c = new Cell();
    c.setErrorStatus(new ProcessingStatus("error", IStatus.ERROR));
    Assert.assertEquals(IProcessingStatus.ERROR, c.getErrorStatus().getSeverity());
  }

  /**
   * When creating a cell. The errorstatus should not be set.
   * {@link Cell#setErrorStatus(org.eclipse.scout.commons.exception.IProcessingStatus)}
   */
  @Test
  public void testClearErrorStatus() {
    Cell c = new Cell();
    c.setErrorStatus(new ProcessingStatus("error", IStatus.ERROR));
    c.clearErrorStatus();
    Assert.assertNull(c.getErrorStatus());
  }

  /**
   * When creating a cell. The errorstatus should not be set.
   * {@link Cell#setErrorStatus(org.eclipse.scout.commons.exception.IProcessingStatus)}
   */
  @Test
  public void testIconCellSetErrorStatus() {
    final String testIconId = "";
    Cell c = new Cell();
    c.setIconId(testIconId);
    c.setErrorStatus(new ProcessingStatus("error", IStatus.ERROR));
    c.setErrorStatus(new ProcessingStatus("error2", IStatus.ERROR));
    c.clearErrorStatus();
    c.setErrorStatus(new ProcessingStatus("error3", IStatus.ERROR));
    c.clearErrorStatus();
    Assert.assertNull(c.getErrorStatus());
    Assert.assertEquals(testIconId, c.getIconId());
  }
}
