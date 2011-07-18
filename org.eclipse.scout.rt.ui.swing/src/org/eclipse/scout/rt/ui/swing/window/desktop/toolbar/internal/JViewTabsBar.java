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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.AbstractButton;
import javax.swing.JToolBar;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;

import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.action.ISwingScoutAction;
import org.eclipse.scout.rt.ui.swing.window.desktop.toolbar.AbstractJViewTabsBar;

public class JViewTabsBar extends AbstractJViewTabsBar {
  private static final long serialVersionUID = 1L;

  private final ISwingEnvironment m_env;

  private final SpringLayout m_layout;

  public JViewTabsBar(ISwingEnvironment env) {
    m_env = env;
    m_layout = new SpringLayout();
    setLayout(m_layout);
  }

  @Override
  public void rebuild(IDesktop desktop) {
    removeAll();

    JToolBar swingToolBar = new JToolBar(JToolBar.HORIZONTAL);
    swingToolBar.setFloatable(false);
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

    add(swingToolBar);

    m_layout.putConstraint(SpringLayout.WEST, swingToolBar, 0, SpringLayout.WEST, this);
    m_layout.putConstraint(SpringLayout.SOUTH, swingToolBar, 0, SpringLayout.SOUTH, this);
  }

  protected ISwingEnvironment getSwingEnvironment() {
    return m_env;
  }

  @SuppressWarnings("unchecked")
  private ISwingScoutAction<IViewButton> createSwingScoutViewButton(IViewButton scoutViewButton) {
    return getSwingEnvironment().createAction(this, scoutViewButton);
  }
}
