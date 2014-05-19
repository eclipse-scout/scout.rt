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
package org.eclipse.scout.rt.ui.rap.action.menu;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.swt.widgets.Menu;

/**
 *
 */
public class RwtScoutMenuContributionItem extends ContributionItem {
  private static final long serialVersionUID = 1L;
  private IMenu m_scoutMenu;
  private IRwtEnvironment m_environment;
  private RwtScoutMenuItem m_swtMenuItem;

  public RwtScoutMenuContributionItem(IMenu scoutMenu, IRwtEnvironment environment) {
    m_scoutMenu = scoutMenu;
    m_environment = environment;

  }

  public IMenu getScoutMenu() {
    return m_scoutMenu;
  }

  public IRwtEnvironment getEnvironment() {
    return m_environment;
  }

  public RwtScoutMenuItem getSwtMenuItem() {
    return m_swtMenuItem;
  }

  @Override
  public void fill(Menu menu, int index) {
    m_swtMenuItem = new RwtScoutMenuItem(getScoutMenu(), menu, ActionUtility.createVisibleFilter(), getEnvironment());

  }
}
