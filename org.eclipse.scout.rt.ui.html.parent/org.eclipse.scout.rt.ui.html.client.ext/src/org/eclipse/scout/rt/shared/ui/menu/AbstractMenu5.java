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
package org.eclipse.scout.rt.shared.ui.menu;

import java.util.EventListener;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;

public abstract class AbstractMenu5 extends AbstractMenu implements IMenu5 {
  private final EventListenerList m_listenerList = new EventListenerList();

  public AbstractMenu5() {
    super(true);
  }

  public AbstractMenu5(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  public void doAction() throws ProcessingException {
    if (isEnabled() && isVisible()) {
      try {
        setEnabledProcessingAction(false);
        doActionInternal();
        fireActionPerformed();
      }
      finally {
        setEnabledProcessingAction(true);
      }
    }
  }

  @Override
  public void addActionListener(ActionListener listener) {
    m_listenerList.add(ActionListener.class, listener);
  }

  @Override
  public void removeActionListener(ActionListener listener) {
    m_listenerList.remove(ActionListener.class, listener);
  }

  private void fireActionPerformed() {
    fireActionEvent(new ActionEvent(this, ActionEvent.TYPE_PERFORMED));
  }

  private void fireActionEvent(ActionEvent e) {
    EventListener[] listeners = m_listenerList.getListeners(ActionListener.class);
    if (listeners != null && listeners.length > 0) {
      for (int i = 0; i < listeners.length; i++) {
        ((ActionListener) listeners[i]).actionChanged(e);
      }
    }
  }

}
