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
package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JTextField;

import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.TextBlockDragAndDrop;
import org.eclipse.scout.rt.ui.swing.UndoableEditObserver;
import org.eclipse.scout.rt.ui.swing.form.fields.OnFieldLabelDecorator;

/**
 * Workaround for better disabled text component behaviour:
 * <ul>
 * <li>background like disabled selectable copy/paste</li>
 * <li>ability to scroll</li>
 * <li>focausable with mouse, not with TAB</li>
 * <li>not mutable</li>
 * </ul>
 * <p>
 * Support for on-field label using {@link OnFieldLabelDecorator}
 */
public class JTextFieldEx extends JTextField {
  private static final long serialVersionUID = 1L;

  private OnFieldLabelDecorator m_onFieldLabelHandler;

  public JTextFieldEx() {
    super();
    SwingUtility.installDefaultFocusHandling(this);
    TextBlockDragAndDrop.attach(this);
    UndoableEditObserver.attach(this);
    SwingUtility.installAlternateCopyPaste(this);
    SwingUtility.installCopyPasteMenu(this);
  }

  public JTextFieldEx(int n) {
    super(n);
    SwingUtility.installDefaultFocusHandling(this);
    TextBlockDragAndDrop.attach(this);
    UndoableEditObserver.attach(this);
    SwingUtility.installAlternateCopyPaste(this);
    SwingUtility.installCopyPasteMenu(this);
  }

  /**
   * Deep inside swing the text field editor View.class is only checking for
   * isEnable() This is not changeable by a LookAndFeel. This is handled in the
   * corresponding ...Ex sub classes of JTextComponent
   */
  @Override
  public Color getForeground() {
    if (isEditable()) {
      return super.getForeground();
    }
    else {
      return getDisabledTextColor();
    }
  }

  /**
   * Swing sets the cursor of a non-editable text pane to DEFAULT, instead of
   * setting it to null to inherit from the parent.
   */
  @Override
  public void setCursor(Cursor cursor) {
    if (cursor != null && cursor.getType() == Cursor.DEFAULT_CURSOR) {
      cursor = null;
    }
    super.setCursor(cursor);
  }

  /**
   * setter for on-field label handler, since swing knows no paint override or decoration listener
   */
  public void setOnFieldLabelHandler(OnFieldLabelDecorator onFieldLabelHandler) {
    m_onFieldLabelHandler = onFieldLabelHandler;
    repaint();
  }

  public OnFieldLabelDecorator getOnFieldLabelHandler() {
    return m_onFieldLabelHandler;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (m_onFieldLabelHandler != null) {
      m_onFieldLabelHandler.paintOnFieldLabel(g, this);
    }
  }

  @Override
  public Point getToolTipLocation(MouseEvent e) {
    return SwingUtility.getAdjustedToolTipLocation(e, this, getTopLevelAncestor());
  }
}
