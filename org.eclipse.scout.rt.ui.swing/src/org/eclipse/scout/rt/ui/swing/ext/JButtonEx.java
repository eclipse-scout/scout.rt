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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import org.eclipse.scout.rt.ui.swing.SwingUtility;

/**
 * Extensions to Swing support for default pressed/hover icon wrappers support
 * for disabledBackgroundColor property support for button menu dropdown area
 */

public class JButtonEx extends JButton {
  private static final long serialVersionUID = 1L;

  private boolean m_actionOnMousePressed = false;
  private Color m_enabledBackgroundColor;
  private Color m_disabledBackgroundColor;

  public JButtonEx(Action a) {
    this();
    setAction(a);
  }

  public JButtonEx() {
    super();
    SwingUtility.installDefaultFocusHandling(this);
    // default icon wrappers
    // setPressedIcon(new BeveledButtonIcon(this,false));
    // setRolloverIcon(new BeveledButtonIcon(this,true));
    addMouseListener(new P_MouseListener());
  }

  /**
   * support for touch screen guis, where user presses button and does not
   * release. auto-release is performed after 250 ms
   */
  public boolean isActionOnMousePressed() {
    return m_actionOnMousePressed;
  }

  public void setActionOnMousePressed(boolean b) {
    m_actionOnMousePressed = b;
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

  /**
   * @see isActionOnMousePressed
   */
  private class P_MouseListener extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      if (!e.isPopupTrigger()) {
        // touchscreen: enqueue a timer that disables pressed after some time
        if (isActionOnMousePressed()) {
          new Thread() {
            @Override
            public void run() {
              try {
                sleep(250);
              }
              catch (InterruptedException ie) {
              }
              Runnable t = new Runnable() {
                @Override
                public void run() {
                  ButtonModel m = getModel();
                  if (m != null && m.isPressed()) {
                    m.setPressed(false);
                  }
                }
              };
              SwingUtilities.invokeLater(t);
            }
          }.start();
        }
      }
    }
  }// end private class

}
