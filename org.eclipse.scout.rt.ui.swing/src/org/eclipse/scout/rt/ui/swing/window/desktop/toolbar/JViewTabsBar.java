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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.synth.SynthLookAndFeel;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.SwingIcons;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.action.ISwingScoutAction;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup.IconState;
import org.eclipse.scout.rt.ui.swing.ext.FlowLayoutEx;
import org.eclipse.scout.rt.ui.swing.window.desktop.menubar.SwingScoutMainMenuPopup;

/**
 * SwingScoutToolBar
 * <p>
 * Checks UIManager.get("MenuBar.policy") == "toolbar" (alternative: "menubar")
 * </p>
 */
public class JViewTabsBar extends AbstractJTabBar {
  private static final long serialVersionUID = 1L;

  private final ISwingEnvironment m_env;
  private Icon m_activeTabLeftIcon;
  private Icon m_activeTabRightIcon;
  private IconGroup m_menuIcon;
  private IDesktop m_desktop;
  private JLabel m_menuLabel;
  private SwingScoutMainMenuPopup m_popupMenu;

  JViewTabsBar(ISwingEnvironment env) {
    m_env = env;
    m_activeTabLeftIcon = m_env.getIcon(SwingIcons.ViewTabLeft);
    m_activeTabRightIcon = m_env.getIcon(SwingIcons.ViewTabRight);
    m_menuIcon = new IconGroup(m_env, SwingIcons.IconMenu);
    setName("Synth.ViewTabsBar");
    setLayout(new Layout());
    setOpaque(true);
    if (!(UIManager.getLookAndFeel() instanceof SynthLookAndFeel)) {
      setBackground(new Color(0xddebf4));//XXX
    }
  }

  private boolean isShowMenuBar() {
    return "toolbar".equals(UIManager.get("MenuBar.policy"));
  }

  /**
   * Adds a border on the right instead of using hgap of flowlayout, because flowlayout adds the gap also
   * in front of the first element and that's not what we want.
   * 
   * @param tab
   * @return
   */
  private AbstractButton addViewTab(AbstractButton tab) {
    addActiveTabListener(tab);
    tab.setBorder(new EmptyBorder(3, 12, 0, 14));//XXX UI?
    add(tab);
    return tab;
  }

  private void addMenuIcon() {
    m_menuLabel = new JLabel();
    m_menuLabel.setIcon(m_menuIcon.getIcon(IconState.NORMAL));
    m_menuLabel.setBorder(new EmptyBorder(3, 3, 2, 0));//XXX UI?
    new HandCursorAdapater(m_menuLabel);
    m_menuLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        m_popupMenu.getSwingPopupMenu().show(m_menuLabel, 0, m_menuLabel.getHeight());
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        m_menuLabel.setIcon(m_menuIcon.getIcon(IconState.ROLLOVER));
      }

      @Override
      public void mouseExited(MouseEvent e) {
        m_menuLabel.setIcon(m_menuIcon.getIcon(IconState.NORMAL));
      }
    });
    add(m_menuLabel);
  }

  void rebuild(IDesktop desktop) {
    removeAll();
    if (isShowMenuBar()) {
      addMenuIcon();
      //add rigid glue
      add(SwingUtility.createGlue(5, 5, false, false));
    }
    for (IAction a : desktop.getViewButtons()) {
      ISwingScoutAction button = m_env.createAction(this, a);
      if (button != null) {
        addViewTab((AbstractButton) button.getSwingField());
      }
    }
    //add variable glue
    add(SwingUtility.createGlue(0, 0, true, false));
    if (isShowMenuBar()) {
      m_popupMenu = new SwingScoutMainMenuPopup();
      m_popupMenu.createField(desktop, m_env);
    }
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    paintActiveTabOnBackground(g);
  }

  private void paintActiveTabOnBackground(Graphics g) {
    AbstractButton tab = getActiveTab();
    if (tab == null) {
      return;
    }
    Insets i = tab.getInsets();
    int y = i.top;
    // left tab icon
    int x = tab.getX() + i.left - m_activeTabLeftIcon.getIconWidth();
    m_activeTabLeftIcon.paintIcon(this, g, x, y);
    // right tab icon
    x = tab.getX() + tab.getWidth() - i.right;
    m_activeTabRightIcon.paintIcon(this, g, x, y);
    // white fill
    x = tab.getX() + i.left;
    g.setColor(Color.WHITE);
    g.fillRect(x, y, tab.getWidth() - i.right - i.left, getHeight() - y);
  }

  private class Layout extends FlowLayoutEx {

    Layout() {
      super(FlowLayoutEx.LEFT, FlowLayoutEx.HORIZONTAL, 0, 0);
      setFillVertical(true);
      setFillHorizontal(true);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
      Dimension d = super.minimumLayoutSize(parent);
      //XXX fixed height, so tool and view bar match size, solve this in swinscouttoolbar with common container for both
      d.height = 30;
      return d;
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
      Dimension d = super.preferredLayoutSize(parent);
      //XXX fixed height, so tool and view bar match size, solve this in swinscouttoolbar with common container for both
      d.height = 30;
      return d;
    }

    @Override
    public Dimension maximumLayoutSize(Container parent) {
      Dimension d = super.maximumLayoutSize(parent);
      d.width = 10240;
      //XXX fixed height, so tool and view bar match size, solve this in swinscouttoolbar with common container for both
      d.height = 30;
      return d;
    }
  }
}
