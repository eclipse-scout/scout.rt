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
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JPasswordField;

import org.eclipse.scout.rt.ui.swing.SwingUtility;

/**
 * Extension class to the {@link JPasswordField} that implements a different behavior for disabled password fields.
 * The background is painted like it is disabled, but the field's contents is selectable, so that it can be copied to
 * the clipboard. Additionally, the widget is scrollable but not focusable and of course not editable.
 */
public class JPasswordFieldEx extends JPasswordField {

  private static final long serialVersionUID = 1L;

  public JPasswordFieldEx() {
    super();
    SwingUtility.installAlternateCopyPaste(this);
  }

  @Override
  public void setForeground(Color c) {
    super.setForeground(c);
  }

  @SuppressWarnings("deprecation")
  @Override
  public String getText() {
    char[] ch = getPassword();
    if (ch == null) return null;
    else return new String(ch);
  }

  private boolean m_realFocusable = true;

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

  @Override
  public void setFocusable(boolean focusable) {
    m_realFocusable = focusable;
    super.setFocusable(m_realFocusable && isEditable());
  }

  @Override
  public void setEditable(boolean b) {
    super.setEditable(b);
    super.setFocusable(m_realFocusable && isEditable());
  }

  @Override
  public Point getToolTipLocation(MouseEvent e) {
    return SwingUtility.getAdjustedToolTipLocation(e, this, getTopLevelAncestor());
  }
}
