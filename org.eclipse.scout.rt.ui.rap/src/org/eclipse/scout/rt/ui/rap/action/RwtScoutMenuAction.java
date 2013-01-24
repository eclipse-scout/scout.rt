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

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * @since 3.8.0
 */
public class RwtScoutMenuAction extends AbstractRwtMenuAction {

  public RwtScoutMenuAction(Menu uiMenu, IAction action, IRwtEnvironment environment) {
    super(uiMenu, action, environment, true);
  }

  @Override
  protected void initializeUi(Menu uiMenu) {
    MenuItem item = new MenuItem(uiMenu, SWT.PUSH);
    setUiMenuItem(item);
  }

}
