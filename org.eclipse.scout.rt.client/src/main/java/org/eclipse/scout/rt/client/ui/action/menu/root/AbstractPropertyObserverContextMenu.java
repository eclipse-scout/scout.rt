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
package org.eclipse.scout.rt.client.ui.action.menu.root;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;

public abstract class AbstractPropertyObserverContextMenu<T extends IPropertyObserver> extends AbstractContextMenu {

  public AbstractPropertyObserverContextMenu(T owner, List<? extends IMenu> initialChildMenus) {
    super(owner, initialChildMenus);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    getOwner().addPropertyChangeListener(new P_OwnerPropertyListener());
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getOwner() {
    return (T) super.getOwner();
  }

  protected void handleOwnerPropertyChanged(PropertyChangeEvent evt) {
  }

  private class P_OwnerPropertyListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      handleOwnerPropertyChanged(evt);
    }
  }

}
