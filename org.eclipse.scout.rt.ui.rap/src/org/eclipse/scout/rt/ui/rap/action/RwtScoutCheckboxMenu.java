/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.action;

import org.eclipse.scout.rt.client.ui.action.menu.checkbox.ICheckBoxMenu;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class RwtScoutCheckboxMenu extends AbstractRwtMenuAction {

  public RwtScoutCheckboxMenu(Menu uiMenu, ICheckBoxMenu scoutMenu, IRwtEnvironment uiEnvironment, boolean callInitializer) {
    super(uiMenu, scoutMenu, uiEnvironment, callInitializer);
  }

  public RwtScoutCheckboxMenu(Menu uiMenu, ICheckBoxMenu scoutMenu, IRwtEnvironment uiEnvironment) {
    this(uiMenu, scoutMenu, uiEnvironment, true);
  }

  @Override
  protected void initializeUi(Menu uiMenu) {
    MenuItem item = new MenuItem(uiMenu, SWT.CHECK);
    setUiMenuItem(item);
  }

  @Override
  protected void applyScoutProperties() {
    super.applyScoutProperties();
    setSelectedFromScout();
  }

  private void setSelectedFromScout() {
    if (!getUiMenuItem().isDisposed()) {
      getUiMenuItem().setSelection(getScoutAction().isSelected());
    }
  }

  /**
   * in rwt thread
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ICheckBoxMenu.PROP_SELECTED)) {
      setSelectedFromScout();
    }
  }

}
