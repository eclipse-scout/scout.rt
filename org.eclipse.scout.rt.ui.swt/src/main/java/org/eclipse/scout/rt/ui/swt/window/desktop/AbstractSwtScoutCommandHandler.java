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
package org.eclipse.scout.rt.ui.swt.window.desktop;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironmentListener;
import org.eclipse.scout.rt.ui.swt.SwtEnvironmentEvent;

/**
 * <h3>AbstractSwtScoutCommandHandler</h3> ...
 * 
 * @since 1.0.0 06.05.2008
 */
public abstract class AbstractSwtScoutCommandHandler extends AbstractHandler {
  private final ISwtEnvironment m_environment;
  private final String m_actionQName;
  private IAction m_scoutAction;
  private P_ActionPropertyListener m_actionPropertyListener = new P_ActionPropertyListener();

  public AbstractSwtScoutCommandHandler(ISwtEnvironment environmet, String actionQName) {
    m_environment = environmet;
    m_actionQName = actionQName;
    if (!m_environment.isInitialized()) {
      m_environment.addEnvironmentListener(new P_EnvironmentListener());
    }
    else {
      setScoutAction(findAction());
    }
  }

  protected abstract IAction findAction();

  @Override
  public Object execute(ExecutionEvent arg0) throws ExecutionException {
    if (getScoutAction() != null) {
      Runnable job = new Runnable() {
        @Override
        public void run() {
          getScoutAction().getUIFacade().fireActionFromUI();
        }
      };
      m_environment.invokeScoutLater(job, 0);
    }
    return null;
  }

  protected void handleEnvironmentEvent(SwtEnvironmentEvent e) {
    switch (e.getType()) {
      case SwtEnvironmentEvent.STARTED:
        setScoutAction(findAction());

        break;
      case SwtEnvironmentEvent.STOPPED:
        m_scoutAction = null;
        fireHandlerChanged(new HandlerEvent(this, true, false));

        break;

      default:
        break;
    }
  }

  protected void updateHandler() {
  }

  @Override
  public boolean isEnabled() {
    if (getScoutAction() == null) {
      return false;
    }
    else {
      return getScoutAction().isEnabled();
    }
  }

  private class P_EnvironmentListener implements ISwtEnvironmentListener {
    @Override
    public void environmentChanged(final SwtEnvironmentEvent e) {
      Runnable job = new Runnable() {
        @Override
        public void run() {
          handleEnvironmentEvent(e);
        }
      };
      m_environment.getDisplay().asyncExec(job);
    }
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

  public IAction getScoutAction() {
    return m_scoutAction;
  }

  public void setScoutAction(IAction action) {
    if (m_scoutAction != null) {
      m_scoutAction.removePropertyChangeListener(m_actionPropertyListener);
    }
    m_scoutAction = action;
    if (m_scoutAction != null) {
      m_scoutAction.addPropertyChangeListener(m_actionPropertyListener);
    }
    fireHandlerChanged(new HandlerEvent(this, true, false));
  }

  protected String getActionQName() {
    return m_actionQName;
  }

  protected void handlePropertyChanged(PropertyChangeEvent e) {
    if (IAction.PROP_ENABLED == e.getPropertyName()) {
      fireHandlerChanged(new HandlerEvent(this, true, false));
    }
  }

  private class P_ActionPropertyListener implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
      Runnable job = new Runnable() {
        @Override
        public void run() {
          handlePropertyChanged(evt);
        }
      };
      getEnvironment().invokeSwtLater(job);
    }

  } // end class P_ActionPropertyListener
}
