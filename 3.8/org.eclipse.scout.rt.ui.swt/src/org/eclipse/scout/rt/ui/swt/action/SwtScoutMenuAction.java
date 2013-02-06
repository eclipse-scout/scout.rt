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

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * <h3>SwtScoutAction</h3> ...
 * 
 * @since 1.0.0 14.03.2008
 */
public class SwtScoutMenuAction extends AbstractSwtMenuAction {

  public SwtScoutMenuAction(Menu swtMenu, IAction action, ISwtEnvironment environment) {
    super(swtMenu, action, true, environment);
  }

  @Override
  protected void initializeSwt(Menu swtMenu) {
    MenuItem item = new MenuItem(swtMenu, SWT.PUSH);
    setSwtMenuItem(item);
  }

}
