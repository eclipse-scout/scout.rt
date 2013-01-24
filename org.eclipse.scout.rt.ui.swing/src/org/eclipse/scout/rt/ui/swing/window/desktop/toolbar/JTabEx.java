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
package org.eclipse.scout.rt.ui.swing.window.desktop.toolbar;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.eclipse.scout.rt.ui.swing.ext.JToggleButtonEx;

/**
 * Base class used for tabs. Adds hand cursor and mouse over support to the JToggleButton.
 * 
 * @author awe
 */
public class JTabEx extends JToggleButtonEx {
  private static final long serialVersionUID = 1L;

  public static final String PROP_ACTIVE = "active";

  private boolean m_mouseOver = false;
  private boolean m_active;

  public JTabEx() {
    m_active = isSelected();
    setModel(new ToggleButtonModel() {
      private static final long serialVersionUID = 1L;

      @Override
      public void setPressed(boolean b) {
        //inhibit click if selected
        if (b && isSelected()) {
          //re-send an action event
          fireActionPerformed(new ActionEvent(JTabEx.this, ActionEvent.ACTION_PERFORMED, getActionCommand()));
          return;
        }
        super.setPressed(b);
      }
    });
    installMouseAdapter();
    new HandCursorAdapater(this);
    addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        boolean oldActive = m_active;
        m_active = isSelected();
        firePropertyChange(PROP_ACTIVE, oldActive, m_active);
      }
    });
  }

  public boolean isMouseOver() {
    return m_mouseOver;
  }

  protected void setMouseOver(boolean mouseOver) {
    this.m_mouseOver = mouseOver;
    repaint();
  }

  protected void installMouseAdapter() {
    addMouseListener(new MouseAdapter() {

      @Override
      public void mouseEntered(MouseEvent e) {
        setMouseOver(true);
      }

      @Override
      public void mouseExited(MouseEvent e) {
        setMouseOver(false);
      }
    });
  }
}
