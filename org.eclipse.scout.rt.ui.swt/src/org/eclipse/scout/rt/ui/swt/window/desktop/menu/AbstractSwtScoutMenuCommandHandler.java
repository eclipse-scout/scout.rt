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
package org.eclipse.scout.rt.ui.swt.window.desktop.menu;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.window.desktop.AbstractSwtScoutCommandHandler;

/**
 * <h3>AbstractSwtScoutMenuCommandHandler</h3> ...
 * 
 * @since 1.0.0 06.05.2008
 */
public abstract class AbstractSwtScoutMenuCommandHandler extends AbstractSwtScoutCommandHandler {

  public AbstractSwtScoutMenuCommandHandler(ISwtEnvironment environmet, String actionQName) {
    super(environmet, actionQName);
  }

  @Override
  protected IAction findAction() {
    return DesktopUtility.findDesktopMenu(getActionQName(), getEnvironment());
  }
}
