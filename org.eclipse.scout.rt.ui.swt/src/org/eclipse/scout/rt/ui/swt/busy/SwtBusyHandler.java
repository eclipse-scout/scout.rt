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
package org.eclipse.scout.rt.ui.swt.busy;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.busy.AbstractBusyHandler;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.swt.widgets.Display;

/**
 * Default SWT busy handler for a {@link IClientSession}
 * 
 * @author imo
 * @since 3.8
 */
public class SwtBusyHandler extends AbstractBusyHandler {
  private final ISwtEnvironment m_env;
  private final Display m_display;

  public SwtBusyHandler(IClientSession session, ISwtEnvironment env) {
    super(session);
    m_env = env;
    m_display = env.getDisplay();
  }

  public ISwtEnvironment getSwtEnvironment() {
    return m_env;
  }

  public Display getDisplay() {
    return m_display;
  }

  @Override
  protected void runBusy(Job job) {
    //BusyJobStrategy.createSimpleJob(this, true).schedule();
    BusyJobStrategy.createWorkbenchJob(this, false).schedule();
  }

}
