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

import javax.swing.SwingUtilities;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.rt.shared.services.common.useractivity.IUserActivityProvider;
import org.osgi.framework.ServiceRegistration;

public class UserActivityProvider extends AbstractPropertyObserver implements IUserActivityProvider {
  private long m_idleTrigger;
  private boolean m_userActive;
  private UserInactiveJob m_userInactiveJob;
  private Object m_jobLock = new Object();

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
  public void initializeService(ServiceRegistration registration) {
    // nop
  }

  @Override
  public boolean isActive() {
    return propertySupport.getPropertyBool(PROP_ACTIVE);
  }

  private void setActiveInternal(boolean b) {
    m_userActive = b;
    propertySupport.setPropertyBool(PROP_ACTIVE, b);
  }

  private void userBusy() {
    synchronized (m_jobLock) {
      if (!m_userActive) {
        setActiveInternal(true);
      }
      if (m_userInactiveJob == null) {
        m_userInactiveJob = new UserInactiveJob();
        m_userInactiveJob.schedule(m_idleTrigger + 1000L);
      }
      m_userInactiveJob.postponed = System.currentTimeMillis() + m_idleTrigger;
    }
  }

  private void userIdle() {
    synchronized (m_jobLock) {
      if (m_userInactiveJob != null) {
        long delta = m_userInactiveJob.postponed - System.currentTimeMillis();
        if (delta < 1000L) {
          setActiveInternal(false);
          m_userInactiveJob = null;
        }
        else {
          m_userInactiveJob.schedule(delta);
        }
      }
    }
  }

  private class UserInactiveJob extends Job {
    long postponed;

    public UserInactiveJob() {
      super("User activity");
      setUser(false);
      setSystem(true);
      setPriority(Job.DECORATE);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      userIdle();
      return Status.OK_STATUS;
    }
  }

}
