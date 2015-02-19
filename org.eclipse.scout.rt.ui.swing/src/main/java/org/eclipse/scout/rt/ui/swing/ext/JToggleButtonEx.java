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

import javax.swing.Action;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import org.eclipse.scout.rt.ui.swing.SwingUtility;

/**
 * Extensions to Swing support for default pressed/hover icon wrappers support
 * for disabledBackgroundColor property support for button menu dropdown area
 */

public class JToggleButtonEx extends JToggleButton {
  private static final long serialVersionUID = 1L;

  private Color m_enabledBackgroundColor;
  private Color m_disabledBackgroundColor;

  public JToggleButtonEx() {
    this(null);
  }

  public JToggleButtonEx(Action a) {
    super();
    if (a != null) {
      setAction(a);
    }
    SwingUtility.installDefaultFocusHandling(this);
  }

  /**
   * Background color depending on enabled/disabled
   */
  @Override
  public void setBackground(Color bg) {
    m_enabledBackgroundColor = bg;
    m_disabledBackgroundColor = null;
    if (bg != null) {
      // add transparence by mixing background with control background
      Color cc = UIManager.getColor("control");
      if (cc != null) {
        m_disabledBackgroundColor = new ColorUIResource(new Color((bg.getRed() + cc.getRed()) / 2, (bg.getGreen() + cc.getGreen()) / 2, (bg.getBlue() + cc.getBlue()) / 2));
      }
    }
    super.setBackground(bg);
  }

  @Override
  public void setEnabled(boolean b) {
    boolean changed = (isEnabled() != b);
    super.setEnabled(b);
    if (changed) {
      if (b) {
        // enabled
        if (m_enabledBackgroundColor != null) {
          // super call, otherwise the background color is calculated again
          super.setBackground(m_enabledBackgroundColor);
        }
      }
      else {
        // disabled
        if (m_disabledBackgroundColor != null) {
          // super call, otherwise the background color is calculated again
          super.setBackground(m_disabledBackgroundColor);
        }
      }
    }
  }

  @Override
  public Point getToolTipLocation(MouseEvent e) {
    return SwingUtility.getAdjustedToolTipLocation(e, this, getTopLevelAncestor());
  }
}
