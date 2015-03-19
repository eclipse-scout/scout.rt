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
package org.eclipse.scout.rt.ui.swing.services;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.rt.client.job.ClientJobInput;
import org.eclipse.scout.rt.client.job.ClientJobs;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.shared.services.common.useractivity.IUserActivityProvider;

public class UserActivityProvider extends AbstractPropertyObserver implements IUserActivityProvider {
  private long m_idleTrigger;
  private boolean m_userActive;
  private IFuture<Void> m_userInactiveJob;
  private long m_postponed;

  public UserActivityProvider() {
    m_idleTrigger = 30000L;
    // attach to awt
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
          @Override
          public void eventDispatched(AWTEvent e) {
            userBusy();
          }
        }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
      }
    });
  }

  @Override
  public boolean isActive() {
    return propertySupport.getPropertyBool(PROP_ACTIVE);
  }

  private void setActiveInternal(boolean b) {
    m_userActive = b;
    propertySupport.setPropertyBool(PROP_ACTIVE, b);
  }

  private synchronized void userBusy() {
    if (!m_userActive) {
      setActiveInternal(true);
    }
    if (m_userInactiveJob == null) {
      m_userInactiveJob = ClientJobs.schedule(new UserInactiveRunnable(), m_idleTrigger + 1000L, TimeUnit.MILLISECONDS, ClientJobInput.defaults().sessionRequired(false));
    }
    m_postponed = System.currentTimeMillis() + m_idleTrigger;
  }

  private synchronized void userIdle() {
    if (m_userInactiveJob != null) {
      long delta = m_postponed - System.currentTimeMillis();
      if (delta < 1000L) {
        setActiveInternal(false);
        m_userInactiveJob = null;
      }
      else {
        m_userInactiveJob = ClientJobs.schedule(new UserInactiveRunnable(), delta, TimeUnit.MILLISECONDS, ClientJobInput.defaults().sessionRequired(false));
      }
    }
  }

  private class UserInactiveRunnable implements IRunnable {
    @Override
    public void run() throws Exception {
      userIdle();
    }
  }

}
