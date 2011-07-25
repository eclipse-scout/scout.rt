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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.AbstractButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractFormToolButton;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.action.ISwingScoutAction;
import org.eclipse.scout.rt.ui.swing.window.desktop.toolbar.AbstractJToolTabsBar;
import org.eclipse.scout.rt.ui.swing.window.desktop.toolbar.SwingScoutHeaderPanel;

public class JToolTabsBar extends AbstractJToolTabsBar {
  private static final long serialVersionUID = 1L;

  private final ISwingEnvironment m_env;
  private SwingScoutHeaderPanel m_swingScoutHeaderPanel;
  private final OptimisticLock m_syncLock;

  public JToolTabsBar(ISwingEnvironment env) {
    m_env = env;
    m_syncLock = new OptimisticLock();
    setLayout(new GridBagLayout());
    setOpaque(false);
  }

  @Override
  public void rebuild(IDesktop desktop) {
    removeAll();

    JToolBar swingToolBar = new JToolBar(JToolBar.HORIZONTAL);
    swingToolBar.setFloatable(false);
    swingToolBar.setBorder(new EmptyBorder(0, 0, 0, 3));
    swingToolBar.setOpaque(false);
    swingToolBar.setLayout(new GridBagLayout());

    for (IToolButton scoutToolButton : desktop.getToolButtons()) {
      if (!(scoutToolButton instanceof AbstractFormToolButton)) {
        continue; // only render @{link AbstractFormToolButton}'s
      }

      ISwingScoutAction<IToolButton> swingScoutToolButton = createSwingScoutToolButton(scoutToolButton);
      if (swingScoutToolButton != null) {
        AbstractButton swingButton = (AbstractButton) swingScoutToolButton.getSwingField();
        addActiveTabListener(swingButton);

        // layout tool button
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 2, 0, 0);
        gbc.fill = GridBagConstraints.VERTICAL;
        swingToolBar.add(swingButton, gbc);
      }
    }

    // top filler panel (transparent)
    JPanel filler = new JPanel();
    filler.setPreferredSize(new Dimension(-1, 0)); // set height to 0 to not claim extra space
    filler.setOpaque(false);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.weightx = 0;
    gbc.weighty = 1;
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;
    add(filler, gbc);

    // toolbar
    gbc = new GridBagConstraints();
    gbc.weightx = 0;
    gbc.weighty = 0;
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.SOUTH;
    add(swingToolBar, gbc);

    swingToolBar.addComponentListener(new ComponentAdapter() {

      private int m_width;

      @Override
      public void componentResized(ComponentEvent e) {
        try {
          if (m_syncLock.acquire()) {
            int newWidth = getPreferredSize().width;
            if (m_width != newWidth) {
              m_width = newWidth;
              m_swingScoutHeaderPanel.adjustToolButtonPanelWidth(getPreferredSize().width);
            }
          }
        }
        finally {
          m_syncLock.release();
        }
      }

    });
  }

  @Override
  public void setSwingScoutHeaderPanel(SwingScoutHeaderPanel swingScoutHeaderPanel) {
    m_swingScoutHeaderPanel = swingScoutHeaderPanel;
  }

  @SuppressWarnings("unchecked")
  private ISwingScoutAction<IToolButton> createSwingScoutToolButton(IToolButton scoutToolButton) {
    return m_env.createAction(this, scoutToolButton);
  }
}
