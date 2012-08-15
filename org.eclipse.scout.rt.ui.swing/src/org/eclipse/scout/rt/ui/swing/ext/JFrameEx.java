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

import java.awt.AWTEvent;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JRootPane;

/**
 * bug fixes:
 * <ul>
 * <li>respects native screen insets (taskbar)</li>
 * <li>iconify</li>
 * <li>using {@link JRootPaneEx} with min/max size validation</li>
 * <li>fire property "state" whenever extendedState is changed</li>
 * </ul>
 */
public class JFrameEx extends JFrame {
  private static final long serialVersionUID = 1L;

  private boolean m_autoCorrectSize;
  private Rectangle m_nonMaximizedBounds;

  public JFrameEx() {
    super();
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    enableEvents(AWTEvent.COMPONENT_EVENT_MASK);
    m_autoCorrectSize = true;
  }

  public boolean isAutoCorrectSize() {
    return m_autoCorrectSize;
  }

  public void setAutoCorrectSize(boolean b) {
    m_autoCorrectSize = b;
  }

  public Rectangle getNonMaximizedBounds() {
    return m_nonMaximizedBounds;
  }

  /**
   * WORKAROUND send property events
   */
  @Override
  public synchronized void setExtendedState(int state) {
    int oldState = getExtendedState();
    super.setExtendedState(state);
    int newState = getExtendedState();
    firePropertyChange("state", oldState, newState);
  }

  /**
   * WORKAROUND for correctly bringing a JFrame to front
   */
  @Override
  public void toFront() {
    setState(JFrame.ICONIFIED);
    setState(JFrame.NORMAL);
    super.toFront();
  }

  @Override
  protected JRootPane createRootPane() {
    return new JRootPaneEx() {
      private static final long serialVersionUID = 1L;

      @Override
      protected void reflow() {
        if (m_autoCorrectSize) {
          JFrameEx.this.pack();
        }
      }
    };
  }

}
