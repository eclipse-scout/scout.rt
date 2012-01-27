/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.basic;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.ui.rap.core.util.BrowserInfo;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

public abstract class AbstractOpenMenuJob extends JobEx {
  private Point m_eventPosition;
  private boolean m_openMenu = false;

  private final Control m_UiField;
  private BrowserInfo m_browserInfo = RwtUtility.getBrowserInfo();

  public AbstractOpenMenuJob(Control UiField) {
    super("OpenMenuJob");
    m_UiField = UiField;
  }

  public void startOpenJob(Point eventPosition) {
    if (m_browserInfo.isTablet()
        || m_browserInfo.isMobile()) {
      setEventPosition(eventPosition);
      setOpenMenu(true);
      schedule(500L);
    }
  }

  public void stopOpenJob() {
    setOpenMenu(false);
    if (Job.SLEEPING == getState()
          || Job.WAITING == getState()) {
      cancel();
    }
  }

  public boolean openMenuCheck() {
    return getUiField() != null
        && !getUiField().isDisposed();
  }

  public abstract void showMenu(Point pt);

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    if (isOpenMenu()) {
      getUiField().getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          if (openMenuCheck()) {
            Point pt = getUiField().toDisplay(getEventPosition());
            //Position of menu is moved 50px to the right, so it doesn't popup right under the finger
            pt.x = pt.x + 50;
            showMenu(pt);
          }
        }
      });
    }
    return Status.OK_STATUS;
  }

  private Control getUiField() {
    return m_UiField;
  }

  public void setEventPosition(Point eventPosition) {
    m_eventPosition = eventPosition;
  }

  public Point getEventPosition() {
    return m_eventPosition;
  }

  public void setOpenMenu(boolean openMenu) {
    m_openMenu = openMenu;
  }

  public boolean isOpenMenu() {
    return m_openMenu;
  }
}
