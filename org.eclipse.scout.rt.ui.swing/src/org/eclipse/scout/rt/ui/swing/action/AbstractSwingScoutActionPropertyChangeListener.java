/*******************************************************************************
 * Copyright (c) 2010, 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.action;

import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.WeakEventListener;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

/**
 * Property change listener that can be attached to particular Scout menus.
 * 
 * @since 4.0.0-M6
 */
public abstract class AbstractSwingScoutActionPropertyChangeListener implements PropertyChangeListener, WeakEventListener {

  public void attachToScoutMenus(IMenu[] menus) {
    for (IMenu menu : menus) {
      menu.addPropertyChangeListener(this);
    }
  }

  public void detachFromScoutMenus(IMenu[] menus) {
    for (IMenu menu : menus) {
      menu.removePropertyChangeListener(this);
    }
  }
}
