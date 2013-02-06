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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.TextBlockDragAndDrop;
import org.eclipse.scout.rt.ui.swing.UndoableEditObserver;
import org.eclipse.scout.rt.ui.swing.form.fields.OnFieldLabelDecorator;

/**
 * Extension class to the {@link JTextArea} that implements a different behavior for disabled password fields.
 * The background is painted like it is disabled, but the field's contents is selectable, so that it can be copied to
 * the clipboard. Additionally, the widget is scrollable but not focusable and of course not editable.
 */
public class JTextAreaEx extends JTextArea {

  private static final long serialVersionUID = 1L;
  public static final String ACTION_ENTER_PRESSED = "actionEnterPressed";
  public static final String ACTION_ENTER_RELEASED = "actionEnterReleased";

  private OnFieldLabelDecorator m_onFieldLabelHandler;

  public JTextAreaEx() {
    super();
    // focus corrections
    SwingUtility.installDefaultFocusHandling(this);
    setFocusCycleRoot(false);
    //
    setWrapStyleWord(true);
    setLineWrap(true);
    UndoableEditObserver.attach(this);
    TextBlockDragAndDrop.attach(this);
    SwingUtility.installAlternateCopyPaste(this);
    SwingUtility.installCopyPasteMenu(this);
    // consume the enter key stroke
    KeyStroke relesedEnter = KeyStroke.getKeyStroke("released ENTER");
    getInputMap().put(relesedEnter, ACTION_ENTER_RELEASED);
    Action a = new AbstractAction() {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        // void
      }
    };
    getActionMap().put(ACTION_ENTER_RELEASED, a);
  }

  @Override
  protected void processKeyEvent(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_TAB && e.isControlDown()) {
      e = new KeyEvent(e.getComponent(), e.getID(), e.getWhen(), 0, e.getKeyCode(), e.getKeyChar(), e.getKeyLocation());
    }
    super.processKeyEvent(e);
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
