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
package org.eclipse.scout.rt.client.ui.action.menu;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.beans.IPropertyObserver;

/**
 *
 */
public abstract class AbstractPropertyObserverContextMenu<T extends IPropertyObserver> extends AbstractContextMenu {

  private final T m_owner;

  public AbstractPropertyObserverContextMenu(T owner) {
    super(false);
    m_owner = owner;
    callInitializer();
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    getOwner().addPropertyChangeListener(new P_OwnerPropertyListener());
  }

  public T getOwner() {
    return m_owner;
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
