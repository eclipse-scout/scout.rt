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
package org.eclipse.scout.rt.ui.swing.window.desktop.toolbar.internal;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.action.ISwingScoutAction;
import org.eclipse.scout.rt.ui.swing.window.desktop.toolbar.AbstractJToolTabsBar;
import org.eclipse.scout.rt.ui.swing.window.desktop.toolbar.SwingScoutToolBar;

public class JToolTabsBar extends AbstractJToolTabsBar {
  private static final long serialVersionUID = 1L;

  private final ISwingEnvironment m_env;
  private JButton m_collapseButton;
  private List<ISwingScoutAction<IToolButton>> m_swingScoutToolButtons;

  private JToolBar m_swingToolBar;
  private SwingScoutToolBar m_swingScoutToolBarContainer;
  private final SpringLayout m_layout;

  public JToolTabsBar(ISwingEnvironment env) {
    m_env = env;
    m_layout = new SpringLayout();
    setLayout(m_layout);
  }

  @Override
  public void rebuild(IDesktop desktop) {
    removeAll();

    m_swingToolBar = new JToolBar(JToolBar.HORIZONTAL);
    m_swingToolBar.setFloatable(false);
    m_swingToolBar.setBorder(new EmptyBorder(0, 0, 0, 3));
    m_swingToolBar.setLayout(new GridBagLayout());

    m_collapseButton = new JButton();
    m_collapseButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        collapseBar();
      }
    });
    m_collapseButton.setIcon(createArrowRightIcon());
    m_collapseButton.setVisible(false);
    m_collapseButton.setHorizontalAlignment(SwingConstants.LEFT);
    m_collapseButton.setMargin(new Insets(2, 5, 2, 5));

    // layout collapse button
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 100.0;
    gbc.insets = new Insets(0, 0, 0, 0);
    m_swingToolBar.add(m_collapseButton, gbc);

    m_swingScoutToolButtons = new LinkedList<ISwingScoutAction<IToolButton>>();
    for (IToolButton scoutToolButton : desktop.getToolButtons()) {
      ISwingScoutAction<IToolButton> swingScoutToolButton = createSwingScoutToolButton(scoutToolButton);
      m_swingScoutToolButtons.add(swingScoutToolButton);

      if (swingScoutToolButton != null) {
        AbstractButton swingButton = (AbstractButton) swingScoutToolButton.getSwingField();
        if (swingButton.getIcon() != null) {
          swingButton.setText(null);
        }
        addActiveTabListener(swingButton);
        addToolBarChangedListener(scoutToolButton);

        // layout tool button
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 2, 0, 0);
        gbc.fill = GridBagConstraints.VERTICAL;
        m_swingToolBar.add(swingButton, gbc);
      }
    }

    add(m_swingToolBar);

    // layout tool bar
    m_layout.putConstraint(SpringLayout.WEST, m_swingToolBar, 0, SpringLayout.WEST, this);
    m_layout.putConstraint(SpringLayout.EAST, m_swingToolBar, 0, SpringLayout.EAST, this);
    m_layout.putConstraint(SpringLayout.SOUTH, m_swingToolBar, 0, SpringLayout.SOUTH, this);
  }

  @Override
  public void setSwingScoutToolBarContainer(SwingScoutToolBar swingScoutToolBarContainer) {
    m_swingScoutToolBarContainer = swingScoutToolBarContainer;
  }

  @Override
  protected void tabActivated(AbstractButton tab) {
    Dimension oldMin = getMinSizeOfToolBar();
    if (isCollapsed()) {
      expandBar();
      // to ensure that the tool bar frame cannot be resized to a width smaller than the minimal tool bar button width
      firePropertyChange(AbstractJToolTabsBar.PROP_MINIMUM_SIZE, oldMin, getMinSizeOfToolBar());
    }
  }

  private Dimension getMinSizeOfToolBar() {
    return m_swingToolBar.getMinimumSize();
  }

  protected ISwingEnvironment getSwingEnvironment() {
    return m_env;
  }

  private void expandBar() {
    setCollapsed(false);
  }

  private void collapseBar() {
    if (isCollapsed()) {
      return;
    }
    for (ISwingScoutAction<IToolButton> swingScoutToolButton : m_swingScoutToolButtons) {
      if (swingScoutToolButton.getScoutObject().isSelected()) {
        ((AbstractButton) swingScoutToolButton.getSwingField()).setSelected(false);
      }
    }

    setCollapsed(true);
    m_swingScoutToolBarContainer.adjustWidthToToolsView((int) getMinSizeOfToolBar().getWidth());
  }

  private boolean isCollapsed() {
    return !m_collapseButton.isVisible();
  }

  private void setCollapsed(boolean collapsed) {
    boolean oldCollapsed = isCollapsed();
    m_collapseButton.setVisible(!collapsed);
    firePropertyChange(AbstractJToolTabsBar.PROP_COLLAPSED, oldCollapsed, collapsed);
  }

  private void addToolBarChangedListener(IAction scoutAction) {
    // bsh 2010-10-15: This listener is needed in order for the "on/off semantics"
    // to work properly. The listener acts as a bridge between the tab and
    // the tool bar (so each tab can inform the toolbar that it should hide
    // the collapse button).
    scoutAction.addPropertyChangeListener(IAction.PROP_SELECTED, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        boolean allTabsAreInactive = true;
        for (ISwingScoutAction<IToolButton> swingScoutToolButton : m_swingScoutToolButtons) {
          if (swingScoutToolButton.getScoutObject().isSelected()) {
            m_collapseButton.setText(swingScoutToolButton.getScoutObject().getText());
            allTabsAreInactive = false;
            break;
          }
        }
        if (allTabsAreInactive) {
          collapseBar();
        }
      }
    });
  }

  @SuppressWarnings("unchecked")
  private ISwingScoutAction<IToolButton> createSwingScoutToolButton(IToolButton scoutToolButton) {
    return getSwingEnvironment().createAction(this, scoutToolButton);
  }

  protected Icon createArrowRightIcon() {
    int arrowHeight = 8;
    int arrowWidth = arrowHeight / 2;
    BufferedImage img = new BufferedImage(arrowWidth + 5, arrowHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics g = img.createGraphics();
    g.setColor(UIManager.getColor("controlDkShadow"));
    g.fillPolygon(new int[]{0, arrowWidth, 0},
                  new int[]{0, arrowHeight / 2, arrowHeight},
                  3);

    return new ImageIcon(img);
  }
}
