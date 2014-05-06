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
package org.eclipse.scout.rt.ui.swt.action.menu;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.swt.widgets.Menu;

/**
 *
 */
public class SwtScoutMenuContributionItem extends ContributionItem {
  private IMenu m_scoutMenu;
  private ISwtEnvironment m_environment;
  private ISwtScoutMenuItem m_swtMenuItem;

  public SwtScoutMenuContributionItem(IMenu scoutMenu, ISwtEnvironment environment) {
    m_scoutMenu = scoutMenu;
    m_environment = environment;

  }

  public IMenu getScoutMenu() {
    return m_scoutMenu;
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

  public ISwtScoutMenuItem getSwtMenuItem() {
    return m_swtMenuItem;
  }

  @Override
  public void fill(Menu menu, int index) {
    m_swtMenuItem = getEnvironment().createMenuItem(menu, getScoutMenu());

  }
}
