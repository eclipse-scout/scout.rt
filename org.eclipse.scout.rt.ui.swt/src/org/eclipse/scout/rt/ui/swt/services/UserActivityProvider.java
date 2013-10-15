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
package org.eclipse.scout.rt.ui.swt.services;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.rt.shared.services.common.useractivity.IUserActivityProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.ServiceRegistration;

public class UserActivityProvider extends AbstractPropertyObserver implements IUserActivityProvider {
  private long m_idleTrigger;
  private boolean m_userActive;
  private UserInactiveJob m_userInactiveJob;
  private Object m_jobLock = new Object();

  public UserActivityProvider() {
    m_idleTrigger = 30000L;
    // attach to swt but do NOT use Display.getDefault().
    //wait until the worbench is available
    Job job = new Job("UserActivityProvider waiting for workbench") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        if (PlatformUI.isWorkbenchRunning()) {
          attachToDisplay(PlatformUI.getWorkbench().getDisplay());
        }
        else {
          //re-schedule
          schedule(1000);
        }
        return Status.OK_STATUS;
      }
    };
    job.setUser(false);
    job.setSystem(true);
    job.schedule(1000);
  }

  private void attachToDisplay(final Display display) {
    display.asyncExec(new Runnable() {
      @Override
      public void run() {
        display.addFilter(SWT.MouseMove, new Listener() {
          @Override
          public void handleEvent(Event event) {
            userBusy();
          }
        });
        display.addFilter(SWT.KeyDown, new Listener() {
          @Override
          public void handleEvent(Event event) {
            userBusy();
          }
        });
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
