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
package org.eclipse.scout.rt.ui.swing.ext.decoration;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.event.EventListenerList;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;

public abstract class AbstractDecorationItem extends AbstractDecoration implements IDecorationItem {

  private State m_state = State.Normal;

  private boolean m_enabled = true;
  private boolean m_visible = true;
  private EventListenerList m_eventListeners = new EventListenerList();
  private Cursor m_mouseOverCursor;
  private Cursor m_defaultCursor;

  public AbstractDecorationItem(JComponent owner, ISwingEnvironment environment, boolean callInitializer) {
    super(owner, environment, callInitializer);
    m_defaultCursor = owner.getCursor();
  }

  private boolean updateState(boolean mouseOver) {
    State newState = State.Normal;
    if (!m_visible) {
      return false;
    }
    if (!m_enabled) {
      newState = State.Disabled;

    }
    else if (mouseOver) {
      newState = State.Rollover;
    }
    else {
      newState = State.Normal;
    }
    if (!CompareUtility.equals(newState, m_state)) {
      m_state = newState;
      return true;
    }
    return false;
  }

  @Override
  public State getState() {
    return m_state;
  }

  public boolean isVisible() {
    return m_visible;
  }

  public void setVisible(boolean visible) {
    m_visible = visible;
  }

  public boolean isEnabled() {
    return m_enabled;
  }

  public void setEnabled(boolean enabled) {
    m_enabled = enabled;
    updateState(false);
  }

  public void setMouseOverCursor(Cursor mouseOverCursor) {
    m_mouseOverCursor = mouseOverCursor;
  }

  public Cursor getMouseOverCursor() {
    return m_mouseOverCursor;
  }

  public synchronized void addMouseListener(MouseListener l) {
    if (l == null) {
      return;
    }
    m_eventListeners.add(MouseListener.class, l);
  }

  public synchronized void removeMouseListener(MouseListener l) {
    if (l == null) {
      return;
    }
    m_eventListeners.remove(MouseListener.class, l);
  }

  @Override
  public void handleMouseChlicked(MouseEvent e) {
    if (isVisible() && isEnabled()) {
      for (MouseListener l : m_eventListeners.getListeners(MouseListener.class)) {
        l.mouseClicked(e);
      }
    }
  }

  @Override
  public void handleMouseMoved(DecorationMouseEvent e) {

    if (!isVisible() || !isEnabled()) {
      return;
    }
    boolean mouseOver = getBounds() != null && getBounds().contains(e.getPoint());
    if (mouseOver && getMouseOverCursor() != e.getCurrentCursor()) {
      e.setCursorToApply(getMouseOverCursor());
    }
    if (updateState(mouseOver)) {
      e.setRepaintNeeded(true);
    }
  }

  @Override
  public void handleMouseExit(DecorationMouseEvent e) {
    if (!isVisible() || !isEnabled()) {
      return;
    }
    // reset state
    if (updateState(false)) {
      e.setRepaintNeeded(true);
    }
  }

}
