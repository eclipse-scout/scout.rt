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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.eclipse.scout.rt.ui.swing.Activator;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.SwingIcons;

public class JNavigationWidget extends JPanel {

  enum ButtonName {
    NONE(new Rectangle(), SwingIcons.NavigationShadow),
    BACK(new Rectangle(5, 4, 33, 32), SwingIcons.NavigationShadowBack),
    FORWARD(new Rectangle(39, 4, 42, 32), SwingIcons.NavigationShadowForward),
    STOP_REFRESH(new Rectangle(82, 4, 22, 32), SwingIcons.NavigationShadowStopRefresh);

    private Rectangle m_bounds;

    private String m_dropshadowIcon;

    ButtonName(Rectangle bounds, String dropshadowIcon) {
      this.m_bounds = bounds;
      this.m_dropshadowIcon = dropshadowIcon;
    }

    static ButtonName getMouseOverButton(int x, int y) {
      // System.out.println("x=" + x + " y=" + y);
      for (ButtonName buttonName : values()) {
        if (buttonName.m_bounds.contains(x, y)) {
          return buttonName;
        }
      }
      return NONE;
    }
  }

  private static final long serialVersionUID = 1L;
  private static final int SHADOW_DISTANCE = 2;
  private static final Dimension SIZE = new Dimension(109 + SHADOW_DISTANCE, 40 + SHADOW_DISTANCE);
  private static final Point HISTORY_MENU_LOCATION = new Point(38, 38);

  private ISwingEnvironment m_env;
  private NavigationWidgetButton m_mouseOverButton;
  private ButtonName m_dropshadowBelowButton = ButtonName.NONE;
  private Map<ButtonName, NavigationWidgetButton> m_buttonMap = new HashMap<ButtonName, NavigationWidgetButton>();

  public JNavigationWidget(ISwingEnvironment env) {
    m_env = env;
    setPreferredSize(SIZE);
    setOpaque(false);
    initButtons();

    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        detectMouseOverButton(e.getX(), e.getY());
      }
    });

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseExited(MouseEvent e) {
        setMouseOverButton(ButtonName.NONE);
      }

      @Override
      public void mousePressed(MouseEvent e) {
        setPressedButton(true);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        setPressedButton(false);
        buttonClicked(e.getButton());
      }
    });

    env.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (ISwingEnvironment.PROP_BUSY.equals(evt.getPropertyName())) {
          Boolean busy = (Boolean) evt.getNewValue();
          getStopRefreshButton().setLoading(busy);
        }
      }
    });
  }

  private void initButtons() {
    NavigationWidgetButton back = new NavigationWidgetButton(ButtonName.BACK, this);
    back.loadButtonStates(SwingIcons.NavigationBtnBack);
    back.loadIconStates(SwingIcons.NavigationIcoBack);
    back.m_buttonPos = new Point(5, 4);
    back.m_iconPos = new Point(13, 9);
    m_buttonMap.put(ButtonName.BACK, back);
    addDisabledListener(back);

    final ForwardButton forward = new ForwardButton(this);
    forward.loadButtonStates(SwingIcons.NavigationBtnForward);
    forward.loadIconStates(SwingIcons.NavigationIcoForward);
    forward.m_buttonPos = new Point(39, 4);
    forward.m_iconPos = new Point(44, 9);
    forward.loadHistoryIcon(SwingIcons.NavigationIcoHistory);
    forward.m_historyIconPos = new Point(57, 9);
    forward.setEnabled(false);
    forward.setHistoryEnabled(back.isEnabled());
    m_buttonMap.put(ButtonName.FORWARD, forward);
    addDisabledListener(forward);

    StopRefreshButton stopRefresh = new StopRefreshButton(this);
    stopRefresh.loadButtonStates(SwingIcons.NavigationBtnStopRefresh);
    stopRefresh.loadIconStates(SwingIcons.NavigationIcoRefresh);
    stopRefresh.m_buttonPos = new Point(73, 4);
    stopRefresh.m_iconPos = new Point(79, 11);
    stopRefresh.loadStopIcon(SwingIcons.NavigationIcoStop);
    stopRefresh.loadGlowAnimation(SwingIcons.NavigationGlow);
    stopRefresh.showStopIcon();
    m_buttonMap.put(ButtonName.STOP_REFRESH, stopRefresh);
    addDisabledListener(stopRefresh);

    PropertyChangeListener historyEnabledListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (NavigationWidgetButton.PROP_ENABLED.equals(evt.getPropertyName())) {
          boolean enabled = (Boolean) evt.getNewValue();
          forward.setHistoryEnabled(enabled);
        }
      }
    };
    back.addPropertyChangeListener(historyEnabledListener);
  }

  /**
   * Remove orange glow below button, when a button is disabled.
   * 
   * @param button
   */
  private void addDisabledListener(NavigationWidgetButton button) {
    button.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (NavigationWidgetButton.PROP_ENABLED.equals(evt.getPropertyName())) {
          boolean enabled = (Boolean) evt.getNewValue();
          if (!enabled) {
            NavigationWidgetButton disabledButton = (NavigationWidgetButton) evt.getSource();
            if (m_mouseOverButton != null && m_mouseOverButton.getName() == disabledButton.getName()) {
              setMouseOverButton(ButtonName.NONE);
            }
          }
        }
      }
    });
  }

  // TODO awe: bessere kollisions erkennung für runde bereiche (evt. B/W bitmap verwenden)
  private void detectMouseOverButton(int x, int y) {
    ButtonName moButtonName = ButtonName.getMouseOverButton(x, y);
    setMouseOverButton(moButtonName);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    paintDropshadow(g);
    paintButtons(g);
  }

  private void paintDropshadow(Graphics g) {
    Icon icon = Activator.getIcon(m_dropshadowBelowButton.m_dropshadowIcon);
    if (icon != null) {
      icon.paintIcon(this, g, SHADOW_DISTANCE, SHADOW_DISTANCE);
    }
  }

  private NavigationWidgetButton getButton(ButtonName button) {
    return m_buttonMap.get(button);
  }

  private void paintButtons(Graphics g) {
    getButton(ButtonName.BACK).paintComponent(this, g);
    getButton(ButtonName.FORWARD).paintComponent(this, g);
    getButton(ButtonName.STOP_REFRESH).paintComponent(this, g);
  }

  private void setPressedButton(boolean pressed) {
    if (m_mouseOverButton != null) {
      m_mouseOverButton.setPressed(pressed);
    }
  }

  private void buttonClicked(int button) {
    if (m_mouseOverButton != null) {
      m_mouseOverButton.buttonClicked(button);
    }
  }

  private void setMouseOverButton(ButtonName moButtonName) {
    if (m_mouseOverButton != null) {
      m_mouseOverButton.setMouseOver(false);
      m_mouseOverButton.setPressed(false);
    }

    m_mouseOverButton = getButton(moButtonName);
    if (m_mouseOverButton != null && m_mouseOverButton.isEnabled()) {
      m_dropshadowBelowButton = moButtonName;
    }
    else {
      m_dropshadowBelowButton = ButtonName.NONE;
    }

    if (m_mouseOverButton != null) {
      m_mouseOverButton.setMouseOver(true);
    }
  }

  public NavigationWidgetButton getBackButton() {
    return getButton(ButtonName.BACK);
  }

  public ForwardButton getForwardButton() {
    return (ForwardButton) getButton(ButtonName.FORWARD);
  }

  public StopRefreshButton getStopRefreshButton() {
    return (StopRefreshButton) getButton(ButtonName.STOP_REFRESH);
  }

  public Point getHistoryMenuLocation() {
    return HISTORY_MENU_LOCATION;
  }
}
