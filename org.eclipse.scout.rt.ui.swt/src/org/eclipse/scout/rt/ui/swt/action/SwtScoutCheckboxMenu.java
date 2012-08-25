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

import org.eclipse.scout.rt.client.ui.action.menu.checkbox.ICheckBoxMenu;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class SwtScoutCheckboxMenu extends AbstractSwtMenuAction {

  public SwtScoutCheckboxMenu(Menu swtMenu, ICheckBoxMenu scoutMenu, ISwtEnvironment environment) {
    super(swtMenu, scoutMenu, true, environment);
  }

  @Override
  protected void initializeSwt(Menu swtMenu) {
    MenuItem item = new MenuItem(swtMenu, SWT.CHECK);
    setSwtMenuItem(item);
  }

  @Override
  protected void applyScoutProperties() {
    super.applyScoutProperties();
    setSelectedFromScout();
  }

  private void setSelectedFromScout() {
    if (!getSwtMenuItem().isDisposed()) {
      getSwtMenuItem().setSelection(getScoutAction().isSelected());
    }
  }

  /**
   * in swt thread
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ICheckBoxMenu.PROP_SELECTED)) {
      setSelectedFromScout();
    }
  }

}
