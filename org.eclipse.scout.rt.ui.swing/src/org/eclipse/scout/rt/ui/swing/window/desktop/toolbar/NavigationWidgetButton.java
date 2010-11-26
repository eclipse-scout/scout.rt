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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;

import org.eclipse.scout.rt.ui.swing.Activator;
import org.eclipse.scout.rt.ui.swing.window.desktop.toolbar.JNavigationWidget.ButtonName;

/**
 * Base class of all buttons of the navigation widget.
 * 
 * @author awe
 */
public class NavigationWidgetButton {

  protected static final String PROP_ENABLED = "enabled";

  private PropertyChangeSupport m_support = new PropertyChangeSupport(this);

  private JComponent m_parent;

  private Icon[] m_button;

  private Icon[] m_icon;

  Point m_buttonPos;

  Point m_iconPos;

  private boolean m_enabled = true;

  private boolean m_mouseOver = false;

  private boolean m_pressed = false;

  private AbstractAction m_primaryAction;

  private AbstractAction m_secondaryAction;

  private ButtonName m_name;

//  protected final ISwingEnvironment m_env;

  NavigationWidgetButton(ButtonName name, JComponent parent) {
    m_name = name;
    m_parent = parent;
  }

  Icon getButton() {
    return m_button[m_mouseOver ? 1 : 0];
  }

  protected Icon getTriStateIcon(Icon[] triStateIcon, boolean enabled) {
    int index = 0;
    if (!enabled) {
      index = 2;
    }
    else if (isMouseOver()) {
      index = 1;
    }
    return triStateIcon[index];
  }

  Icon getIcon() {
    return getTriStateIcon(m_icon, isEnabled());
  }

  public boolean isPressed() {
    return m_pressed;
  }

  public void setPressed(boolean pressed) {
    if (isEnabled()) {
      boolean oldPressed = m_pressed;
      m_pressed = pressed;
      if (oldPressed != m_pressed) {
        repaintParent();
      }
    }
  }

  protected void performAction(AbstractAction action) {
    action.actionPerformed(new ActionEvent(m_parent, ActionEvent.ACTION_PERFORMED, null));
  }

  /**
   * Called when a button has been clicked. The default impl. performs the primary action.
   * 
   * @param button
   *          A button constant from MouseEvent.
   */
  public void buttonClicked(int button) {
    performAction(m_primaryAction);
  }

  public boolean isMouseOver() {
    return m_mouseOver;
  }

  public void setMouseOver(boolean mouseOver) {
    if (isEnabled()) {
      boolean oldMouseOver = m_mouseOver;
      m_mouseOver = mouseOver;
      if (oldMouseOver != m_mouseOver) {
        repaintParent();
      }
    }
  }

  public boolean isEnabled() {
    return m_enabled;
  }

  public void setEnabled(boolean enabled) {
    boolean oldEnabled = m_enabled;
    m_enabled = enabled;
    m_support.firePropertyChange(PROP_ENABLED, oldEnabled, m_enabled);
    if (oldEnabled != m_enabled) {
      if (!enabled) {
        m_mouseOver = false;
        m_pressed = false;
      }
      repaintParent();
    }
  }

  public void loadButtonStates(String iconUrl) {
    m_button = new Icon[2];
    m_button[0] = Activator.getIcon(iconUrl);
    m_button[1] = Activator.getIcon(iconUrl + "_mo");
  }

  public void loadIconStates(String iconUrl) {
    m_icon = loadTriStateIcon(iconUrl);
  }

  protected Icon[] loadTriStateIcon(String iconUrl) { // TODO AWE use IconGroup here
    Icon[] icon = new Icon[3];
    icon[0] = Activator.getIcon(iconUrl);
    icon[1] = Activator.getIcon(iconUrl + "_mo");
    icon[2] = Activator.getIcon(iconUrl + "_da");
    return icon;
  }

  public final void paintComponent(Component c, Graphics g) {
    paintButton(c, g);
    paintIcon(c, g);
  }

  protected void paintButton(Component c, Graphics g) {
    paintPressedImage(getButton(), m_buttonPos, m_pressed, c, g);
  }

  protected void paintIcon(Component c, Graphics g) {
    paintPressedImage(getIcon(), m_iconPos, m_pressed, c, g);
  }

  protected void paintPressedImage(Icon img, Point p, boolean pressed, Component c, Graphics g) {
    int x = p.x;
    int y = p.y;
    if (pressed) {
      x += 1;
      y += 1;
    }
    img.paintIcon(c, g, x, y);
  }

  protected void repaintParent() {
    m_parent.repaint();
  }

  void addPropertyChangeListener(PropertyChangeListener l) {
    m_support.addPropertyChangeListener(l);
  }

  void removePropertyChangeListener(PropertyChangeListener l) {
    m_support.removePropertyChangeListener(l);
  }

  public final void setPrimaryAction(AbstractAction a) {
    m_primaryAction = a;
  }

  public final AbstractAction getPrimaryAction() {
    return m_primaryAction;
  }

  public final void setSecondaryAction(AbstractAction a) {
    m_secondaryAction = a;
  }

  public final AbstractAction getSecondaryAction() {
    return m_secondaryAction;
  }

  public ButtonName getName() {
    return m_name;
  }

}
