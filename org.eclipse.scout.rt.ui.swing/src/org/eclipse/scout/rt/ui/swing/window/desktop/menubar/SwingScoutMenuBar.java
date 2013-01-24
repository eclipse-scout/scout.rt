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
package org.eclipse.scout.rt.ui.swing.window.desktop.menubar;

import java.util.Arrays;

import javax.swing.JMenuBar;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;

public class SwingScoutMenuBar extends SwingScoutComposite<IDesktop> {

  private int m_topLevelMenuCount;

  @Override
  protected void initializeSwing() {
    m_topLevelMenuCount = getScoutObject().getMenus().length;
    JMenuBar menuBar = new JMenuBar();
    setSwingField(menuBar);
    rebuildMenuBar();
  }

  @Override
  protected boolean isHandleScoutPropertyChange(String name, Object newValue) {
    return false;
  }

  public boolean isEmpty() {
    return m_topLevelMenuCount == 0;
  }

  public JMenuBar getSwingMenuBar() {
    return (JMenuBar) getSwingField();
  }

  private void rebuildMenuBar() {
    IMenu[] toplevelMenus = getScoutObject().getMenus();
    JMenuBar menuBar = getSwingMenuBar();
    menuBar.removeAll();
    getSwingEnvironment().appendActions(menuBar, Arrays.asList(toplevelMenus));
  }
}
