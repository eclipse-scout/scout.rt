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
package org.eclipse.scout.rt.client.ui.basic.cell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.client.ui.form.fields.ParsingFailedStatus;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.MultiStatus;
import org.eclipse.scout.rt.platform.status.Status;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
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
    assertNull(c.getObserver());
    assertFalse(c.isHtmlEnabled());
    assertFalse(c.isMandatory());
    assertNull(c.getCssClass());
  }

  /**
   * When a new Cell is crated as a copy <br>
   * , all values should be copied and that there are no calls to an observer
   */
  @Test
  public void testConstructor_copy() throws Exception {
    Object value = new Object();
    String text = "text";
    String iconId = "iconId";
    String tooltipText = "Tooltip";
    String bgColor = "eeeeee";
    String fgColor = "ff0000";
    String cssClass = "myClass";
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
    c.setHtmlEnabled(true);
    c.setCssClass(cssClass);
    c.setMandatory(true);
    c.setObserver(observer);

    Cell copy = new Cell(c);
    assertSame(value, copy.getValue());
    assertEquals(text, copy.getText());
    assertEquals(iconId, copy.getIconId());
    assertEquals(tooltipText, copy.getTooltipText());
    assertEquals(cssClass, copy.getCssClass());
    assertTrue(copy.isHtmlEnabled());
    assertTrue(copy.isMandatory());

    assertEquals(100, c.getHorizontalAlignment());
    assertEquals(bgColor, c.getBackgroundColor());
    assertEquals(fgColor, c.getForegroundColor());
    assertEquals(cssClass, c.getCssClass());
    assertEquals(font.toPattern(), c.getFont().toPattern());
    assertTrue(c.isMandatory());
    assertTrue(c.isHtmlEnabled());

    assertSame(observer, c.getObserver());
    verifyZeroInteractions(observer);
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
    assertNull(c.getCssClass());
    assertFalse(c.isHtmlEnabled());
    assertSame(observer, c.getObserver());
    verifyZeroInteractions(observer);
  }

  @Test
  public void testSetValue_newValue() throws Exception {
    Cell c = new Cell();
    Object value = new Object();

    ICellObserver observer = mock(ICellObserver.class);
    when(observer.validateValue(c, value)).thenReturn(value);

    c.setObserver(observer);

    boolean changed = c.setValue(value);
    assertTrue(changed);
    assertSame(value, c.getValue());
    verify(observer).cellChanged(c, ICell.VALUE_BIT);
  }

  @Test
  public void testSetValue_sameValue() throws Exception {
    Cell c = new Cell();
    Object value = new Object();

    ICellObserver observer = Mockito.mock(ICellObserver.class);
    when(observer.validateValue(c, value)).thenReturn(value);

    c.setObserver(observer);

    boolean changed = c.setValue(value);
    assertTrue(changed);

    changed = c.setValue(value);
    assertFalse(changed);
    assertSame(value, c.getValue());

    verify(observer).cellChanged(c, ICell.VALUE_BIT);
    verify(observer, Mockito.times(2)).validateValue(c, value);
  }

  @Test
  public void testSetValue_validateValidValue() throws Exception {
    ICellObserver observer = Mockito.mock(ICellObserver.class);
    Cell c = new Cell(observer);
    Object value = new Object();

    when(observer.validateValue(c, value)).thenReturn(value);

    boolean changed = c.setValue(value);
    assertTrue(changed);
    assertSame(value, c.getValue());

    verify(observer).cellChanged(c, ICell.VALUE_BIT);
    verify(observer).validateValue(c, value);
  }

  @Test
  public void testSetValue_validateInalidValue() throws Exception {
    ICellObserver observer = Mockito.mock(ICellObserver.class);
    Cell c = new Cell(observer);
    Object value = new Object();

    when(observer.validateValue(c, value)).thenThrow(new ProcessingException());

    boolean changed = c.setValue(value);
    assertTrue(changed);
    assertSame(value, c.getValue());
    assertFalse(c.isContentValid());
  }

  @Test
  public void testSetText() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c);
    String text = "text";
    c.setText(text);
    assertEquals(text, c.getText());
    verify(observer).cellChanged(c, ICell.TEXT_BIT);
  }

  @Test
  public void testSetIconId() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c);
    String iconId = "iconId";
    c.setIconId(iconId);
    assertEquals(iconId, c.getIconId());
    verify(observer).cellChanged(c, ICell.ICON_ID_BIT);
  }

  @Test
  public void testSetTooltipText_notNull() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c);
    String tooltip = "tooltip";
    c.setTooltipText(tooltip);
    assertEquals(tooltip, c.getTooltipText());
    verify(observer).cellChanged(c, ICell.TOOLTIP_BIT);
  }

  @Test
  public void testSetCssClass_notNull() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c);
    String cssClass = "cssClass";
    c.setCssClass(cssClass);
    assertEquals(cssClass, c.getCssClass());
    verify(observer).cellChanged(c, ICell.CSS_CLASS_BIT);
  }

  @Test
  public void testSetTooltipText_null() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c);
    c.setTooltipText(null);
    assertNull(c.getTooltipText());
    verifyZeroInteractions(observer);
  }

  @Test
  public void testSetHorizontalAlignment() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c);
    int hAlignment = 100;
    c.setHorizontalAlignment(hAlignment);
    assertEquals(hAlignment, c.getHorizontalAlignment());
    verify(observer).cellChanged(c, ICell.H_ALIGN_BIT);
  }

  @Test
  public void testSetBackgroundColor() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c);
    String bgColor = "eeeeee";
    c.setBackgroundColor(bgColor);
    assertEquals(bgColor, c.getBackgroundColor());
    verify(observer).cellChanged(c, ICell.BG_COLOR_BIT);
  }

  @Test
  public void testSetForegroundColor() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c);
    String fgColor = "ff0000";
    c.setForegroundColor(fgColor);
    assertEquals(fgColor, c.getForegroundColor());
    verify(observer).cellChanged(c, ICell.FG_COLOR_BIT);
  }

  @Test
  public void testSetFont() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c);
    FontSpec font = FontSpec.parse("Arial-bold-italic-13");
    c.setFont(font);
    assertEquals(font.toPattern(), c.getFont().toPattern());
    verify(observer).cellChanged(c, ICell.FONT_BIT);
  }

  @Test
  public void testSetHtmlEnabled() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c);
    c.setHtmlEnabled(true);
    assertTrue(c.isHtmlEnabled());
    verify(observer).cellChanged(c, ICell.HTML_ENABLED_BIT);
  }

  @Test
  public void testSetMandatory() {
    Cell c = new Cell();
    ICellObserver observer = installMockObserver(c);
    c.setMandatory(true);
    assertTrue(c.isMandatory());
    verify(observer).cellChanged(c, ICell.MANDATORY_BIT);
  }

  @Test
  public void testSetObserver() {
    Cell c = new Cell();
    ICellObserver observer = Mockito.mock(ICellObserver.class);
    c.setObserver(observer);
    assertSame(observer, c.getObserver());
    verifyZeroInteractions(observer);
  }

  private ICellObserver installMockObserver(Cell c) {
    ICellObserver observer = mock(ICellObserver.class);
    c.setObserver(observer);
    return observer;
  }

  /**
   * When creating a cell. The errorstatus should not be set.
   * {@link Cell#setErrorStatus(org.eclipse.scout.rt.platform.status.IStatus)}
   */
  @Test
  public void testInitialErrorStatus() {
    Cell c = new Cell();
    assertNull(c.getErrorStatus());
  }

  /**
   * {@link Cell#setErrorStatus(org.eclipse.scout.rt.platform.status.IStatus)}
   */
  @Test
  public void testSetErrorStatus() {
    Cell c = new Cell();
    c.addErrorStatus(new Status("error", IStatus.ERROR));
    assertEquals(IStatus.ERROR, c.getErrorStatus().getSeverity());
  }

  /**
   * When creating a cell. The errorstatus should not be set.
   * {@link Cell#setErrorStatus(org.eclipse.scout.rt.platform.status.IStatus)}
   */
  @Test
  public void testClearErrorStatus() {
    Cell c = new Cell();
    c.addErrorStatus(new Status("error", IStatus.ERROR));
    c.clearErrorStatus();
    assertNull(c.getErrorStatus());
  }

  /**
   * When creating a cell. The errorstatus should not be set.
   * {@link Cell#setErrorStatus(org.eclipse.scout.rt.platform.status.IStatus)}
   */
  @Test
  public void testIconCellSetErrorStatus() {
    final String testIconId = "";
    Cell c = new Cell();
    c.setIconId(testIconId);
    c.addErrorStatus(new Status("error", IStatus.ERROR));
    c.addErrorStatus(new Status("error2", IStatus.ERROR));
    c.clearErrorStatus();
    c.addErrorStatus(new Status("error3", IStatus.ERROR));
    c.clearErrorStatus();
    assertNull(c.getErrorStatus());
    assertEquals(testIconId, c.getIconId());
  }

  /**
   * {@link Cell#setErrorStatus(org.eclipse.scout.rt.platform.status.IStatus)}
   */
  @Test
  public void testAddRemoveMultistatus() {
    Cell c = new Cell();
    ParsingFailedStatus errorStatus = new ParsingFailedStatus("failed", "rawString");
    MultiStatus ms = new MultiStatus();
    ms.add(errorStatus);
    c.addErrorStatuses(ms.getChildren());
    c.removeErrorStatus(ParsingFailedStatus.class);
    assertNull(c.getErrorStatus());
  }

}
