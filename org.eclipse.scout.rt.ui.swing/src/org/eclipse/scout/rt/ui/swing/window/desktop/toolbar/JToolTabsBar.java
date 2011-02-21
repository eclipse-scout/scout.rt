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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Shape;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.synth.SynthLookAndFeel;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.swing.Activator;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.SwingIcons;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.action.ISwingScoutAction;
import org.eclipse.scout.rt.ui.swing.ext.FlowLayoutEx;

public class JToolTabsBar extends AbstractJTabBar {
  private static final long serialVersionUID = 1L;

  private static final class P_ToolTabData {
    String title;
    String icon;
    String tooltip;

    private P_ToolTabData(String title, String icon, String tooltip) {
      this.title = title;
      this.icon = icon;
      this.tooltip = tooltip;
    }
  }

  public static final String PROP_COLLAPSED = "collapsed";
  public static final String PROP_MINIMUM_SIZE = "minimumSize";

  private final ISwingEnvironment m_env;
  private Icon m_activeTabIcon;
  private boolean m_collapsed = true;
  private int m_preferredWidth = 0;
  private CollapseButton m_collapseButton;

  JToolTabsBar(ISwingEnvironment env) {
    m_env = env;
    m_activeTabIcon = Activator.getIcon(SwingIcons.ToolTab);
    setLayout(new Layout());
    setName("Synth.ToolTabsBar");
    if (!(UIManager.getLookAndFeel() instanceof SynthLookAndFeel)) {
      setOpaque(true);
      setBackground(new Color(0x67a8ce));//TODO [awe] add to lookandfeel
    }
  }

  void rebuild(IDesktop desktop) {
    removeAll();
    addCollapseButton();
    add(SwingUtility.createGlue(0, 0, true, false));
    ArrayList<String> textList = new ArrayList<String>();
    for (IAction a : desktop.getToolButtons()) {
      ISwingScoutAction button = m_env.createAction(this, a);
      if (button != null) {
        addToolTab((AbstractButton) button.getSwingField());
        textList.add(a.getText());
      }
    }
    m_collapseButton.setPotentialTexts(textList);
  }

  @Override
  protected void tabActivated(AbstractButton tab) {
    Dimension oldMin = getMinimumSize();
    if (m_collapsed) {
      expandBar();
    }
    m_collapseButton.setText(tab.getText());
    firePropertyChange(PROP_MINIMUM_SIZE, oldMin, getMinimumSize());
    revalidateAndRepaint();
  }

  private void expandBar() {
    m_collapseButton.setVisible(true);
    revalidateAndRepaint();
    setCollapsed(false);
  }

  private void collapseBar() {
    if (isCollapsed()) {
      return;
    }
    if (getActiveTab() != null) {
      getActiveTab().setSelected(false);
    }
    m_preferredWidth = 0;
    m_collapseButton.setVisible(false);
    revalidateAndRepaint();
    setCollapsed(true);
  }

  public boolean isCollapsed() {
    return m_collapsed;
  }

  private void setCollapsed(boolean collapsed) {
    boolean oldCollapsed = m_collapsed;
    m_collapsed = collapsed;
    firePropertyChange(PROP_COLLAPSED, oldCollapsed, m_collapsed);
  }

  private void revalidateAndRepaint() {
    revalidate();
    repaint();
  }

  private void addCollapseButton() {
    m_collapseButton = new CollapseButton(m_env) {
      private static final long serialVersionUID = 1L;

      /**
       * WORKAROUND swing is inconsistent in handling gui events: all events are sync except component events!
       */
      @SuppressWarnings("deprecation")
      @Override
      public void hide() {
        super.hide();
        //
        collapseBar();
      }
    };
    if (isCollapsed()) {
      m_collapseButton.setVisible(false);
    }
    add(m_collapseButton);
  }

  private void addToolTab(AbstractButton tab) {
    tab.setBorder(new EmptyBorder(0, 10, 0, 10));//XXX UI?
    add(tab);
    addActiveTabListener(tab);
    addToolBarChangedListener(tab);
  }

  private void addToolBarChangedListener(AbstractButton tab) {
    // bsh 2010-10-15: This listener is needed in order for the "on/off semantics"
    // to work properly. The listener acts as a bridge between the tab and
    // the tool bar (so each tab can inform the toolbar that it should hide
    // the collapse button).
    tab.addPropertyChangeListener(JToolTab.PROP_CHANGED_TOOL_TAB_STATE, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        // If the tool tab state was changed, but now tabs are selected anymore,
        // we interpret this as a request to collapse the toolbar.
        boolean allTabsAreInactive = true;
        for (Component c : getComponents()) {
          if (c instanceof AbstractButton) {
            if (((AbstractButton) c).isSelected()) {
              allTabsAreInactive = false;
              break;
            }
          }
        }
        if (allTabsAreInactive) {
          collapseBar();
        }
      }
    });
  }

  public void adjustWidthToToolsView(int newValue) {
    if (newValue != m_preferredWidth) {
      m_preferredWidth = newValue;
      revalidateAndRepaint();
    }
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    paintActiveTabOnBackground(g);
  }

  private void paintActiveTabOnBackground(Graphics g) {
    AbstractButton tab = getActiveTab();
    Icon icon = m_activeTabIcon;
    if (tab != null && icon != null) {
      int x = tab.getX();
      int y = getHeight() - icon.getIconHeight();
      int w = tab.getWidth();
      int h = icon.getIconHeight();
      Shape oldClip = g.getClip();
      g.setClip(x, y, 6, h);
      icon.paintIcon(this, g, x, y);
      g.setClip(x + 6, y, w - 12, h);
      for (int k = 0; k < w; k = k + icon.getIconWidth()) {
        icon.paintIcon(this, g, x + k, y);
      }
      g.setClip(x + w - 6, y, 6, h);
      icon.paintIcon(this, g, x, y);
      g.setClip(oldClip);
    }
  }

  private class Layout extends FlowLayoutEx {

    Layout() {
      super(FlowLayoutEx.LEFT, FlowLayoutEx.HORIZONTAL, 0, 0);
      setFillVertical(true);
      setFillHorizontal(true);
    }

    @Override
    protected Dimension getLayoutSize(Container parent, int sizeflag) {
      Dimension d;
      if (isCollapsed()) {
        d = super.getLayoutSize(parent, PREF_SIZE);
      }
      else {
        d = super.getLayoutSize(parent, PREF_SIZE);
        switch (sizeflag) {
          case MIN_SIZE: {
            break;
          }
          case PREF_SIZE: {
            if (m_preferredWidth > 0) {
              d.width = Math.max(d.width, m_preferredWidth);
            }
            break;
          }
          case MAX_SIZE: {
            d.width = 10240;
            break;
          }
        }
      }
      //XXX fixed height, so tool and view bar match size, solve this in swinscouttoolbar with common container for both
      d.height = 30;
      return d;
    }
  }
}
