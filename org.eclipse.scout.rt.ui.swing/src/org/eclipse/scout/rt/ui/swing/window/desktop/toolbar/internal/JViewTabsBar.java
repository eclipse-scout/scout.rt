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

import javax.swing.AbstractButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.action.ISwingScoutAction;
import org.eclipse.scout.rt.ui.swing.window.desktop.toolbar.AbstractJViewTabsBar;

public class JViewTabsBar extends AbstractJViewTabsBar {
  private static final long serialVersionUID = 1L;

  private final ISwingEnvironment m_env;

  public JViewTabsBar(ISwingEnvironment env) {
    m_env = env;
    setLayout(new GridBagLayout());
    setOpaque(false);
  }

  @Override
  public void rebuild(IDesktop desktop) {
    removeAll();

    JToolBar swingToolBar = new JToolBar(JToolBar.HORIZONTAL);
    swingToolBar.setFloatable(false);
    swingToolBar.setOpaque(false);
    swingToolBar.setBorder(new EmptyBorder(0, 3, 0, 0));
    swingToolBar.setLayout(new GridBagLayout());
    for (IViewButton scoutViewButton : desktop.getViewButtons()) {
      ISwingScoutAction<IViewButton> swingScoutViewButton = createSwingScoutViewButton(scoutViewButton);
      if (swingScoutViewButton != null) {
        AbstractButton swingButton = (AbstractButton) swingScoutViewButton.getSwingField();
        addActiveTabListener(swingButton);

        // layout outline button
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 0, 2);
        swingToolBar.add(swingButton, gbc);
      }
    }

    // top filler panel (transparent)
    JPanel filler = new JPanel();
    filler.setPreferredSize(new Dimension(-1, 0)); // set height to 0 to not claim extra space
    filler.setOpaque(false);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weighty = 1;
    gbc.fill = GridBagConstraints.BOTH;
    add(filler, gbc);

    // toolbar
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.weightx = 0;
    gbc.weighty = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;
    add(swingToolBar, gbc);

    // right filler (transparent)
    filler = new JPanel();
    filler.setOpaque(false);
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.gridheight = 2;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.fill = GridBagConstraints.BOTH;
    add(filler, gbc);
  }

  protected ISwingEnvironment getSwingEnvironment() {
    return m_env;
  }

  @SuppressWarnings("unchecked")
  private ISwingScoutAction<IViewButton> createSwingScoutViewButton(IViewButton scoutViewButton) {
    return getSwingEnvironment().createAction(this, scoutViewButton);
  }
}
