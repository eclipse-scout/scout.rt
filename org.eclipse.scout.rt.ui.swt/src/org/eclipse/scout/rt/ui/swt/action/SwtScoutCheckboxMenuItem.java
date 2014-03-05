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
package org.eclipse.scout.rt.ui.swt.action;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.swt.widgets.Menu;

/**
 *
 */
public class SwtScoutCheckboxMenuItem extends SwtScoutMenuItem {

  private boolean m_handleSelectionPending;

  /**
   * @param scoutMenu
   * @param parentMenu
   * @param environment
   */
  public SwtScoutCheckboxMenuItem(IMenu scoutMenu, Menu parentMenu, ISwtEnvironment environment) {
    super(scoutMenu, parentMenu, environment);
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    updateSelectedFromScout();
  }

  protected void updateSelectedFromScout() {
    if (getSwtMenuItem() != null && !getSwtMenuItem().isDisposed()) {
      getSwtMenuItem().setSelection(getScoutMenu().isSelected());
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IMenu.PROP_SELECTED)) {
      updateSelectedFromScout();
    }
  }

}
